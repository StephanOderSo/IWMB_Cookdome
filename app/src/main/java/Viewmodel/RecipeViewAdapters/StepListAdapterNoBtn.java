package Viewmodel.RecipeViewAdapters;

import android.content.Context;
import android.util.Log;
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

public class StepListAdapterNoBtn extends ArrayAdapter<Step> {
    public StepListAdapterNoBtn(@NonNull Context context, int resource, @NonNull ArrayList<Step> stepList) {
        super(context, resource, stepList);
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Step stepItem = getItem(position);
        if(stepItem!=null&&convertView==null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.cell_step_list_item, parent, false);

            TextView counterView = convertView.findViewById(R.id.counter);
            TextView stepView = convertView.findViewById(R.id.step);
            ImageButton imageButton = convertView.findViewById(R.id.removeStepBtn);
            ImageView stepImage = convertView.findViewById(R.id.stepImage);
            imageButton.setVisibility(View.GONE);
            stepView.setText(stepItem.getStep());
            if (stepItem.getMedia() != null) {
                stepImage.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(stepItem.getMedia())
                        .placeholder(R.drawable.image)
                        .fit()
                        .centerCrop()
                        .into(stepImage);
            }
            int counter = getPosition(stepItem) + 1;
            String counterText = Integer.toString(counter);
            counterView.setText(counterText);
        }

        return convertView;
    }
}
