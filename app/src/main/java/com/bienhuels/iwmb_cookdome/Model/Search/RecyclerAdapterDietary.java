package com.bienhuels.iwmb_cookdome.Model.Search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bienhuels.iwmb_cookdome.R;
import com.bienhuels.iwmb_cookdome.Model.RecyclerViewHolder;

import java.util.ArrayList;

public class RecyclerAdapterDietary extends RecyclerView.Adapter<RecyclerViewHolder>{
    private Context context;
    private ArrayList<String> list;

    public RecyclerAdapterDietary(Context context, ArrayList<String> list) {
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
