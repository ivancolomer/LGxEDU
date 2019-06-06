package com.lglab.ivan.lgxeducontroller.activities;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

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
import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.legacy.LGPCAdminActivity;
import com.lglab.ivan.lgxeducontroller.legacy.POISFragment;
import com.lglab.ivan.lgxeducontroller.legacy.data.POIsContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*This fragment is the responsible to create POIs, Tours and Categories*/
public class CreateItemFragment_Copy extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener, LocationListener, GoogleMap.OnMapLongClickListener {

    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    static CreateItemFragment_Copy fragment;
    private static View rootView = null;
    private static Map<String, String> spinnerIDsAndShownNames;

    GoogleMap map;
    private LocationManager locationManager;
    private String creationType;
    private int button;
    private Cursor queryCursor;


    public CreateItemFragment_Copy() {

    }

    public static CreateItemFragment_Copy newInstance() {
        fragment = new CreateItemFragment_Copy();
        return fragment;
    }

    /*    To be able to add one POI inside the Tour POIs List, as it is said inside setTourLayoutSettings method,
         user will select one POI by clicking on one instance of POIsFragment and adding it to the list and
        for this reason is why this method is called by POIsFragment class.*/

    private static void screenSizeTreatment(ImageView delete) {
        DisplayMetrics metrics = new DisplayMetrics();
        FragmentActivity act = (FragmentActivity) rootView.getContext();
        act.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;
        float scaleFactor = metrics.density;


        //The size of the diagonal in inches is equal to the square root of the height in inches squared plus the width in inches squared.
        float widthDp = widthPixels / scaleFactor;
        float heightDp = heightPixels / scaleFactor;

        float smallestWidth = Math.min(widthDp, heightDp);

        if (smallestWidth >= 1000) {
            delete.setImageResource(R.drawable.ic_remove_circle_black_36dp);
        }
    }

    private static boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
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
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle extras = getActivity().getIntent().getExtras();
        rootView = null;

        if (extras != null) {
            this.creationType = extras.getString("CREATION_TYPE");
            try {
                this.button = extras.getInt("Button");
            } catch (Exception e) {

            }
        }

        //When creation button (the once with the arrow's symbol inside, located in POIsFragment) is
        //clicked, this class looks at extras Bundle to know what kind of item it has to create.
        if (creationType != null && creationType.startsWith("POI")) {
            getActivity().setTitle(getResources().getString(R.string.new_poi));
            //If admin user is creating a POI, first of all layout settings are shown on the screen.
            final ViewHolderPoi viewHolder = setPOILayoutSettings(inflater, container);
            viewHolder.createPOI.setOnClickListener(v -> {//When POIs Creation button is clicked
                createPOI(viewHolder);
            });

            SupportMapFragment fragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            fragment.getMapAsync(this);
        } else {//CATEGORY
            getActivity().setTitle(getResources().getString(R.string.new_category));
            final ViewHolderCategory viewHolder = setCategoryLayoutSettings(inflater, container);
            viewHolder.createCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createCategory(viewHolder);
                }
            });
        }

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this); //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER
        } catch (SecurityException e) {
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
        } catch (SecurityException e) {
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
            //We get the values that user has typed inside input objects.
            ContentValues contentValues = getContentValuesFromDataFromPOIInputForm(viewHolder);

            POIsContract.POIEntry.createNewPOI(getActivity(), contentValues);

            //After creation, the next view page on screen would be the once corresponding to the
            //admin once.
            Intent intent = new Intent(getActivity(), LGPCAdminActivity.class);
            intent.putExtra("comeFrom", "pois");
            startActivity(intent);

        } catch (NumberFormatException e) {
            Toast.makeText(getActivity(), getResources().getString(R.string.poiNumericFields), Toast.LENGTH_LONG).show();
        }
    }

    private ContentValues getContentValuesFromDataFromPOIInputForm(ViewHolderPoi viewHolder) {

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

        //If, in POIsFragment, admin has clicked Creation Here button, the algorythm takes the
        //category ID of the once shown on screen.
        if (creationType.endsWith("HERE")) {
            categoryID = POISFragment.routeID;
        } else if (creationType.endsWith("HERENEW")) {
            Bundle extras = getActivity().getIntent().getExtras();
            categoryID = Integer.parseInt(extras.getString("CATEGORY_ID"));
        } else {
            //Contrary, the algorythm takes the category name selected and gets its ID.
            String shownName = getShownNameValueFromInputForm(viewHolder.categoryID);
            categoryID = getFatherIDValueFromInputForm(shownName);
        }


        ContentValues contentValues = new ContentValues();

        contentValues.put(POIsContract.POIEntry.COLUMN_COMPLETE_NAME, completeName);
        contentValues.put(POIsContract.POIEntry.COLUMN_VISITED_PLACE_NAME, visitedPlace);
        contentValues.put(POIsContract.POIEntry.COLUMN_LONGITUDE, longitude);
        contentValues.put(POIsContract.POIEntry.COLUMN_LATITUDE, latitude);
        contentValues.put(POIsContract.POIEntry.COLUMN_ALTITUDE, altitude);
        contentValues.put(POIsContract.POIEntry.COLUMN_HEADING, heading);
        contentValues.put(POIsContract.POIEntry.COLUMN_TILT, tilt);
        contentValues.put(POIsContract.POIEntry.COLUMN_RANGE, range);
        contentValues.put(POIsContract.POIEntry.COLUMN_ALTITUDE_MODE, altitudeMode);
        contentValues.put(POIsContract.POIEntry.COLUMN_HIDE, hide);
        contentValues.put(POIsContract.POIEntry.COLUMN_CATEGORY_ID, categoryID);

        return contentValues;
    }

    private ViewHolderPoi setPOILayoutSettings(LayoutInflater inflater, ViewGroup container) {

        rootView = inflater.inflate(R.layout.fragment_create_or_update_poi, container, false);
        final ViewHolderPoi viewHolder = new ViewHolderPoi(rootView);
        viewHolder.updatePOI.setVisibility(View.GONE);
        viewHolder.createPOI.setVisibility(View.VISIBLE);


        //If user has clicked on Create Here, obviously, no spinner categories option will be shown.
        if (creationType.endsWith("HERE")) {
            viewHolder.categoryID.setVisibility(View.GONE);

            Cursor categories = POIsContract.CategoryEntry.getCategoriesByName(getActivity(), "EARTH");
            long earthCategorycategoryId;

            if (categories != null && categories.moveToFirst()) {
                //Category Exists, we fetch it
                earthCategorycategoryId = POIsContract.CategoryEntry.getIdByShownName(getActivity(), "EARTH/");

                if (POISFragment.routeID != 0 && earthCategorycategoryId != POISFragment.routeID) {
                    rootView.findViewById(R.id.mapPOILayout).setVisibility(View.GONE);
                } else {
                    rootView.findViewById(R.id.mapPOILayout).setVisibility(View.VISIBLE);
                }
            }
        } else if (creationType.endsWith("HERENEW")) {
            viewHolder.categoryID.setVisibility(View.GONE);

            Bundle extras = getActivity().getIntent().getExtras();
            POISFragment.routeID = Integer.parseInt(extras.getString("CATEGORY_ID"));

            Cursor categories = POIsContract.CategoryEntry.getCategoriesByName(getActivity(), "EARTH");
            long earthCategorycategoryId;

            if (categories != null && categories.moveToFirst()) {
                //Category Exists, we fetch it
                earthCategorycategoryId = POIsContract.CategoryEntry.getIdByShownName(getActivity(), "EARTH/");

                //We check if category belongs to earth in order to display the map
                if (categories.getString(categories.getColumnIndex(POIsContract.CategoryEntry.COLUMN_SHOWN_NAME)).toUpperCase().contains("EARTH")) {
                    rootView.findViewById(R.id.mapPOILayout).setVisibility(View.VISIBLE);
                } else if (POISFragment.routeID != 0 && earthCategorycategoryId != POISFragment.routeID) {
                    rootView.findViewById(R.id.mapPOILayout).setVisibility(View.GONE);
                } else {
                    rootView.findViewById(R.id.mapPOILayout).setVisibility(View.VISIBLE);
                }
            }
        } else {
            try {
                fillCategorySpinner(viewHolder.categoryID);
            } catch (Exception e) {
            }
        }
        //On the screen there is a button to cancel the creation and return to the main administration view
        setCancelComeBackBehaviour(viewHolder.cancel);

        return viewHolder;
    }

    /*CATEGORIES TREATMENT*/
    private void createCategory(ViewHolderCategory viewHolder) {
        //The same with POIs, but with categories
        ContentValues contentValues = getContentValuesFromDataFromCategoryInputForm(viewHolder);

        try {

            POIsContract.CategoryEntry.createNewCategory(getActivity(), contentValues);

            Intent intent = new Intent(getActivity(), LGPCAdminActivity.class);
            intent.putExtra("comeFrom", "categories");
            startActivity(intent);
        } catch (android.database.SQLException e) {
            Toast.makeText(getActivity(), getResources().getString(R.string.categoryExists), Toast.LENGTH_LONG).show();
        }
    }

    private ContentValues getContentValuesFromDataFromCategoryInputForm(ViewHolderCategory viewHolder) {
        ContentValues contentValues = new ContentValues();

        String categoryName = viewHolder.categoryName.getText().toString();
        int hideValue = getHideValueFromInputForm(viewHolder.switchButtonHide);
        int fatherID;
        String shownName;

        if (creationType.endsWith("HERE")) {
            fatherID = POISFragment.routeID;
            shownName = POIsContract.CategoryEntry.getShownNameByID(getActivity(), fatherID)
                    + viewHolder.categoryName.getText().toString() + "/";
        } else if (creationType.endsWith("HERENEW")) {
            Bundle extras = getActivity().getIntent().getExtras();
            fatherID = Integer.parseInt(extras.getString("CATEGORY_ID"));
            shownName = POIsContract.CategoryEntry.getShownNameByID(getActivity(), fatherID)
                    + viewHolder.categoryName.getText().toString() + "/";
        } else {
            shownName = getShownNameValueFromInputForm(viewHolder.fatherID);
            fatherID = getFatherIDValueFromInputForm(shownName);
            shownName = shownName + viewHolder.categoryName.getText().toString() + "/";
        }

        contentValues.put(POIsContract.CategoryEntry.COLUMN_NAME, categoryName);
        contentValues.put(POIsContract.CategoryEntry.COLUMN_FATHER_ID, fatherID);
        contentValues.put(POIsContract.CategoryEntry.COLUMN_SHOWN_NAME, shownName);
        contentValues.put(POIsContract.CategoryEntry.COLUMN_HIDE, hideValue);

        return contentValues;
    }

    private ViewHolderCategory setCategoryLayoutSettings(LayoutInflater inflater, ViewGroup container) {
        rootView = inflater.inflate(R.layout.fragment_create_or_update_category, container, false);
        final ViewHolderCategory viewHolder = new ViewHolderCategory(rootView);
        viewHolder.updateCategory.setVisibility(View.GONE);
        viewHolder.createCategory.setVisibility(View.VISIBLE);

        if (creationType.endsWith("HERE")) {
            viewHolder.fatherID.setVisibility(View.GONE);
        } else if (creationType.endsWith("HERENEW")) {
            viewHolder.fatherID.setVisibility(View.GONE);
        } else {
            fillCategorySpinner(viewHolder.fatherID);
        }
        setCancelComeBackBehaviour(viewHolder.cancel);
        return viewHolder;
    }

    /*OTHER UTILITIES*/
    private void fillCategorySpinner(Spinner spinner) {

        List<String> list = new ArrayList<>();
        list.add(getResources().getString(R.string.noRouteText));
        spinnerIDsAndShownNames = new HashMap<>();

        //We get all the categories IDs and ShownNames
        queryCursor = POIsContract.CategoryEntry.getIDsAndShownNamesOfAllCategories(getActivity());

        while (queryCursor.moveToNext()) {
            spinnerIDsAndShownNames.put(queryCursor.getString(1), String.valueOf(queryCursor.getInt(0)));
            list.add(queryCursor.getString(1));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    } //Fill the spinner with all the categories.

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

    private void setCancelComeBackBehaviour(FloatingActionButton cancel) {

        cancel.setOnClickListener(view -> this.getActivity().onBackPressed());
    }

    //These three ViewHolder classes are a kind of containers which contain all the elements related
    //to the creation of one item. The POIs once contains the elements for creating a POI, the Tour
    //once to be able to create a Tour and the same with the Categories once.
    public static class ViewHolderPoi {

        public EditText name;
        public FloatingActionButton cancel;
        EditText visitedPlaceET;
        EditText longitudeET;
        EditText latitudeET;
        EditText altitudeET;
        EditText headingET;
        EditText tiltET;
        EditText rangeET;
        Spinner categoryID;
        FloatingActionButton createPOI;
        FloatingActionButton updatePOI;
        Spinner spinnerAltitudeMode;
        private Switch switchButtonHide;

        ViewHolderPoi(final View rootView) {

            name = (EditText) rootView.findViewById(R.id.name);
            visitedPlaceET = (EditText) rootView.findViewById(R.id.visited_place);
            longitudeET = (EditText) rootView.findViewById(R.id.longitude);
            latitudeET = (EditText) rootView.findViewById(R.id.latitude);
            altitudeET = (EditText) rootView.findViewById(R.id.altitude);
            headingET = (EditText) rootView.findViewById(R.id.heading);
            tiltET = (EditText) rootView.findViewById(R.id.tilt);
            rangeET = (EditText) rootView.findViewById(R.id.range);
            spinnerAltitudeMode = (Spinner) rootView.findViewById(R.id.spinnerAltitude);

            categoryID = (Spinner) rootView.findViewById(R.id.categoryID_spinner);
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

            switchButtonHide = (Switch) rootView.findViewById(R.id.switchButtonHide);
            createPOI = (FloatingActionButton) rootView.findViewById(R.id.create_poi);
            updatePOI = (FloatingActionButton) rootView.findViewById(R.id.update_poi);
            cancel = (FloatingActionButton) rootView.findViewById(R.id.cancel_come_back);
        }
    }

    public static class ViewHolderCategory {

        public FloatingActionButton cancel;
        EditText categoryName;
        Spinner fatherID;
        FloatingActionButton createCategory;
        FloatingActionButton updateCategory;
        private Switch switchButtonHide;

        ViewHolderCategory(View rootView) {

            categoryName = (EditText) rootView.findViewById(R.id.category_name);
            switchButtonHide = (Switch) rootView.findViewById(R.id.switchButtonHide);
            fatherID = (Spinner) rootView.findViewById(R.id.father_spinner);
            createCategory = (FloatingActionButton) rootView.findViewById(R.id.create_category);
            updateCategory = (FloatingActionButton) rootView.findViewById(R.id.update_category);
            cancel = (FloatingActionButton) rootView.findViewById(R.id.cancel_come_back);
        }

    }
}