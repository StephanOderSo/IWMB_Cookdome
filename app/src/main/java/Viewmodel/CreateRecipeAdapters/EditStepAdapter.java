package Viewmodel.CreateRecipeAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bienhuels.iwmb_cookdome.R;

import java.util.ArrayList;

public class EditStepAdapter extends ArrayAdapter {
    public EditStepAdapter(@NonNull Context context, int resource, @NonNull ArrayList<String> stepList) {
        super(context, resource, stepList);
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String step =  getItem(position).toString();

        if(convertView==null) {
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.edit_step,parent,false);
        }
        TextView counter=(TextView) convertView.findViewById(R.id.counter);
        EditText stepView=(EditText) convertView.findViewById(R.id.editStep);
        ImageButton removeBtn=(ImageButton)convertView.findViewById(R.id.removeStepbtn);
        ImageButton updateBtn=(ImageButton)convertView.findViewById(R.id.updateStepbtn);
        ImageButton editBtn=(ImageButton)convertView.findViewById(R.id.editstepBtn);
        ImageView up=(ImageView)convertView.findViewById(R.id.up);
        up.setImageResource(R.drawable.arrow_up_lav);
        ImageView down=(ImageView)convertView.findViewById(R.id.down);
        down.setImageResource(R.drawable.arrow_down_lav);
        editBtn.setImageResource(R.drawable.edit);
        removeBtn.setImageResource(R.drawable.remove);
        updateBtn.setImageResource(R.drawable.sync);
        String pos=String.valueOf(getPosition(step)+1);
        counter.setText(pos);
        stepView.setText(step);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editBtn.setVisibility(View.GONE);
                removeBtn.setVisibility(View.VISIBLE);
                updateBtn.setVisibility(View.VISIBLE);
                up.setVisibility(View.VISIBLE);
                down.setVisibility(View.VISIBLE);
            }
        });
        removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String toBeRemoved= getItem(position).toString();
                remove(toBeRemoved);
                notifyDataSetChanged();
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String toBeUpdated= getItem(position).toString();
                remove(toBeUpdated);
                String step=stepView.getText().toString();
                insert(step,position);
                removeBtn.setVisibility(View.GONE);
                updateBtn.setVisibility(View.GONE);
                up.setVisibility(View.GONE);
                down.setVisibility(View.GONE);
                editBtn.setVisibility(View.VISIBLE);
                notifyDataSetChanged();
            }
        });
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(position>0) {
                    String toBeUpdated = getItem(position).toString();
                    remove(toBeUpdated);
                    insert(toBeUpdated, position - 1);
                    notifyDataSetChanged();
                }
            }
        });
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(position<(getCount()-1)) {
                    String toBeUpdated = getItem(position).toString();
                    remove(toBeUpdated);
                    insert(toBeUpdated, position + 1);
                    notifyDataSetChanged();
                }
            }
        });

        return convertView;
    }
}
