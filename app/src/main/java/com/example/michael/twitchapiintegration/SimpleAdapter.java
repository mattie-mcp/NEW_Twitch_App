package com.example.michael.twitchapiintegration;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by michael on 2/15/2016.
 */
public class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.ViewHolder> {

    ArrayList<String> mItems;
    ArrayList<String> previewLinks;
    ArrayList<String> game;
    int controller;

    public SimpleAdapter(ArrayList<String> s,ArrayList<String> a, ArrayList<String> g, int c) {
        super();
        mItems = s;
        previewLinks = a;
        game = g;
        controller = c;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recycler_layout, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v, controller);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        if (controller == 0){
            viewHolder.itemView.setText(mItems.get(i));
            viewHolder.itemViewGame.setText(game.get(i));
            Context context = viewHolder.imageView.getContext();
            Picasso.with(context).load(previewLinks.get(i)).into(viewHolder.imageView);
        }
        else if (controller == 1){
            viewHolder.itemViewGame.setText(mItems.get(i));
            Context context = viewHolder.imageView.getContext();
            Picasso.with(context).load(previewLinks.get(i)).into(viewHolder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        public TextView itemView;
        public ImageView imageView;
        public TextView itemViewGame;
        public ViewHolder(final View itemView, int c) {
            super(itemView);
            if (c== 0){
                this.itemView = (TextView)itemView.findViewById(R.id.title);
                this.imageView = (ImageView)itemView.findViewById(R.id.imageView);
                this.itemViewGame = (TextView)itemView.findViewById(R.id.game);
            }
            else if (c == 1){
                this.imageView = (ImageView)itemView.findViewById(R.id.imageView);
                this.itemViewGame = (TextView)itemView.findViewById(R.id.title);
            }
        }
    }
}
