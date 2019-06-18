package com.lglab.ivan.lgxeducontroller.activities_new.manager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.games.Category;
import com.lglab.ivan.lgxeducontroller.games.Game;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import java.util.List;

public class CategoryManagerAdapter extends ExpandableRecyclerViewAdapter<CategoryManagerAdapter.CategoryViewHolder, CategoryManagerAdapter.GameViewHolder> {

    public CategoryManagerAdapter(List<? extends ExpandableGroup> groups) {
        super(groups);
    }

    @Override
    public CategoryViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public GameViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_quiz, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindChildViewHolder(GameViewHolder holder, int flatPosition, ExpandableGroup group,
                                      int childIndex) {
        final Game game = ((Category) group).getItems().get(childIndex);
        holder.onBind(game);

    }

    @Override
    public void onBindGroupViewHolder(CategoryViewHolder holder, int flatPosition,
                                      ExpandableGroup group) {
        holder.setCategoryTitle(group);
    }

    public static class CategoryViewHolder extends GroupViewHolder {

        private TextView categoryTitle;

        public CategoryViewHolder(View itemView) {
            super(itemView);
            categoryTitle = itemView.findViewById(R.id.list_item_category_name);
        }

        public void setCategoryTitle(ExpandableGroup group) {
            categoryTitle.setText(group.getTitle());
        }
    }

    public static class GameViewHolder extends ChildViewHolder {

        public Game game;
        private TextView quizName;
        private ImageView playButton;
        private ImageView shareButton;

        public GameViewHolder(View itemView) {
            super(itemView);
            quizName = itemView.findViewById(R.id.list_item_quiz_name);
            //shareButton = itemView.findViewById(R.id.list_item_category_share);
            //playButton = itemView.findViewById(R.id.list_item_category_arrow);
        }

        public void onBind(Game game) {
            quizName.setText(game.getName());
            this.game = game;
            //this.itemView.setOnClickListener(arg0 -> startQuiz((GoogleDriveActivity) itemView.getContext()));
            //this.playButton.setOnClickListener(arg0 -> startQuiz((GoogleDriveActivity) itemView.getContext()));
            //this.shareButton.setOnClickListener(arg0 -> shareQuiz((GoogleDriveActivity) itemView.getContext()));

        }

        /*private void startQuiz(GoogleDriveActivity activity) {
            QuizManager.getInstance().startQuiz(quiz);

            Intent intent = new Intent(activity, QuizActivity.class);
            intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); //Adds the FLAG_ACTIVITY_NO_HISTORY flag
            activity.startActivity(intent);
        }

        private void shareQuiz(GoogleDriveActivity activity) {
            activity.exportQuiz(quiz);
        }*/
    }
}