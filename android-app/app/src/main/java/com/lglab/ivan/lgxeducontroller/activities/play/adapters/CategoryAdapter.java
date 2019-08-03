package com.lglab.ivan.lgxeducontroller.activities.play.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.games.Game;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.utils.Utils;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.GameViewHolder> {

    private List<Game> gameList;
    private Context context;

    public CategoryAdapter(List<Game> list, Context context) {
        this.gameList = list;
        this.context = context;
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new GameViewHolder(LayoutInflater.from(context).inflate(R.layout.activity_play_inner_row_new, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final GameViewHolder holder, final int position) {
        Game game = gameList.get(position);

        holder.textViewTitle.setText(game.getName());
        holder.textViewGenre.setText(game.getType().name());

        //new DownloadImageTask(holder.imageViewMovie).execute("https://i.ytimg.com/vi/ymIhLJ5AKpE/maxresdefault.jpg");
        try {
            Bitmap image = game.getImage(this.context);
            image = Utils.getRoundedCornerBitmap(image, (int) holder.itemView.getContext().getResources().getDimension(R.dimen._10sdp));
            if (image != null)
                holder.imageViewMovie.setImageBitmap(image);
        } catch(Exception e) {
            Log.e("ERROR", e.toString() + " || " + game.getName());
        }
        holder.itemView.setOnClickListener(arg0 -> startGame((Activity) holder.itemView.getContext(), position));
        /*Picasso.with(context).
                load(context.getResources().getString(R.string.image_url) + movie.getPoster())
                .into(holder.imageViewMovie);*/
    }

    private void startGame(Activity activity, final int position) {
        GameManager.startGame(activity, gameList.get(position));
    }

    @Override
    public int getItemCount() {
        return gameList.size();
    }

    class GameViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewTitle;
        private TextView textViewGenre;
        private AppCompatImageView imageViewMovie;

        GameViewHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.game_name);
            textViewGenre = itemView.findViewById(R.id.game_type);
            imageViewMovie = itemView.findViewById(R.id.image_view_game);
        }
    }
}
