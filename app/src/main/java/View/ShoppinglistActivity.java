package View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

import Model.Ingredient;
import Viewmodel.ShoppinglistAdapter;

public class ShoppinglistActivity extends AppCompatActivity {
    FirebaseDatabase database=FirebaseDatabase.getInstance();
    DatabaseReference dbRefUsers;
    ArrayList<Ingredient> shoppingList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shoppinglist);
        FirebaseAuth auth;
        auth= FirebaseAuth.getInstance();
        FirebaseUser user=auth.getCurrentUser();
        String uID=new String();
        try{
            uID=user.getUid();
        }catch(NullPointerException e){
            Toast.makeText(this, R.string.signedOut, Toast.LENGTH_SHORT).show();
            Intent i=new Intent(ShoppinglistActivity.this,LoginActivity.class);
            startActivity(i);
        }

        dbRefUsers =database.getReference("Cookdome/Users");
        shoppingList=new ArrayList<>();
        ShoppinglistAdapter adapter=new ShoppinglistAdapter(getApplicationContext(),0,shoppingList);
        ListView shoppinglistView=findViewById(R.id.shoppinglistView);
        shoppinglistView.setBackground(null);
        shoppinglistView.setAdapter(adapter);
        dbRefUsers.child(uID).child("Shoppinglist").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    DataSnapshot snapshot=task.getResult();
                    for(DataSnapshot IngSS:snapshot.getChildren()){
                        Double amount=IngSS.child("amount").getValue(Double.class);
                        String unit=IngSS.child("unit").getValue(String.class);
                        String ingredientName=IngSS.child("ingredientName").getValue(String.class);
                        Ingredient ingredient=new Ingredient(amount,unit,ingredientName);
                        shoppingList.add(ingredient);
                        Log.d("ingredient", ingredient.toString());}
                    adapter.notifyDataSetChanged();
                    if(shoppingList.isEmpty()){
                        TextView slEmpty=findViewById(R.id.shoppinngListEmpty);
                        CardView slCard=findViewById(R.id.shoppingListCard);
                        shoppinglistView.setVisibility(View.GONE);
                        slEmpty.setVisibility(View.VISIBLE);


                    }

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ShoppinglistActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }
}