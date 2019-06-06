package com.lglab.ivan.lgxeducontroller.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.games.quiz.Quiz;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class CategoryAdapter extends ExpandableRecyclerViewAdapter<CategoryViewHolder, QuizViewHolder> {

    public CategoryAdapter(List<? extends ExpandableGroup> groups) {
        super(groups);
    }

    @Override
    public CategoryViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public QuizViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_quiz, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindChildViewHolder(QuizViewHolder holder, int flatPosition, ExpandableGroup group,
                                      int childIndex) {
        final Quiz quiz = ((Category) group).getItems().get(childIndex);
        holder.onBind(quiz);

    }

    @Override
    public void onBindGroupViewHolder(CategoryViewHolder holder, int flatPosition,
                                      ExpandableGroup group) {
        holder.setCategoryTitle(group);
    }
}
