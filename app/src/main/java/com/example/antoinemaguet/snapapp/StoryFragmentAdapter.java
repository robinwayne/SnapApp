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
import com.squareup.picasso.Picasso;


public class StoryFragmentAdapter extends RecyclerView.Adapter<StoryFragmentAdapter.ViewHolder> {

    private List<ListObjectRecyclerView> dataset;

    // Provide a reference to the views for each data item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        private ImageView imageView;

        public ViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.mytv);
            imageView= v.findViewById(R.id.myiv);
        }

        public void bind(ListObjectRecyclerView mediaObject){
            textView.setText(mediaObject.getText());
            if(mediaObject.getImagePath()!=null) {
                Picasso.with(imageView.getContext()).load(mediaObject.getImagePath()).into(imageView);
            }
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public StoryFragmentAdapter(List<ListObjectRecyclerView> dataset) {
        this.dataset = dataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public StoryFragmentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_message, parent, false);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        ListObjectRecyclerView myObject = dataset.get(position);
        holder.bind(myObject);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataset.size();
    }
}
