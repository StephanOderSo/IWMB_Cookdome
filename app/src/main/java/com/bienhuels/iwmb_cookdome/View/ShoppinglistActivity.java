package com.bienhuels.iwmb_cookdome.View;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bienhuels.iwmb_cookdome.R;
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
        String uID="";
        try{
            uID=user.getUid();
        }catch(NullPointerException e){
            Toast.makeText(this, R.string.signedOut, Toast.LENGTH_SHORT).show();
            Intent i=new Intent(ShoppinglistActivity.this, LoginActivity.class);
            startActivity(i);
        }

        dbRefUsers =database.getReference("Cookdome/Users");
        shoppingList=new ArrayList<>();
        ShoppinglistAdapter adapter=new ShoppinglistAdapter(getApplicationContext(),0,shoppingList);
        ListView shoppinglistView=findViewById(R.id.shoppinglistView);
        shoppinglistView.setBackground(null);
        shoppinglistView.setAdapter(adapter);
        dbRefUsers.child(uID).child("Shoppinglist").get().addOnCompleteListener(task -> {
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
                    shoppinglistView.setVisibility(View.GONE);
                    slEmpty.setVisibility(View.VISIBLE);


                }

            }

        }).addOnFailureListener(e -> Toast.makeText(ShoppinglistActivity.this, e.getMessage(), Toast.LENGTH_LONG).show());

    }
}