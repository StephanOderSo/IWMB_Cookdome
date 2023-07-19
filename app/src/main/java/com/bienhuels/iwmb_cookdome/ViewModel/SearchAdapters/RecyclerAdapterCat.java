package com.bienhuels.iwmb_cookdome.Viewmodel.SearchAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bienhuels.iwmb_cookdome.R;
import com.bienhuels.iwmb_cookdome.View.RecyclerViewHolder;

import java.util.ArrayList;

public class RecyclerAdapterCat extends RecyclerView.Adapter<RecyclerViewHolder> {
    private Context context;
    private static ArrayList<String>list;

    public RecyclerAdapterCat(Context context, ArrayList<String> list) {
        this.context=context;
        this.list=list;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater.from(context).inflate(R.layout.removable_cell,parent,false));


    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        holder.textView2.setText(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }





}
