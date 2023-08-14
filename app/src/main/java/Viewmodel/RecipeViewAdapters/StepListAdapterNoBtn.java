package Viewmodel.RecipeViewAdapters;

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

public class StepListAdapterNoBtn extends ArrayAdapter<String> {
    public StepListAdapterNoBtn(@NonNull Context context, int resource, @NonNull ArrayList<String> stepList) {
        super(context, resource, stepList);
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String stepItem = getItem(position);

        if(convertView==null) {
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.step_list_item,parent,false);
        }
        TextView counterView= convertView.findViewById(R.id.counter);
        TextView stepView= convertView.findViewById(R.id.step);
        ImageButton imageButton= convertView.findViewById(R.id.removeStepBtn);
        imageButton.setVisibility(View.GONE);
        stepView.setText(stepItem);
        int counter=getPosition(stepItem)+1;
        String counterText= Integer.toString(counter);
        counterView.setText(counterText);

        return convertView;
    }
}
