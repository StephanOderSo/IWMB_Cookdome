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

import Model.Ingredient;

public class IngredientListAdapter extends ArrayAdapter {
    ArrayList<Ingredient>list;
    public IngredientListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Ingredient> ingredientList) {
        super(context, resource, ingredientList);
        list=ingredientList;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Ingredient ingredient = (Ingredient) getItem(position);

        if(convertView==null) {
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.ingredient_list_item,parent,false);
        }
        TextView amountView=(TextView) convertView.findViewById(R.id.amountColumn);
        TextView unitView=(TextView)convertView.findViewById(R.id.unitColumn);
        TextView ingredientView=(TextView)convertView.findViewById(R.id.ingredientColumn);
        ImageButton removeStepBtn=(ImageButton)convertView.findViewById(R.id.removeStepBtn);
        removeStepBtn.setImageResource(R.drawable.remove);
        String amount=Double.toString(ingredient.getAmount());
        amountView.setText(amount);
        unitView.setText(ingredient.getUnit());
        ingredientView.setText(ingredient.getIngredientName());
        removeStepBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Ingredient toBeRemoved=(Ingredient) getItem(position);
                remove(toBeRemoved);
                notifyDataSetChanged();
            }
        });

        return convertView;
    }
}