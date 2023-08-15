package Viewmodel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bienhuels.iwmb_cookdome.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import Model.Ingredient;


public class ShoppinglistAdapter extends ArrayAdapter<Ingredient> {
    public ShoppinglistAdapter(@NonNull Context context, int ressource, ArrayList<Ingredient> shoppingList) {
        super(context,ressource,shoppingList);
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Ingredient ingredient= getItem(position);

        if(convertView==null) {
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.shoppinglist_item,parent,false);
        }
        TextView amountView= convertView.findViewById(R.id.amountColumnSl);
        TextView unitView= convertView.findViewById(R.id.unitColumnSl);
        TextView ingredientView= convertView.findViewById(R.id.ingredientColumnSl);
        CheckBox checkBox= convertView.findViewById(R.id.checkBoxSL);
        String amount=Double.toString(ingredient.getAmount());
        amountView.setText(amount);
        unitView.setText(ingredient.getUnit());
        ingredientView.setText(ingredient.getIngredientName());
        checkBox.setOnClickListener(view -> onCheck(checkBox,ingredient));


        return convertView;
    }
    private void onCheck(CheckBox checkbox,Ingredient ingredient){
        FirebaseDatabase database=FirebaseDatabase.getInstance();
        DatabaseReference dbrefUsers;
        FirebaseAuth auth;
        auth= FirebaseAuth.getInstance();
        FirebaseUser user=auth.getCurrentUser();
        String uID="";
        try{
            uID= user.getUid();
        }catch(Exception e){
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        dbrefUsers=database.getReference("Cookdome/Users");
        if(checkbox.isChecked()){
            dbrefUsers.child(uID).child("Shoppinglist").child(ingredient.getIngredientName()).removeValue().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Toast.makeText(getContext(), R.string.removed, Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
        }
        else{
            dbrefUsers.child(uID).child("Shoppinglist").child(ingredient.getIngredientName()).setValue(ingredient).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Toast.makeText(getContext(), R.string.added, Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}
