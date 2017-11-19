package com.example.antoinemaguet.snapapp;

/**
 * Created by antoinemaguet on 03/11/2017.
 */
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;


public class StoryFragmentAdapter extends RecyclerView.Adapter<StoryFragmentAdapter.ViewHolder> {

    private List<ListObjectRecyclerView> dataset;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        private ImageView imageView;

        public ViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.storytext);
            imageView= (ImageView) v.findViewById(R.id.storyimage);
        }

        public void bind(ListObjectRecyclerView mediaObject){
            textView.setText(mediaObject.getText());
            imageView.setImageBitmap(mediaObject.getImagePath());
        }
    }

    public StoryFragmentAdapter(List<ListObjectRecyclerView> dataset) {
        this.dataset = dataset;
    }

    @Override
    public StoryFragmentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_message, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        ListObjectRecyclerView myObject = dataset.get(position);
        holder.bind(myObject);
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }
}
