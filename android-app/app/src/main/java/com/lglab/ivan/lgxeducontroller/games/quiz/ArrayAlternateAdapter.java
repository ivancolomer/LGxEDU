package com.lglab.ivan.lgxeducontroller.games.quiz;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

public class ArrayAlternateAdapter<T> extends ArrayAdapter<T> {
    public ArrayAlternateAdapter(@NonNull Context context, int resource, @NonNull List<T> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        if (position % 2 == 1) {
            view.setBackgroundColor(Color.argb(127, 240, 240, 240));
        } else {
            view.setBackgroundColor(Color.TRANSPARENT);
        }

        return view;
    }
}
