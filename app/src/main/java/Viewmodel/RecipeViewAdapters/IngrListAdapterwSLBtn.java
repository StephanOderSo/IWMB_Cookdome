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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import Model.Ingredient;
import com.bienhuels.iwmb_cookdome.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


public class IngrListAdapterwSLBtn extends ArrayAdapter {
    FirebaseDatabase database=FirebaseDatabase.getInstance();
    DatabaseReference dbRefUsers;
    FirebaseAuth auth;
    public IngrListAdapterwSLBtn(@NonNull Context context, int resource, @NonNull ArrayList ingredientList) {
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
                addToSList(ingredient,imageButton);

            }
        });


        return convertView;
    }

    public void addToSList(Ingredient ingredient,ImageButton imageButton){
        dbRefUsers =database.getReference("Cookdome/Users");
        auth=FirebaseAuth.getInstance();
        FirebaseUser user=auth.getCurrentUser();
        String uID= user.getUid();
        dbRefUsers.child(uID).child("Shoppinglist").child(ingredient.getIngredientName()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();

                    Double amount=snapshot.child("amount").getValue(Double.class);
                    String unit=snapshot.child("unit").getValue(String.class);
                    String name=snapshot.child("ingredientName").getValue(String.class);

                    if(ingredient.getUnit().equals(unit)){
                        amount=amount+ingredient.getAmount();
                    }else{
                        convertUnit();
                    }
                    Ingredient updatedIngredient=new Ingredient(amount,unit,name);
                    dbRefUsers.child(uID).child("Shoppinglist").child(ingredient.getIngredientName()).setValue(updatedIngredient).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                imageButton.setImageResource(R.drawable.tick);
                                imageButton.setBackground(null);
                                imageButton.setBackgroundColor(00000000);
                                Toast.makeText(getContext(), R.string.added, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }else{
                    Log.d(TAG, "Ingredient not in List yet");
                    dbRefUsers.child(uID).child("Shoppinglist").child(ingredient.getIngredientName()).setValue(ingredient).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getContext(), R.string.added, Toast.LENGTH_SHORT).show();
                                imageButton.setImageResource(R.drawable.tick);
                                imageButton.setBackground(null);
                                imageButton.setBackgroundColor(00000000);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }



        });
    }
    public void convertUnit() {
    }

}
