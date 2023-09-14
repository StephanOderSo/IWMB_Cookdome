package Viewmodel.CreateRecipeAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bienhuels.iwmb_cookdome.R;

import java.util.ArrayList;

public class StepListAdapter extends ArrayAdapter<String> {
    public StepListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<String> stepList) {
        super(context, resource, stepList);
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        String stepItem = getItem(position);

        if(convertView==null) {
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.cell_step_list_item,parent,false);
        }
        TextView counterView= convertView.findViewById(R.id.counter);
        TextView stepView=convertView.findViewById(R.id.step);
        ImageButton removeStepBtn=convertView.findViewById(R.id.removeStepBtn);
        removeStepBtn.setImageResource(R.drawable.remove);
        stepView.setText(stepItem);
        int counter=getPosition(stepItem)+1;
        String counterText= Integer.toString(counter);
        counterView.setText(counterText);
        removeStepBtn.setOnClickListener(view -> {
            String toBeRemoved=getItem(position);
            remove(toBeRemoved);
            notifyDataSetChanged();
        });

        return convertView;
    }
}
