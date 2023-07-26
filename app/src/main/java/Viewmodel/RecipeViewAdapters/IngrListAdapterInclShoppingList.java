package Viewmodel.RecipeViewAdapters;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import View.RecipeViewActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import Model.Ingredient;
import com.bienhuels.iwmb_cookdome.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


public class IngrListAdapterInclShoppingList extends ArrayAdapter {
    FirebaseDatabase database=FirebaseDatabase.getInstance();
    DatabaseReference dbRefShoppingList;
    ArrayList<Ingredient> shoppingList;
    public IngrListAdapterInclShoppingList(@NonNull Context context, int resource, @NonNull ArrayList ingredientList) {
        super(context, resource,ingredientList);


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
        String amount=Double.toString(ingredient.getAmount());
        amountView.setText(amount);
        unitView.setText(ingredient.getUnit());
        ingredientView.setText(ingredient.getIngredientName());
        ImageButton imageButton=(ImageButton)convertView.findViewById(R.id.removeStepBtn);
        imageButton.setImageResource(R.drawable.add_to_cart);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbRefShoppingList=database.getReference("Cookdome/Users/ShoppingList");
                Context context = null;
                dbRefShoppingList.child(ingredient.getIngredientName()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (task.getResult().exists()) {
                                DataSnapshot snapshot = task.getResult();

                                    Double amount=snapshot.child("amount").getValue(Double.class);
                                    String unit=snapshot.child("unit").getValue(String.class);

                                    Ingredient dBingredient=new Ingredient(amount,unit,ingredient.getIngredientName());
                                Log.d(TAG, dBingredient.toString());

                            }else{
                                Log.d(TAG, "Ingredient not in List yet");
                            }
                        }

                });
            }
        });


        return convertView;
    }

}
