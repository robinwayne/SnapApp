package com.example.antoinemaguet.snapapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


public class StoryFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_story_view, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView =(RecyclerView)view.findViewById(R.id.listRecyclerView) ;
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter
        List<ListObjectRecyclerView> myDataSet=new ArrayList<>();
        myDataSet.add(new ListObjectRecyclerView("test"));
        myDataSet.add(new ListObjectRecyclerView("eee"));
        myDataSet.add(new ListObjectRecyclerView("ggggg"));

        StoryFragmentAdapter adapter = new StoryFragmentAdapter(myDataSet);
        recyclerView.setAdapter(adapter);

    }
}
