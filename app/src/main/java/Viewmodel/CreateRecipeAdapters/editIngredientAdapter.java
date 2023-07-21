package Viewmodel.CreateRecipeAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bienhuels.iwmb_cookdome.R;

import java.util.List;

import Model.Ingredient;

public class editIngredientAdapter extends ArrayAdapter {
    public editIngredientAdapter(@NonNull Context context, int resource, @NonNull List ingredientList) {
        super(context, resource, ingredientList);
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Ingredient ingredient = (Ingredient) getItem(position);

        if(convertView==null) {
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.edit_ingredient,parent,false);
        }
        EditText amountView=(EditText) convertView.findViewById(R.id.editamount);
        TextView unitView=(TextView)convertView.findViewById(R.id.unit);
        EditText ingredientView=(EditText) convertView.findViewById(R.id.editingredient);
        ImageButton removeBtn=(ImageButton)convertView.findViewById(R.id.removeIngredient);
        ImageButton updateBtn=(ImageButton)convertView.findViewById(R.id.updateIngredient);
        ImageButton editBtn=(ImageButton)convertView.findViewById(R.id.editIngrBtn);
        editBtn.setImageResource(R.drawable.edit);
        removeBtn.setImageResource(R.drawable.remove);
        updateBtn.setImageResource(R.drawable.sync);
        String amount=Double.toString(ingredient.getAmount());
        amountView.setText(amount);
        unitView.setText(ingredient.getUnit());
        ingredientView.setText(ingredient.getIngredientName());
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editBtn.setVisibility(View.GONE);
                removeBtn.setVisibility(View.VISIBLE);
                updateBtn.setVisibility(View.VISIBLE);
            }
        });
        removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Ingredient toBeRemoved=(Ingredient) getItem(position);
                remove(toBeRemoved);
                notifyDataSetChanged();
            }
        });
        Double i=ingredient.getAmount();
        String a=Double.toString(i);
        amountView.setText(a);
        unitView.setText(ingredient.getUnit());
        ingredientView.setText(ingredient.getIngredientName());
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Ingredient toBeUpdated=(Ingredient) getItem(position);
                remove(toBeUpdated);
                String unit=unitView.getText().toString();
                Double amount=Double.parseDouble(amountView.getText().toString());
                String ingredientname=ingredientView.getText().toString();
                Ingredient ingredientnew=new Ingredient(amount,unit,ingredientname);
                insert(ingredientnew,position);
                removeBtn.setVisibility(View.GONE);
                updateBtn.setVisibility(View.GONE);
                editBtn.setVisibility(View.VISIBLE);

            }
        });


        return convertView;
    }
}
