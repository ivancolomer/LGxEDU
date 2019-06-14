package com.lglab.ivan.lgxeducontroller.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.games.Game;
import com.lglab.ivan.lgxeducontroller.games.Question;
import com.lglab.ivan.lgxeducontroller.games.trivia.Trivia;
import com.lglab.ivan.lgxeducontroller.games.trivia.TriviaQuestion;
import com.lglab.ivan.lgxeducontroller.legacy.data.POIsProvider;
import com.lglab.ivan.lgxeducontroller.legacy.utils.CustomAndroidTreeView;
import com.lglab.ivan.lgxeducontroller.utils.Category;
import com.lglab.ivan.lgxeducontroller.utils.TreeQuizHolder;
import com.unnamed.b.atv.model.TreeNode;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ManageGamesFragment extends Fragment {

    private static final String TAG = ManageGamesFragment.class.getSimpleName();
    private CustomAndroidTreeView tView;

    public static ManageGamesFragment newInstance() {
        return new ManageGamesFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.new_fragment_pois, null, false);
        ViewGroup containerView = (ViewGroup) rootView.findViewById(R.id.container);

        TreeNode root = TreeNode.root();


        TreeNode categoriesRoot = new TreeNode(new TreeQuizHolder.IconTreeItem(R.drawable.ic_home_black_24dp, "Subjects", 0, TreeQuizHolder.TreeQuizType.NONE));

        List<Category> categories = makeCategories();
        for (Category category : categories) {
            TreeQuizHolder.IconTreeItem parentNode = new TreeQuizHolder.IconTreeItem(android.R.drawable.ic_delete, category.getTitle(), category.id, TreeQuizHolder.TreeQuizType.SUBJECT);
            final TreeNode parent = new TreeNode(parentNode).setViewHolder(new TreeQuizHolder(getActivity()));
            for (Game game : category.getItems()) {
                TreeNode quizNode = new TreeNode(new TreeQuizHolder.IconTreeItem(R.drawable.ic_place_black_24dp, game.getName() + " (" + game.getQuestions().size() + ")", game.getId(), TreeQuizHolder.TreeQuizType.GAME, game));
                quizNode.setViewHolder(new TreeQuizHolder(getActivity()));

                List<Question> questions = game.getQuestions();
                for (int i = 0; i < questions.size(); i++) {
                    Question question = questions.get(i);
                    TreeNode questionNode = new TreeNode(new TreeQuizHolder.IconTreeItem(R.drawable.ic_add_circle_black_48dp, question.getQuestion(), i, TreeQuizHolder.TreeQuizType.QUESTION, game));
                    quizNode.addChild(questionNode);
                }
                parent.addChild(quizNode);
            }
            categoriesRoot.addChild(parent);
        }
        root.addChild(categoriesRoot);

        tView = new CustomAndroidTreeView(getActivity(), root);
        tView.setDefaultAnimation(false);
        tView.setDefaultContainerStyle(R.style.TreeNodeStyleCustom);
        tView.setDefaultViewHolder(TreeQuizHolder.class);

        containerView.addView(tView.getView());

        if (savedInstanceState != null) {
            String state = savedInstanceState.getString("tState");
            if (!TextUtils.isEmpty(state)) {
                tView.restoreState(state);
            }
        }
        return rootView;
    }

    public List<Category> makeCategories() {

        HashMap<String, Category> categories = new HashMap<>();

        Cursor category_cursor = POIsProvider.getAllCategories();
        while (category_cursor.moveToNext()) {
            long categoryId = category_cursor.getLong(category_cursor.getColumnIndexOrThrow("_id"));
            String categoryName = category_cursor.getString(category_cursor.getColumnIndexOrThrow("Name"));
            categories.put(categoryName.toLowerCase(), new Category(categoryId, categoryName, new ArrayList<>()));
        }


        Cursor quiz_cursor = POIsProvider.getAllQuizes();
        while (quiz_cursor.moveToNext()) {
            long quizId = quiz_cursor.getLong(quiz_cursor.getColumnIndexOrThrow("_id"));
            String questData = quiz_cursor.getString(quiz_cursor.getColumnIndexOrThrow("Data"));
            try {
                Trivia newQuiz = new Trivia().unpack(new JSONObject(questData));
                newQuiz.setId(quizId);

                Category category = categories.get(newQuiz.getCategory().toLowerCase());
                if (category == null) {
                    long id = POIsProvider.insertCategory(newQuiz.getCategory());
                    categories.put(newQuiz.getCategory().toLowerCase(), new Category(id, newQuiz.getCategory(), Collections.singletonList(newQuiz)));
                } else {
                    category.getItems().add(newQuiz);
                }
            } catch (Exception e) {
                Log.e("TAG", e.toString());
            }
        }

        //REMOVE EMPTY CATEGORIES
        Iterator<Map.Entry<String, Category>> iter = categories.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Category> entry = iter.next();
            if (entry.getValue().getItems().size() == 0) {
                iter.remove();
            }
        }

        //ORDER CATEGORIES BY ID
        ArrayList<Category> orderedCategories = new ArrayList<>(categories.values());
        Collections.sort(orderedCategories, (p1, p2) -> Long.compare(p1.id, p2.id));

        return orderedCategories;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tState", tView.getSaveState());
    }

}
