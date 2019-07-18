package com.lglab.ivan.lgxeducontroller.activities.manager;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.MenuCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.games.ISaveData;
import com.lglab.ivan.lgxeducontroller.legacy.beans.POI;
import com.lglab.ivan.lgxeducontroller.legacy.data.POIsContract;
import com.lglab.ivan.lgxeducontroller.legacy.data.POIsProvider;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/*This fragment is the responsible to create POIs, Tours and Categories*/
public class CreatePOIFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener, LocationListener, GoogleMap.OnMapLongClickListener {

    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    static CreatePOIFragment fragment;
    private static View rootView = null;
    private ViewHolderPoi viewHolderPoi;
    private static Map<String, String> spinnerIDsAndShownNames;

    private GoogleMap map;
    private LocationManager locationManager;
    private int POIButton = 0;

    public static CreatePOIFragment newInstance() {
        fragment = new CreatePOIFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapsInitializer.initialize(getActivity());
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem itemSettings = menu.findItem(R.id.action_settings);
        if (itemSettings != null) {
            itemSettings.setVisible(false);
        }

        inflater.inflate(R.menu.menu_create_or_edit_poi, menu);
        MenuCompat.setGroupDividerEnabled(menu, true);

        Drawable drawable = menu.findItem(R.id.save_poi).getIcon();
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(getContext(), R.color.whiteGrey));
        menu.findItem(R.id.save_poi).setIcon(drawable);

        drawable = menu.findItem(R.id.close_poi).getIcon();
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(getContext(), R.color.whiteGrey));
        menu.findItem(R.id.close_poi).setIcon(drawable);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.save_poi) {
            if(viewHolderPoi != null)
                createPOI(viewHolderPoi);
        } else if(id == R.id.close_poi) {
            getActivity().onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle extras = getActivity().getIntent().getExtras();
        rootView = null;

        if (extras != null) {
            POIButton = extras.getInt("POI_BUTTON");
        }
        getActivity().setTitle(getResources().getString(R.string.new_poi));
        //If admin user is creating a POI, first of all layout settings are shown on the screen.
        viewHolderPoi = setPOILayoutSettings(inflater, container);
        SupportMapFragment fragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        fragment.getMapAsync(this);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this); //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER
        } catch (SecurityException ignored) {

        }
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setRotateGesturesEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(true);
        try {
            map.setMyLocationEnabled(true);
        } catch (SecurityException ignored) {

        }
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        map.setOnMapLongClickListener(this);
        map.setOnMarkerDragListener(this);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {
        ((EditText) rootView.findViewById(R.id.longitude)).setText(String.valueOf(marker.getPosition().longitude));
        ((EditText) rootView.findViewById(R.id.latitude)).setText(String.valueOf(marker.getPosition().latitude));
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        ((EditText) rootView.findViewById(R.id.longitude)).setText(String.valueOf(marker.getPosition().longitude));
        ((EditText) rootView.findViewById(R.id.latitude)).setText(String.valueOf(marker.getPosition().latitude));
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
        if (map != null) {
            map.animateCamera(cameraUpdate);
        }
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        map.clear();

        MarkerOptions marker = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
        marker.position(latLng).draggable(true);
        map.addMarker(marker);

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, map.getCameraPosition().zoom));

        ((EditText) rootView.findViewById(R.id.longitude)).setText(String.valueOf(marker.getPosition().longitude));
        ((EditText) rootView.findViewById(R.id.latitude)).setText(String.valueOf(marker.getPosition().latitude));

    }

    /*POIs TREATMENT*/
    private void createPOI(ViewHolderPoi viewHolder) {
        try {
            POI poi = getPOIFromInputForm(viewHolder);
            long id = POIsProvider.insertPOI(poi);
            poi.setId(id);
            Intent data = new Intent();
            data.putExtra("POI", poi);
            data.putExtra("button", POIButton);
            getActivity().setResult(RESULT_OK, data);
            getActivity().finish();

        } catch (NumberFormatException e) {
            Toast.makeText(getActivity(), getResources().getString(R.string.poiNumericFields), Toast.LENGTH_LONG).show();
        }
    }

    private POI getPOIFromInputForm(ViewHolderPoi viewHolder) {

        int categoryID;

        String visitedPlace = viewHolder.visitedPlaceET.getText().toString();
        String completeName = viewHolder.name.getText().toString();
        float longitude = Float.parseFloat(viewHolder.longitudeET.getText().toString());
        float latitude = Float.parseFloat(viewHolder.latitudeET.getText().toString());
        float altitude = Float.parseFloat(viewHolder.altitudeET.getText().toString());
        float heading = Float.parseFloat(viewHolder.headingET.getText().toString());
        float tilt = Float.parseFloat(viewHolder.tiltET.getText().toString());
        float range = Float.parseFloat(viewHolder.rangeET.getText().toString());
        int hide = getHideValueFromInputForm(viewHolder.switchButtonHide);

        String altitudeMode = viewHolder.spinnerAltitudeMode.getSelectedItem().toString();

        String shownName = getShownNameValueFromInputForm(viewHolder.categoryID);
        categoryID = getFatherIDValueFromInputForm(shownName);


        POI newPoi = new POI();
        newPoi.setName(completeName);
        newPoi.setVisited_place(visitedPlace);
        newPoi.setLongitude(longitude);
        newPoi.setLatitude(latitude);
        newPoi.setAltitude(altitude);
        newPoi.setHeading(heading);
        newPoi.setTilt(tilt);
        newPoi.setRange(range);
        newPoi.setAltitudeMode(altitudeMode);
        newPoi.setHidden(hide != 0);
        newPoi.setCategoryId(categoryID);

        return newPoi;
    }

    private ViewHolderPoi setPOILayoutSettings(LayoutInflater inflater, ViewGroup container) {

        rootView = inflater.inflate(R.layout.fragment_create_or_update_poi, container, false);
        ViewHolderPoi viewHolder = new ViewHolderPoi(rootView);

        try {
            fillCategorySpinner(viewHolder.categoryID);
        } catch (Exception ignored) {

        }

        return viewHolder;
    }

    private void fillCategorySpinner(Spinner spinner) {
        List<String> list = new ArrayList<>();
        list.add(getResources().getString(R.string.noRouteText));
        spinnerIDsAndShownNames = new HashMap<>();

        Cursor queryCursor = POIsContract.CategoryEntry.getIDsAndShownNamesOfAllCategories(getActivity());
        while (queryCursor.moveToNext()) {
            spinnerIDsAndShownNames.put(queryCursor.getString(1), String.valueOf(queryCursor.getInt(0)));
            list.add(queryCursor.getString(1));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private int getHideValueFromInputForm(Switch switchButton) {
        int hideValue = 1;
        if (switchButton.isChecked()) {
            hideValue = 0;
        }
        return hideValue;
    }

    private String getShownNameValueFromInputForm(Spinner spinner) {
        if (spinner.getSelectedItem() == null || (spinner.getSelectedItem().toString()).equals("NO ROUTE")) {
            return "";
        } else {
            return spinner.getSelectedItem().toString();
        }
    }

    private int getFatherIDValueFromInputForm(String shownNameSelected) {
        if (shownNameSelected.equals("")) {
            return 0;
        } else {
            return Integer.parseInt(spinnerIDsAndShownNames.get(shownNameSelected));
        }
    }

    //These three ViewHolder classes are a kind of containers which contain all the elements related
    //to the creation of one item. The POIs once contains the elements for creating a POI, the Tour
    //once to be able to create a Tour and the same with the Categories once.
    public static class ViewHolderPoi {

        public EditText name;
        EditText visitedPlaceET;
        EditText longitudeET;
        EditText latitudeET;
        EditText altitudeET;
        EditText headingET;
        EditText tiltET;
        EditText rangeET;
        Spinner categoryID;
        Spinner spinnerAltitudeMode;
        private Switch switchButtonHide;

        ViewHolderPoi(final View rootView) {

            name = rootView.findViewById(R.id.name);
            visitedPlaceET = rootView.findViewById(R.id.visited_place);
            longitudeET = rootView.findViewById(R.id.longitude);
            latitudeET = rootView.findViewById(R.id.latitude);
            altitudeET = rootView.findViewById(R.id.altitude);
            headingET = rootView.findViewById(R.id.heading);
            tiltET = rootView.findViewById(R.id.tilt);
            rangeET = rootView.findViewById(R.id.range);
            spinnerAltitudeMode = rootView.findViewById(R.id.spinnerAltitude);

            categoryID = rootView.findViewById(R.id.categoryID_spinner);
            categoryID.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long selectedItemId) {
                    Cursor categories = POIsContract.CategoryEntry.getCategoriesByName(fragment.getActivity(), "EARTH");
                    long earthCategorycategoryId = 0;
                    if (categories != null && categories.moveToFirst()) {
                        //Category Exists, we fetch it
                        earthCategorycategoryId = POIsContract.CategoryEntry.getIdByShownName(fragment.getActivity(), "EARTH/");

                        if (selectedItemId != 0 && earthCategorycategoryId != selectedItemId) {
                            rootView.findViewById(R.id.mapPOILayout).setVisibility(View.GONE);
                        } else {
                            rootView.findViewById(R.id.mapPOILayout).setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            switchButtonHide = rootView.findViewById(R.id.switchButtonHide);
        }
    }
}