package com.lglab.ivan.lgxeducontroller.legacy;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.activities.main.MainActivity;


public class AdminFragment extends Fragment {

    public AdminFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_admin, container, false);
        final ViewHolder viewHolder = new ViewHolder(rootView);

        managementOfPoisToursAndCategories(viewHolder);
        setLogOutButtonBehaviour(viewHolder);
        setNewItemHereButtonBehaviour(viewHolder);
        setNewItemButtonBehaviour(viewHolder);

        return rootView;
    }

    private void managementOfPoisToursAndCategories(final ViewHolder viewHolder) {
        viewHolder.poisManagement.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.management_button_container, new POISFragment(), "ADMIN/POIS").commit();
            if (viewHolder.createPOI.getVisibility() == View.GONE) {
                viewHolder.createTour.setVisibility(View.GONE);
                viewHolder.createCategory.setVisibility(View.GONE);
                viewHolder.createPOI.setVisibility(View.VISIBLE);
                viewHolder.createTourhere.setVisibility(View.GONE);
                viewHolder.createCategoryhere.setVisibility(View.GONE);
                viewHolder.createPOIhere.setVisibility(View.GONE);
            }
        });

        viewHolder.toursManagement.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.management_button_container, new POISFragment(), "ADMIN/TOURS").commit();
            if (viewHolder.createTour.getVisibility() == View.GONE) {
                viewHolder.createPOI.setVisibility(View.GONE);
                viewHolder.createCategory.setVisibility(View.GONE);
                viewHolder.createTour.setVisibility(View.VISIBLE);
                viewHolder.createPOIhere.setVisibility(View.GONE);
                viewHolder.createCategoryhere.setVisibility(View.GONE);
                viewHolder.createTourhere.setVisibility(View.GONE);
            }
        });

        viewHolder.categoriesManagement.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.management_button_container, new POISFragment(), "ADMIN/CATEGORIES").commit();
            if (viewHolder.createCategory.getVisibility() == View.GONE) {
                viewHolder.createPOI.setVisibility(View.GONE);
                viewHolder.createTour.setVisibility(View.GONE);
                viewHolder.createCategory.setVisibility(View.VISIBLE);
                viewHolder.createPOIhere.setVisibility(View.GONE);
                viewHolder.createTourhere.setVisibility(View.GONE);
                viewHolder.createCategoryhere.setVisibility(View.GONE);
            }
        });
    }

    private void setLogOutButtonBehaviour(ViewHolder viewHolder) {
        viewHolder.logout.setOnClickListener(v -> {
            Intent main = new Intent(getActivity(), MainActivity.class);
            startActivity(main);
        });
    }

    private void setNewItemHereButtonBehaviour(ViewHolder viewHolder) {

        viewHolder.createCategoryhere.setOnClickListener(v -> {
            Intent createPoiIntent = new Intent(getActivity(), CreateItemActivity.class);
            createPoiIntent.putExtra("CREATION_TYPE", "CATEGORY/HERE");
            startActivity(createPoiIntent);
        });

        viewHolder.createPOIhere.setOnClickListener(v -> {
            Intent createPoiIntent = new Intent(getActivity(), CreateItemActivity.class);
            createPoiIntent.putExtra("CREATION_TYPE", "POI/HERE");
            startActivity(createPoiIntent);
        });

        viewHolder.createTourhere.setOnClickListener(v -> {
            Intent createPoiIntent = new Intent(getActivity(), CreateItemActivity.class);
            createPoiIntent.putExtra("CREATION_TYPE", "TOUR/HERE");
            startActivity(createPoiIntent);
        });
    }

    private void setNewItemButtonBehaviour(ViewHolder viewHolder) {

        viewHolder.createCategory.setOnClickListener(v -> {
            Intent createCategoryIntent = new Intent(getActivity(), CreateItemActivity.class);
            createCategoryIntent.putExtra("CREATION_TYPE", "CATEGORY");
            startActivity(createCategoryIntent);
        });

        viewHolder.createPOI.setOnClickListener(v -> {
            Intent createPoiIntent = new Intent(getActivity(), CreateItemActivity.class);
            createPoiIntent.putExtra("CREATION_TYPE", "POI");
            startActivity(createPoiIntent);
        });

        viewHolder.createTour.setOnClickListener(v -> {
            Intent createTourIntent = new Intent(getActivity(), CreateItemActivity.class);
            createTourIntent.putExtra("CREATION_TYPE", "TOUR");
            startActivity(createTourIntent);
        });
    }

    public static class ViewHolder {
        Button createPOI;
        Button createCategory;
        Button createTour;
        Button createPOIhere;
        Button createCategoryhere;
        Button createTourhere;

        Button poisManagement;
        Button toursManagement;
        Button categoriesManagement;

        Button logout;

        ViewHolder(View rootView) {

            poisManagement = rootView.findViewById(R.id.pois_management);
            toursManagement = rootView.findViewById(R.id.tours_management);
            categoriesManagement = rootView.findViewById(R.id.categories_management);
            createPOI = rootView.findViewById(R.id.new_poi);
            createCategory = rootView.findViewById(R.id.new_category);
            createTour = rootView.findViewById(R.id.new_tour);
            createPOIhere = rootView.findViewById(R.id.new_poi_here);
            createCategoryhere = rootView.findViewById(R.id.new_category_here);
            createTourhere = rootView.findViewById(R.id.new_tour_here);
            logout = rootView.findViewById(R.id.admin_logout);
        }
    }
}
