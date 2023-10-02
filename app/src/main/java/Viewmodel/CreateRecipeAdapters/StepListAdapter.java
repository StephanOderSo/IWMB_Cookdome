package Viewmodel.CreateRecipeAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bienhuels.iwmb_cookdome.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import Model.Step;

public class StepListAdapter extends ArrayAdapter<Step> {
    public StepListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Step> stepList) {
        super(context, resource, stepList);
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView==null) {
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.cell_step_list_item,parent,false);
        }
        if(getItem(position)!=null){
            Step stepItem = getItem(position);

        TextView counterView= convertView.findViewById(R.id.counter);
        TextView stepView=convertView.findViewById(R.id.step);
        ImageButton removeStepBtn=convertView.findViewById(R.id.removeStepBtn);
        ImageView stepImage=convertView.findViewById(R.id.stepImage);
        removeStepBtn.setImageResource(R.drawable.remove);
        stepView.setText(stepItem.getStep());
        int counter=getPosition(stepItem)+1;
        String counterText= Integer.toString(counter);
        counterView.setText(counterText);
        if(stepItem.getMedia()!=null){
            stepImage.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(stepItem.getMedia())
                    .placeholder(R.drawable.image)
                    .fit()
                    .centerCrop()
                    .into(stepImage);
            removeStepBtn.bringToFront();
            removeStepBtn.invalidate();
        }
        removeStepBtn.setOnClickListener(view -> {
            Step toBeRemoved=getItem(position);
            remove(toBeRemoved);
            notifyDataSetChanged();
        });
    }
        return convertView;
    }
}
