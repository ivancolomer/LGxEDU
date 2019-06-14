package com.lglab.ivan.lgxeducontroller.activities_new.play.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.utils.Category;

import java.util.List;


public class PlayAdapter extends RecyclerView.Adapter<PlayAdapter.PlayViewHolder> {

    private Context context;
    private List<Category> data;
    private GameAdapter horizontalAdapter;
    private RecyclerView.RecycledViewPool recycledViewPool;

    public PlayAdapter(List<Category> data, Context context) {
        this.data = data;
        this.context = context;
        recycledViewPool = new RecyclerView.RecycledViewPool();

    }

    @NonNull
    @Override
    public PlayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View theView = LayoutInflater.from(context).inflate(R.layout.activity_play_row_new, parent, false);
        return new PlayViewHolder(theView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayViewHolder holder, final int position) {
        holder.textViewCategory.setText(data.get(position).getTitle());
        horizontalAdapter = new GameAdapter(data.get(position).getItems(), context);
        holder.recyclerViewHorizontal.setAdapter(horizontalAdapter);
        holder.recyclerViewHorizontal.setRecycledViewPool(recycledViewPool);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class PlayViewHolder extends RecyclerView.ViewHolder {

        private RecyclerView recyclerViewHorizontal;
        private TextView textViewCategory;

        private LinearLayoutManager horizontalManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);

        public PlayViewHolder(View itemView) {
            super(itemView);

            recyclerViewHorizontal = itemView.findViewById(R.id.play_rv_horizontal);
            recyclerViewHorizontal.setHasFixedSize(true);
            recyclerViewHorizontal.setNestedScrollingEnabled(false);
            recyclerViewHorizontal.setLayoutManager(horizontalManager);
            recyclerViewHorizontal.setItemAnimator(new DefaultItemAnimator());

            textViewCategory = itemView.findViewById(R.id.quiz_category);
        }
    }
}