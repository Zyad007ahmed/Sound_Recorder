package org.horaapps.soundrecorder.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.horaapps.soundrecorder.R;
import org.horaapps.soundrecorder.adapters.FileViewerAdapter;

public class FileViewerFragment extends Fragment {

    public static FileViewerFragment newInstance() {
        return new FileViewerFragment();
    }

    private FileViewerAdapter fileViewerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_file_viewer, container, false);

        RecyclerView recyclerView = v.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(RecyclerView.VERTICAL);

        //newest to oldest order (database stores from oldest to newest)
        llm.setReverseLayout(true);
        llm.setStackFromEnd(true);

        recyclerView.setLayoutManager(llm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        fileViewerAdapter = new FileViewerAdapter(getActivity());
        recyclerView.setAdapter(fileViewerAdapter);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        fileViewerAdapter.notifyDataSetChanged();
    }
}