package Viewmodel.SearchAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bienhuels.iwmb_cookdome.R;
import View.RecyclerViewHolder;

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
        return new RecyclerViewHolder(LayoutInflater.from(context).inflate(R.layout.cell_removable_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        holder.textView2.setText(list.get(position));
        holder.textView2.setPadding(6,5,6,5);
        holder.remove.setVisibility(View.GONE);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }




}
