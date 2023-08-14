package Viewmodel.RecipeViewAdapters;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import Model.Ingredient;


public class IngrListAdapterwSLBtn extends ArrayAdapter<Ingredient> {
    FirebaseDatabase database=FirebaseDatabase.getInstance();
    DatabaseReference dbRefUsers;
    FirebaseAuth auth;
    public IngrListAdapterwSLBtn(@NonNull Context context, int resource, @NonNull ArrayList<Ingredient> ingredientList) {
        super(context, resource,ingredientList);


    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Ingredient ingredient = getItem(position);

        if(convertView==null) {
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.ingredient_list_item,parent,false);
        }
        TextView amountView= convertView.findViewById(R.id.amountColumn);
        TextView unitView= convertView.findViewById(R.id.unitColumn);
        TextView ingredientView= convertView.findViewById(R.id.ingredientColumn);
        String amount=Double.toString(ingredient.getAmount());
        amountView.setText(amount);
        unitView.setText(ingredient.getUnit());
        ingredientView.setText(ingredient.getIngredientName());
        ImageButton imageButton= convertView.findViewById(R.id.removeStepBtn);
        imageButton.setImageResource(R.drawable.add_to_cart);
        imageButton.setOnClickListener(view -> addToSList(ingredient,imageButton));


        return convertView;
    }

    public void addToSList(Ingredient ingredient,ImageButton imageButton){
        dbRefUsers =database.getReference("Cookdome/Users");
        auth=FirebaseAuth.getInstance();
        FirebaseUser user=auth.getCurrentUser();
        String uID= user.getUid();
        dbRefUsers.child(uID).child("Shoppinglist").child((ingredient.getIngredientName())+":"+ingredient.getUnit()).get().addOnCompleteListener(task -> {
            if (task.getResult().exists()) {
                DataSnapshot snapshot = task.getResult();
                Double amount=snapshot.child("amount").getValue(Double.class);
                String unit=snapshot.child("unit").getValue(String.class);
                String name=snapshot.child("ingredientName").getValue(String.class);
                    try{
                    amount+=ingredient.getAmount();}
                    catch (NullPointerException e){
                        amount=ingredient.getAmount();
                    }
                Ingredient updatedIngredient=new Ingredient(amount,unit,name);
                dbRefUsers.child(uID).child("Shoppinglist").child((ingredient.getIngredientName())+":"+ingredient.getUnit()).setValue(updatedIngredient).addOnCompleteListener(task12 -> {
                    if(task12.isSuccessful()){
                        imageButton.setImageResource(R.drawable.tick);
                        imageButton.setBackground(null);
                        imageButton.setBackgroundColor(0);
                        Toast.makeText(getContext(), R.string.added, Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());

            }else{
                dbRefUsers.child(uID).child("Shoppinglist").child((ingredient.getIngredientName())+":"+ingredient.getUnit()).setValue(ingredient).addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful()){
                        Toast.makeText(getContext(), R.string.added, Toast.LENGTH_SHORT).show();
                        imageButton.setImageResource(R.drawable.tick);
                        imageButton.setBackground(null);
                        imageButton.setBackgroundColor(0);
                    }
                }).addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());

            }
        });
    }
}
