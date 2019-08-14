package com.lglab.ivan.lgxeducontroller.legacy;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import androidx.appcompat.widget.AppCompatImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.MenuCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.legacy.beans.TourPOI;
import com.lglab.ivan.lgxeducontroller.legacy.data.POIsContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*This class has the same objective that CreateItemFragment once but it is called when one user
wants to update one item already created. The pages are structurally equals with the fragment mentioned before,
however there is a differences: all fields are filled by the values of the item to update.
*/
public class UpdateItemFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener {

    private static final String POI_IDselection = POIsContract.POIEntry._ID + " =?";
    private static final String TOUR_IDselection = POIsContract.TourEntry._ID + " =?";
    private static final String Category_IDselection = POIsContract.CategoryEntry._ID + " =?";
    private static View rootView;
    private static String itemSelectedID;

    private static Map<String, String> spinnerIDsAndShownNames, categoriesOfPOIsSpinner;
    private static List<TourPOI> tourPois, newTourPOIS;
    private double latitude;
    private double longitude;
    private String poiName;
    private GoogleMap map;
    private String updateType, newShownName;
    private Cursor queryCursor;
    private ArrayAdapter<String> adapter;

    private ViewHolderPoi viewHolderPoi;
    private static ViewHolderTour viewHolderTour;
    private ViewHolderCategory viewHolderCategory;
    private String oldItemShownName;

    public UpdateItemFragment() {
        tourPois = new ArrayList<>();
        newTourPOIS = new ArrayList<>();
    }

    public static void deleteButtonTreatment(View view, final TourPOI tourPoi) {
        final AppCompatImageView delete = view.findViewById(R.id.delete);
        delete.setOnClickListener(v -> {

            if (tourPois.contains(tourPoi)) {
                tourPois.remove(tourPoi);
            } else newTourPOIS.remove(tourPoi);

            String id = String.valueOf(tourPoi.getPoiID());
            FragmentActivity activity = (FragmentActivity) rootView.getContext();
            POIsContract.TourPOIsEntry.deleteByTourIdAndPoiID(activity, itemSelectedID, id);

            TourPOIsAdapter.setType("updating");
            TourPOIsAdapter adapter = new TourPOIsAdapter(activity, tourPois);
            viewHolderTour.addedPois.setAdapter(adapter);
        });
    }

    //when, from POIsFragment, we are updating a TOUR and we want to ADD another POI
    static void setPOItoTourPOIsList(TourPOI tourPOI) {
        String global_interval = viewHolderTour.global_interval.getText().toString();
        if (!tourPois.contains(tourPOI)) {
            TourPOIsAdapter.setType("updating");

            tourPOI.setDuration(Integer.parseInt(global_interval));
            tourPOI.setOrder(tourPois.size() + 1);

            newTourPOIS.add(tourPOI);
            tourPois.add(tourPOI);

            FragmentActivity activity = (FragmentActivity) rootView.getContext();
            TourPOIsAdapter adapter = new TourPOIsAdapter(activity, tourPois);
            viewHolderTour.addedPois.setAdapter(adapter);
        } else {
            Toast.makeText(rootView.getContext(), "The POI " + tourPOI.getPoiName() + " already exists inside this Tour.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        // Add a marker
        LatLng latlong = new LatLng(latitude, longitude);
        map.addMarker(new MarkerOptions().position(latlong).title(poiName).draggable(true));
        map.setBuildingsEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlong, 17));

        map.setOnMarkerDragListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle extras = getActivity().getIntent().getExtras();
        rootView = null;
        setHasOptionsMenu(true);
        if (extras != null) {
            this.updateType = extras.getString("UPDATE_TYPE");
            itemSelectedID = extras.getString("ITEM_ID");
        }

        switch (updateType) {
            case "POI": {
                getActivity().setTitle(getResources().getString(R.string.update_poi));
                viewHolderPoi = setPOILayoutSettings(inflater, container);
                updatePOI(viewHolderPoi);

                SupportMapFragment fragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
                fragment.getMapAsync(this);

                latitude = Double.parseDouble(viewHolderPoi.latitudeET.getText().toString());
                longitude = Double.parseDouble(viewHolderPoi.longitudeET.getText().toString());
                poiName = viewHolderPoi.nameET.getText().toString();


                break;
            }
            case "TOUR":
                getActivity().setTitle(getResources().getString(R.string.update_tour));
                viewHolderTour = setTOURLayoutSettings(inflater, container);
                updateTOUR(viewHolderTour);
                break;
            default: {//CATEGORY
                getActivity().setTitle(getResources().getString(R.string.update_category));
                viewHolderCategory = setCategoryLayoutSettings(inflater, container);
                updateCategory(viewHolderCategory);
                break;
            }
        }


        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem itemSettings = menu.findItem(R.id.action_settings);
        if(itemSettings != null)
            itemSettings.setVisible(false);

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

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.save_poi) {
            if(viewHolderPoi != null)
                updatePOIModifications(viewHolderPoi);
            else if(viewHolderCategory != null)
                updateCategoryModifications(viewHolderCategory, oldItemShownName);
            else if(viewHolderTour != null) {
                updateTourModifications();
                updateTourPOIsModifications();
            }
        } else if(id == R.id.close_poi) {
            getActivity().onBackPressed();
        }

        return super.onOptionsItemSelected(item);
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

    /*POI TREATMENT*/
    private void updatePOI(ViewHolderPoi viewHolder) {
        Cursor query = getAllSelectedItemData(POIsContract.POIEntry.CONTENT_URI, POI_IDselection);
        fillPOIsCategoriesSpinner(viewHolder.categoryID);
        setDataToPOIsLayout(query, viewHolder);
    }

    private ViewHolderPoi setPOILayoutSettings(LayoutInflater inflater, ViewGroup container) {
        rootView = inflater.inflate(R.layout.fragment_create_or_update_poi, container, false);
        return new ViewHolderPoi(rootView);
    }

    private void fillPOIsCategoriesSpinner(Spinner spinner) {
        List<String> list = new ArrayList<>();
        list.add("NO ROUTE");
        spinnerIDsAndShownNames = new HashMap<>();
        categoriesOfPOIsSpinner = new HashMap<>();

        queryCursor = POIsContract.CategoryEntry.getIDsAndShownNamesOfAllCategories(getActivity());

        while (queryCursor.moveToNext()) {
            categoriesOfPOIsSpinner.put(String.valueOf(queryCursor.getInt(0)), queryCursor.getString(1));
            spinnerIDsAndShownNames.put(queryCursor.getString(1), String.valueOf(queryCursor.getInt(0)));
            list.add(queryCursor.getString(1));
        }

        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setDataToPOIsLayout(Cursor query, ViewHolderPoi viewHolder) {
        if (query.moveToFirst()) {
            viewHolder.nameET.setText(query.getString(viewHolder.NAME));
            viewHolder.visitedPlaceET.setText(query.getString(viewHolder.VISITED_PLACE_NAME));
            viewHolder.longitudeET.setText(String.valueOf(query.getFloat(viewHolder.LONGITUDE)));
            viewHolder.latitudeET.setText(String.valueOf(query.getFloat(viewHolder.LATITUDE)));
            viewHolder.altitudeET.setText(String.valueOf(query.getFloat(viewHolder.ALTITUDE)));
            viewHolder.headingET.setText(String.valueOf(query.getFloat(viewHolder.HEADING)));
            viewHolder.tiltET.setText(String.valueOf(query.getFloat(viewHolder.TILT)));
            viewHolder.rangeET.setText(String.valueOf(query.getFloat(viewHolder.RANGE)));
            viewHolder.spinnerAltitudeMode.setSelection(((ArrayAdapter) viewHolder.spinnerAltitudeMode.getAdapter()).getPosition(query.getString(viewHolder.ALTITUDE_MODE)));
            viewHolder.categoryID.setSelection(adapter.getPosition(getShownNameByCategoryID(query, viewHolder, null, "POI")));

            if (query.getString(viewHolder.HIDE).equals("0")) {
                viewHolder.switchButtonHide.setChecked(true);
            } else {
                viewHolder.switchButtonHide.setChecked(false);
            }
        }
    }

    private void updatePOIModifications(final ViewHolderPoi viewHolder) {

        ContentValues contentValues = new ContentValues();

        int categoryID;

        String visitedPlace = viewHolder.visitedPlaceET.getText().toString();
        String completeName = viewHolder.nameET.getText().toString();
        float longitude = Float.parseFloat(viewHolder.longitudeET.getText().toString());
        float latitude = Float.parseFloat(viewHolder.latitudeET.getText().toString());
        float altitude = Float.parseFloat(viewHolder.altitudeET.getText().toString());
        float heading = Float.parseFloat(viewHolder.headingET.getText().toString());
        float tilt = Float.parseFloat(viewHolder.tiltET.getText().toString());
        float range = Float.parseFloat(viewHolder.rangeET.getText().toString());
        String altitudeMode = viewHolder.spinnerAltitudeMode.getSelectedItem().toString();

        int hide = getHideValueFromInputForm(viewHolder.switchButtonHide);

        String shownName = getShownNameValueFromInputForm(viewHolder.categoryID);
        categoryID = getFatherIDValueFromInputForm(shownName);

        contentValues.put(POIsContract.POIEntry.COLUMN_COMPLETE_NAME, completeName);
        contentValues.put(POIsContract.POIEntry.COLUMN_VISITED_PLACE_NAME, visitedPlace);
        contentValues.put(POIsContract.POIEntry.COLUMN_COMPLETE_NAME, completeName);
        contentValues.put(POIsContract.POIEntry.COLUMN_LONGITUDE, longitude);
        contentValues.put(POIsContract.POIEntry.COLUMN_LATITUDE, latitude);
        contentValues.put(POIsContract.POIEntry.COLUMN_ALTITUDE, altitude);
        contentValues.put(POIsContract.POIEntry.COLUMN_HEADING, heading);
        contentValues.put(POIsContract.POIEntry.COLUMN_TILT, tilt);
        contentValues.put(POIsContract.POIEntry.COLUMN_RANGE, range);
        contentValues.put(POIsContract.POIEntry.COLUMN_ALTITUDE_MODE, altitudeMode);
        contentValues.put(POIsContract.POIEntry.COLUMN_HIDE, hide);
        contentValues.put(POIsContract.POIEntry.COLUMN_CATEGORY_ID, categoryID);

        int updatedRows = POIsContract.POIEntry.updateByID(getActivity(), contentValues, itemSelectedID);

        if (updatedRows > 0) {
            final Activity activity = getActivity();
            if(activity != null)
                activity.runOnUiThread(activity::onBackPressed);
        } else {
            Toast.makeText(getActivity(), "ERROR UPDATING", Toast.LENGTH_SHORT).show();
            final Activity activity = getActivity();
            if(activity != null)
                activity.runOnUiThread(activity::onBackPressed);
        }
    }

    private String getShownNameByCategoryID(Cursor query, ViewHolderPoi viewHolderPoi, ViewHolderTour viewHolderTour, String type) {
        if (type.equals("POI")) {
            return categoriesOfPOIsSpinner.get(String.valueOf(query.getInt(viewHolderPoi.CATEGORY_ID)));
        } else {
            return categoriesOfPOIsSpinner.get(String.valueOf(query.getInt(viewHolderTour.CATEGORY)));
        }
    }

    /*CATEGORY TREATMENT*/
    private void updateCategory(ViewHolderCategory viewHolder) {
        Cursor query = getAllSelectedItemData(POIsContract.CategoryEntry.CONTENT_URI, Category_IDselection);
        oldItemShownName = fillCategoriesSpinner(query, viewHolder);
        setDataToLayout(query, viewHolder);
    }

    private ViewHolderCategory setCategoryLayoutSettings(LayoutInflater inflater, ViewGroup container) {
        rootView = inflater.inflate(R.layout.fragment_create_or_update_category, container, false);
        viewHolderCategory = new ViewHolderCategory(rootView);
        return viewHolderCategory;
    }

    private String fillCategoriesSpinner(Cursor query, ViewHolderCategory viewHolder) {

        query.moveToFirst();
        String itemShownName = query.getString(viewHolder.SHOWN_NAME);

        int id;
        String shownName;
        List<String> list = new ArrayList<>();
        list.add("NO ROUTE");
        spinnerIDsAndShownNames = new HashMap<>();

        //We get all the categories IDs and ShownNames
        queryCursor = getActivity().getContentResolver().query(POIsContract.CategoryEntry.CONTENT_URI,
                new String[]{POIsContract.CategoryEntry._ID, POIsContract.CategoryEntry.COLUMN_SHOWN_NAME}, null, null, null);

        if (queryCursor != null) {
            while (queryCursor.moveToNext()) {
                id = queryCursor.getInt(0);
                shownName = queryCursor.getString(1);

                if (!shownName.contains(itemShownName)) {
                    spinnerIDsAndShownNames.put(shownName, String.valueOf(id));
                    list.add(shownName);
                }
            }
        }
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        viewHolder.fatherID.setAdapter(adapter);

        return itemShownName;
    }

    private void updateCategoryModifications(final UpdateItemFragment.ViewHolderCategory viewHolder, final String oldItemShownName) {

        //If the user wants to put some category inside one of its sons, he will not be able to,
        //he will have to delete that category and insert it again in the correct category.
        //For example: the order is: ESP - CAT - LLEIDA - ... If the user wants
        //to put ESP inside CAT or LLEIDA (both are sons of ESP) he will not be able to.

        ContentValues contentValues = new ContentValues();
        final String categoryName = viewHolder.categoryName.getText().toString();
        final int hideValue = getHideValueFromInputForm(viewHolder.switchButtonHide);
        String shownNameSelected = getShownNameValueFromInputForm(viewHolder.fatherID);
        final int fatherID = getFatherIDValueFromInputForm(shownNameSelected);
        final String correctShownName = shownNameSelected + viewHolder.categoryName.getText().toString() + "/";
        newShownName = correctShownName;

        contentValues.put(POIsContract.CategoryEntry.COLUMN_NAME, categoryName);
        contentValues.put(POIsContract.CategoryEntry.COLUMN_FATHER_ID, fatherID);
        contentValues.put(POIsContract.CategoryEntry.COLUMN_SHOWN_NAME, correctShownName);
        contentValues.put(POIsContract.CategoryEntry.COLUMN_HIDE, hideValue);

        int updatedRows = POIsContract.CategoryEntry.updateByID(getActivity(), contentValues, itemSelectedID);
        if (updatedRows <= 0) {
            Toast.makeText(getActivity(), "ERROR UPDATING", Toast.LENGTH_SHORT).show();
        }
        updateSubCategoriesShownName(oldItemShownName);
    }

    private void updateSubCategoriesShownName(String oldItemShownName) {

        String whereClause = POIsContract.CategoryEntry.COLUMN_SHOWN_NAME + " LIKE '" + oldItemShownName + "%'";

        Cursor cursor = getActivity().getContentResolver().query(POIsContract.CategoryEntry.CONTENT_URI,
                new String[]{POIsContract.CategoryEntry._ID, POIsContract.CategoryEntry.COLUMN_SHOWN_NAME},
                whereClause, null, null);

        ContentValues updatedShownNameValues;
        String currentShownName, finalShownName, itemTreatedID;

        if (cursor != null) {
            while (cursor.moveToNext()) {

                itemTreatedID = String.valueOf(cursor.getInt(0));
                currentShownName = cursor.getString(1);
                if (!currentShownName.equals(oldItemShownName)) {
                    //remove the bad shownName
                    String currentWithoutOldPartition = currentShownName.substring(oldItemShownName.length());
                    //write the good one
                    finalShownName = newShownName + currentWithoutOldPartition;

                    updatedShownNameValues = new ContentValues();
                    updatedShownNameValues.put(POIsContract.CategoryEntry.COLUMN_SHOWN_NAME, finalShownName);
                    getActivity().getContentResolver().update(POIsContract.CategoryEntry.CONTENT_URI, updatedShownNameValues,
                            Category_IDselection, new String[]{itemTreatedID});
                }
            }
            cursor.close();
        }

        /*Intent intent = new Intent(getActivity(), LGPCAdminActivity.class);
        intent.putExtra("comeFrom", "treeView");
        startActivity(intent);*/
        final Activity activity = getActivity();
        if(activity != null)
            activity.runOnUiThread(activity::onBackPressed);
    }

    private void setDataToLayout(Cursor query, UpdateItemFragment.ViewHolderCategory viewHolder) {
        String fatherShownName = POIsContract.CategoryEntry.getShownNameByID(getActivity(), query.getInt(viewHolder.FATHER_ID));
        if (query.moveToFirst()) {
            viewHolder.categoryName.setText(query.getString(viewHolder.NAME));
            viewHolder.fatherID.setSelection(adapter.getPosition(fatherShownName));
            if (query.getString(viewHolder.HIDE).equals("0")) {
                viewHolder.switchButtonHide.setChecked(true);
            } else {
                viewHolder.switchButtonHide.setChecked(false);
            }
        }
    }

    /*TOUR TREATMENT*/
    private void updateTOUR(ViewHolderTour viewHolder) {
        Cursor query = getAllSelectedItemData(POIsContract.TourEntry.CONTENT_URI, TOUR_IDselection);
        fillPOIsCategoriesSpinner(viewHolder.categoryID);
        setDataToTourLayout(query, viewHolder);
    }

    private void updateTourModifications() {
        ContentValues contentValues = getContentValuesFromDataFromTourInputForm(viewHolderTour);

        POIsContract.TourEntry.updateByID(getActivity(), contentValues, itemSelectedID);
    }

    private void updateTourPOIsModifications() {
        ContentValues contentValues = new ContentValues();

        for (TourPOI tourPoi : tourPois) {
            contentValues.clear();

            contentValues.put(POIsContract.TourPOIsEntry.COLUMN_POI_ID, tourPoi.getPoiID());
            contentValues.put(POIsContract.TourPOIsEntry.COLUMN_TOUR_ID, itemSelectedID);
            contentValues.put(POIsContract.TourPOIsEntry.COLUMN_POI_ORDER, tourPoi.getOrder());
            contentValues.put(POIsContract.TourPOIsEntry.COLUMN_POI_DURATION, tourPoi.getDuration());

            POIsContract.TourPOIsEntry.updateByTourIdAndPoiID(getActivity(), contentValues, itemSelectedID, String.valueOf(tourPoi.getPoiID()));
        }

        for (TourPOI newTourPoi : newTourPOIS) {
            contentValues.clear();
            contentValues.put(POIsContract.TourPOIsEntry.COLUMN_POI_ID, newTourPoi.getPoiID());
            contentValues.put(POIsContract.TourPOIsEntry.COLUMN_TOUR_ID, itemSelectedID);
            contentValues.put(POIsContract.TourPOIsEntry.COLUMN_POI_ORDER, newTourPoi.getOrder());
            contentValues.put(POIsContract.TourPOIsEntry.COLUMN_POI_DURATION, newTourPoi.getDuration());
            POIsContract.TourPOIsEntry.createNewTourPOI(getActivity(), contentValues);
        }

        final Activity activity = getActivity();
        if(activity != null)
            activity.runOnUiThread(activity::onBackPressed);
    }

    private void setDataToTourLayout(Cursor query, ViewHolderTour viewHolder) {
        if (query.moveToFirst()) {
            viewHolder.tourName.setText(query.getString(viewHolder.NAME));
            viewHolder.categoryID.setSelection(adapter.getPosition(getShownNameByCategoryID(query, null, viewHolder, "TOUR")));
            if (query.getString(viewHolder.HIDE).equals("0")) {
                viewHolder.switchButtonHide.setChecked(true);
            } else {
                viewHolder.switchButtonHide.setChecked(false);
            }
            int global_interval = query.getInt(viewHolder.INTERVAL);
            viewHolder.global_interval.setText(String.valueOf(global_interval));
            setListOfPOIs();
            setDataToTourPOIsLayout(global_interval);
        }
    }

    private void setListOfPOIs() {
        POISFragment fragment = new POISFragment();
        Bundle args = new Bundle();
        args.putString("createORupdate", "update");
        args.putString("EDITABLE", "ADMIN/TOUR_POIS");
        fragment.setArguments(args);
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_tour_pois, fragment).commit();
    }

    private void setDataToTourPOIsLayout(int globalInterval) {
        FragmentActivity fragmentActivity = getActivity();
        Cursor cursor = POIsContract.TourPOIsEntry.getPOIsByTourID(itemSelectedID);

        while (cursor.moveToNext()) {
            TourPOI newTourPoi = new TourPOI();
            newTourPoi.setPoiName(cursor.getString(1));
            newTourPoi.setPoiID(cursor.getInt(0));
            newTourPoi.setDuration(cursor.getInt(2));
            newTourPoi.setOrder(cursor.getPosition() + 1);
            tourPois.add(newTourPoi);
        }

        TourPOIsAdapter.setGlobalInterval(globalInterval);
        TourPOIsAdapter.setType("updating");
        TourPOIsAdapter adapter = new TourPOIsAdapter(fragmentActivity, tourPois);
        viewHolderTour.addedPois.setAdapter(adapter);
    }

    private ViewHolderTour setTOURLayoutSettings(LayoutInflater inflater, ViewGroup container) {
        rootView = inflater.inflate(R.layout.fragment_create_or_update_tour, container, false);
        viewHolderTour = new ViewHolderTour(rootView);
        return viewHolderTour;
    }

    private ContentValues getContentValuesFromDataFromTourInputForm(ViewHolderTour viewHolder) {


        String name = viewHolder.tourName.getText().toString();
        int hide = getHideValueFromInputForm(viewHolder.switchButtonHide);
        String shownName = getShownNameValueFromInputForm(viewHolder.categoryID);
        int categoryID = getFatherIDValueFromInputForm(shownName);
        String global_interval = viewHolderTour.global_interval.getText().toString();

        ContentValues contentValues = new ContentValues();

        contentValues.put(POIsContract.TourEntry.COLUMN_NAME, name);
        contentValues.put(POIsContract.TourEntry.COLUMN_HIDE, hide);
        contentValues.put(POIsContract.TourEntry.COLUMN_CATEGORY_ID, categoryID);
        contentValues.put(POIsContract.TourEntry.COLUMN_INTERVAL, Integer.parseInt(global_interval));

        return contentValues;
    }

    /*OTHER UTILITIES*/
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

    private Cursor getAllSelectedItemData(Uri uri, String selection) {
        return getActivity().getContentResolver().query(uri,
                null, selection, new String[]{itemSelectedID}, null);
    }

    public static class ViewHolderPoi {
        int NAME = 1;
        int VISITED_PLACE_NAME = 2;
        int LONGITUDE = 3;
        int LATITUDE = 4;
        int ALTITUDE = 5;
        int HEADING = 6;
        int TILT = 7;
        int RANGE = 8;
        int ALTITUDE_MODE = 9;
        int HIDE = 10;
        int CATEGORY_ID = 11;
        EditText nameET;
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

        ViewHolderPoi(View rootView) {

            nameET = rootView.findViewById(R.id.name);
            visitedPlaceET = rootView.findViewById(R.id.visited_place);
            longitudeET = rootView.findViewById(R.id.longitude);
            latitudeET = rootView.findViewById(R.id.latitude);
            altitudeET = rootView.findViewById(R.id.altitude);
            headingET = rootView.findViewById(R.id.heading);
            tiltET = rootView.findViewById(R.id.tilt);
            rangeET = rootView.findViewById(R.id.range);
            spinnerAltitudeMode = rootView.findViewById(R.id.spinnerAltitude);
            switchButtonHide = rootView.findViewById(R.id.switchButtonHide);
            categoryID = rootView.findViewById(R.id.categoryID_spinner);
        }
    }

    public static class ViewHolderTour {

        public AppCompatImageView up;
        int NAME = 1;
        int CATEGORY = 2;
        int HIDE = 3;
        int INTERVAL = 4;
        EditText tourName;
        Spinner categoryID;
        ListView addedPois;
        AppCompatImageView down;
        EditText global_interval;
        private Switch switchButtonHide;

        ViewHolderTour(View rootView) {
            tourName = rootView.findViewById(R.id.tour_name);
            switchButtonHide = rootView.findViewById(R.id.switchButtonHide);
            categoryID = rootView.findViewById(R.id.categoryID_spinner);
            addedPois = rootView.findViewById(R.id.tour_pois_listview);
            up = rootView.findViewById(R.id.move_up);
            down = rootView.findViewById(R.id.move_down);
            global_interval = rootView.findViewById(R.id.pois_interval);
        }
    }

    public static class ViewHolderCategory {
        EditText categoryName;
        Spinner fatherID;
        private int NAME = 1;
        private int FATHER_ID = 2;
        private int SHOWN_NAME = 3;
        private int HIDE = 4;
        private Switch switchButtonHide;

        ViewHolderCategory(View rootView) {
            categoryName = rootView.findViewById(R.id.category_name);
            switchButtonHide = rootView.findViewById(R.id.switchButtonHide);
            fatherID = rootView.findViewById(R.id.father_spinner);
        }
    }


}