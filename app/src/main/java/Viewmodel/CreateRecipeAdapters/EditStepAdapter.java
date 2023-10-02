package Viewmodel.CreateRecipeAdapters;

import static android.content.Intent.ACTION_GET_CONTENT;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import View.CreateRecipeActivity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintSet;

import com.bienhuels.iwmb_cookdome.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import Model.Step;

public class EditStepAdapter extends ArrayAdapter<Step> {
    ListView listview;
    Context context;


    public EditStepAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Step> stepList,ListView listview) {
        super(context, resource, stepList);
        this.context=context;
        this.listview=listview;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(getItem(position)!=null){
            Step step =  getItem(position);

            if(convertView==null) {
                convertView= LayoutInflater.from(getContext()).inflate(R.layout.cell_edit_step,parent,false);
            }
            TextView counter= convertView.findViewById(R.id.counter);
            EditText stepView= convertView.findViewById(R.id.editStep);
            ImageButton removeBtn= convertView.findViewById(R.id.removeStepbtn);
            ImageButton updateBtn= convertView.findViewById(R.id.updateStepbtn);
            ImageView stepImage=convertView.findViewById(R.id.stepImage);
            ImageView up= convertView.findViewById(R.id.up);
            up.setImageResource(R.drawable.arrow_up_lav);
            ImageView down= convertView.findViewById(R.id.down);
            down.setImageResource(R.drawable.arrow_down_lav);
            removeBtn.setImageResource(R.drawable.remove);
            updateBtn.setImageResource(R.drawable.sync);
            String pos=String.valueOf(getPosition(step)+1);
            counter.setText(pos);
            stepView.setText(step.getStep());
            if(step.getMedia()!=null){
                stepImage.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(step.getMedia())
                        .placeholder(R.drawable.image)
                        .fit()
                        .centerCrop()
                        .into(stepImage);
            }
            removeBtn.setOnClickListener(view -> {
                Step toBeRemoved= getItem(position);
                remove(toBeRemoved);
                notifyDataSetChanged();
            });

            updateBtn.setOnClickListener(view -> {
                Step toBeUpdated= getItem(position);
                remove(toBeUpdated);
                Step step1 =new Step(stepView.getText().toString());
                insert(step1,position);
                removeBtn.setVisibility(View.GONE);
                updateBtn.setVisibility(View.GONE);
                up.setVisibility(View.GONE);
                down.setVisibility(View.GONE);
                notifyDataSetChanged();
            });
            up.setOnClickListener(view -> {
                if(position>0) {
                    Step toBeUpdated = getItem(position);
                    remove(toBeUpdated);
                    insert(toBeUpdated, position - 1);
                    notifyDataSetChanged();
                }
            });
            down.setOnClickListener(view -> {
                if(position<(getCount()-1)) {
                    Step toBeUpdated = getItem(position);
                    remove(toBeUpdated);
                    insert(toBeUpdated, position + 1);
                    notifyDataSetChanged();
                }
            });
        }


        return convertView;
    }
}
