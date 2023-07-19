package com.bienhuels.iwmb_cookdome.viewmodel.RecipeView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bienhuels.iwmb_cookdome.Model.Ingredient;
import com.bienhuels.iwmb_cookdome.R;

import java.util.List;

public class IngrListAdapterNoBtn extends ArrayAdapter {
    public IngrListAdapterNoBtn(@NonNull Context context, int resource, @NonNull List ingredientList) {
        super(context, resource, ingredientList);
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
        ImageButton imageButton=(ImageButton)convertView.findViewById(R.id.removeStepBtn);
        imageButton.setVisibility(View.GONE);
        String amount=Double.toString(ingredient.getAmount());
        amountView.setText(amount);
        unitView.setText(ingredient.getUnit());
        ingredientView.setText(ingredient.getIngredientName());

        return convertView;
    }
}
