package com.lglab.ivan.lgxeducontroller.utils;

import android.view.View;
import android.widget.TextView;

import com.lglab.ivan.lgxeducontroller.R;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

public class CategoryViewHolder extends GroupViewHolder {

    private TextView categoryTitle;

    public CategoryViewHolder(View itemView) {
        super(itemView);
        categoryTitle = itemView.findViewById(R.id.list_item_category_name);
    }

    public void setCategoryTitle(ExpandableGroup group) {
        categoryTitle.setText(group.getTitle());
    }
}

