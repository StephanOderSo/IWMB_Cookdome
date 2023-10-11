package Viewmodel.CreateRecipeAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bienhuels.iwmb_cookdome.R;

import java.util.ArrayList;

import Model.Ingredient;

public class IngredientListAdapter extends ArrayAdapter<Ingredient> {
    ArrayList<Ingredient>list;
    public IngredientListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Ingredient> ingredientList) {
        super(context, resource, ingredientList);
        list=ingredientList;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Ingredient ingredient = getItem(position);

        if(convertView==null) {
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.cell_ingredient_list_item,parent,false);
        }
        if (ingredient != null) {


        TextView amountView= convertView.findViewById(R.id.amountColumn);
        TextView unitView= convertView.findViewById(R.id.unitColumn);
        TextView ingredientView= convertView.findViewById(R.id.ingredientColumn);
        ImageButton removeStepBtn= convertView.findViewById(R.id.removeStepBtn);
        removeStepBtn.setImageResource(R.drawable.remove);
        String amount=Double.toString(ingredient.getAmount());
        amountView.setText(amount);
        unitView.setText(ingredient.getUnit());
        ingredientView.setText(ingredient.getName());
        removeStepBtn.setOnClickListener(view -> {
            Ingredient toBeRemoved= getItem(position);
            remove(toBeRemoved);
            notifyDataSetChanged();
        });
    }else{
            Toast.makeText(getContext(), R.string.sthWrong, Toast.LENGTH_SHORT).show();
        }
        return convertView;
    }
}
