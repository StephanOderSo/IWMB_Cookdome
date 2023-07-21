package Viewmodel.SearchAdapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bienhuels.iwmb_cookdome.R;
import View.RecyclerViewHolder;

import java.util.ArrayList;

public class RecyclerAdapterLo extends RecyclerView.Adapter<RecyclerViewHolder>{
    private Context context;
    private static ArrayList<String> list;

    public RecyclerAdapterLo(Context context, ArrayList<String> list) {
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
        String rest=list.get(position);
        Log.d("TAG", rest);
        holder.textView2.setText(rest);
        holder.remove.setImageResource(R.drawable.remove);
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list.remove(rest);
                Log.d("TAG", list.toString());
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
