package Viewmodel.SearchAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bienhuels.iwmb_cookdome.R;

import java.util.ArrayList;

import View.RecyclerViewHolder;

public class RecyclerAdapterLo extends RecyclerView.Adapter<RecyclerViewHolder>{
    private final Context context;
    private static ArrayList<String> list;

    public RecyclerAdapterLo(Context context, ArrayList<String> list) {
        this.context=context;
        RecyclerAdapterLo.list =list;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater.from(context).inflate(R.layout.cell_removable_item,parent,false));


    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        String rest=list.get(position);
        holder.textView2.setText(rest);
        holder.remove.setImageResource(R.drawable.remove);
        holder.remove.setOnClickListener(view -> {
            list.remove(rest);
            notifyItemRemoved(position);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
