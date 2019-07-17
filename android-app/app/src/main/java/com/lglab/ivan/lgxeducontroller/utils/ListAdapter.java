package com.lglab.ivan.lgxeducontroller.utils;

import android.content.ClipData;
import android.content.res.ColorStateList;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.trivia.adapters.DynamicSquareFrameLayout;
import com.lglab.ivan.lgxeducontroller.interfaces.IDraggableListener;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder>
        implements View.OnTouchListener {

    private List<Integer> list;
    private IDraggableListener listener;

    public ListAdapter(List<Integer> list, IDraggableListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(
                parent.getContext()).inflate(R.layout.multiplayer_player_layout, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        AppCompatTextView text = ((AppCompatTextView)holder.frameLayout.getChildAt(0));
        text.setText(GameManager.getInstance().getPlayerNames()[list.get(position)]);
        text.setSupportBackgroundTintList(ColorStateList.valueOf(holder.itemView.getResources().getColor(colorList[list.get(position)%4])));
        holder.frameLayout.setTag(position);
        holder.frameLayout.setOnTouchListener(this);
        holder.frameLayout.setOnDragListener(new DragListener(listener));
    }

    private static int[] colorList = new int[] {
        R.color.indicator_1, R.color.indicator_2, R.color.indicator_3, R.color.indicator_4
    };


    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v) {
                    @Override
                    public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint) {
                        shadowSize.set(v.getWidth(), v.getHeight());
                        shadowTouchPoint.set((int) event.getX(), (int) event.getY());
                    }
                };
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    v.startDragAndDrop(data, shadowBuilder, v, 0);
                } else {
                    v.startDrag(data, shadowBuilder, v, 0);
                }
                return true;
        }
        return false;
    }

    List<Integer> getList() {
        return list;
    }

    void updateList(List<Integer> list) {
        this.list = list;
    }

    public DragListener getDragInstance() {
        if (listener != null) {
            return new DragListener(listener);
        } else {
            Log.e("ListAdapter", "Listener wasn't initialized!");
            return null;
        }
    }

    class ListViewHolder extends RecyclerView.ViewHolder {
        DynamicSquareFrameLayout frameLayout;

        ListViewHolder(View itemView) {
            super(itemView);
            frameLayout = itemView.findViewById(R.id.player_circle);
        }
    }
}
