package com.lglab.ivan.lgxeducontroller.utils;

import android.content.ClipData;
import android.content.res.ColorStateList;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
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
    public int answerId;
    private ViewGroup viewGroup;
    public boolean isDisabled = false;

    public ListAdapter(List<Integer> list, IDraggableListener listener, int answerId, ViewGroup viewGroup) {
        this.list = list;
        this.listener = listener;
        this.answerId = answerId;
        this.viewGroup = viewGroup;
        enableBorders(false);
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
        if(isDisabled)
        {
            Log.d("ONTOUCH", "disabled");
            return false;
        }


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
                enableBorders(true);
                return true;
        }
        return false;
    }

    public void enableBorders(boolean visible) {
        setBorders(viewGroup.findViewById(R.id.question_0_rv), visible);
        setBorders(viewGroup.findViewById(R.id.question_1_rv), visible);
        setBorders(viewGroup.findViewById(R.id.question_2_rv), visible);
        setBorders(viewGroup.findViewById(R.id.question_3_rv), visible);
        setBorders(viewGroup.findViewById(R.id.question_4_rv), visible);
    }

    private static void setBorders(View v, boolean visible) {
        if(visible) {
            GradientDrawable border = new GradientDrawable();
            border.setColor(0x00FFFFFF); //transparent
            border.setStroke(2, 0xFFF44336); //red border with full opacity
            v.setBackground(border);
        }
        else {
            GradientDrawable border = new GradientDrawable();
            border.setColor(0x00FFFFFF); //transparent
            border.setStroke(2, ContextCompat.getColor(v.getContext(), R.color.whiteGrey));
            v.setBackground(border);
        }
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
