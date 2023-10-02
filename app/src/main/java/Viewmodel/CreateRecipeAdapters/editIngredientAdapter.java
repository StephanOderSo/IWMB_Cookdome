package Viewmodel.CreateRecipeAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bienhuels.iwmb_cookdome.R;
import java.util.List;
import Model.Ingredient;
import View.CreateRecipeActivity;

public class editIngredientAdapter extends ArrayAdapter<Ingredient> {
    Context contextm;
    Thread thread;
    public editIngredientAdapter(@NonNull Context context, int resource, @NonNull List<Ingredient> ingredientList) {
        super(context, resource, ingredientList);
        contextm=context;

    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Ingredient ingredient = getItem(position);

        if(convertView==null) {
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.cell_edit_ingredient,parent,false);
        }
        if(ingredient!=null) {

            EditText amountView = convertView.findViewById(R.id.editamount);
            TextView unitView = convertView.findViewById(R.id.unit);
            EditText ingredientView = convertView.findViewById(R.id.editingredient);
            ImageButton removeBtn = convertView.findViewById(R.id.removeIngredient);
            ImageButton updateBtn = convertView.findViewById(R.id.updateIngredient);
            removeBtn.setImageResource(R.drawable.remove);
            updateBtn.setImageResource(R.drawable.sync);
            String amount = Double.toString(ingredient.getAmount());
            amountView.setText(amount);
            unitView.setText(ingredient.getUnit());
            ingredientView.setText(ingredient.getIngredientName());

            removeBtn.setOnClickListener(view -> {
                Ingredient toBeRemoved = getItem(position);
                remove(toBeRemoved);
                notifyDataSetChanged();
            });
            double i = ingredient.getAmount();
            String a = Double.toString(i);
            amountView.setText(a);
            unitView.setText(ingredient.getUnit());
            ingredientView.setText(ingredient.getIngredientName());
            updateBtn.setOnClickListener(view -> {
                Ingredient toBeUpdated = getItem(position);
                remove(toBeUpdated);
                String unit = unitView.getText().toString();
                double amount1 = Double.parseDouble(amountView.getText().toString());
                String ingredientname = ingredientView.getText().toString();
                Ingredient ingredientnew = new Ingredient(amount1, unit, ingredientname);
                insert(ingredientnew, position);
                removeBtn.setVisibility(View.GONE);
                updateBtn.setVisibility(View.GONE);
            });
        }else{
            Toast.makeText(getContext(), R.string.sthWrong, Toast.LENGTH_SHORT).show();
        }
        return convertView;
    }
}
