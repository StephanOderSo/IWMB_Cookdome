package Model;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayoutStates;

import com.bienhuels.iwmb_cookdome.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;

import Viewmodel.CustomComparator;
import Viewmodel.SearchAdapters.RecipeAdapter;

public class Database {
    ArrayList<Recipe> recipes=new ArrayList<>();
    ArrayList<User>users;
    FirebaseDatabase database=FirebaseDatabase.getInstance();
    FirebaseAuth auth;
    DatabaseReference userRef=database.getReference("/Cookdome/Users");
    DatabaseReference recipeRef=database.getReference("/Cookdome/Recipes");
    int i;

    public synchronized ArrayList<Recipe> getAllRecipes(Context context, Handler handler, Thread thread) {
        recipeRef.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    for(DataSnapshot dsS:snapshot.getChildren()){
                        Recipe selectedRecipe=new Recipe().rebuildFromFirebase(dsS);
                        recipes.add(selectedRecipe);
                    }
                    Collections.sort(recipes,new CustomComparator());
                    Log.d("TAG", "notified ");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (thread){
                                thread.notify();
                            }
                            Toast.makeText(context, R.string.retreived,Toast.LENGTH_SHORT).show();
                        }
                    });
                       }else{
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context,R.string.dBEmpty,Toast.LENGTH_SHORT).show();}});
                }
            }else{
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context,R.string.dataRetrievalFailed,Toast.LENGTH_SHORT).show();}});
            }
        });
        return recipes;
    }
    //retreive recipes from firebase that meet the source criteria
    public ArrayList<Recipe> getSelectedRecipes(String catFilter, String source, Context context, Intent previousIntent,Handler handler,Thread thread){
        recipeRef.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    for(DataSnapshot dsS:snapshot.getChildren()){
                        Recipe selectedRecipe=new Recipe().rebuildFromFirebase(dsS);
                        if(source.equals("categories")){
                            if (selectedRecipe.getCategory().equals(catFilter)) {
                                recipes.add(selectedRecipe);
                            }
                        }
                        if(source.equals("leftovers")){
                            ArrayList<String> leftoverList=previousIntent.getStringArrayListExtra("action");
                            ArrayList<String>ingredientStringList=new ArrayList<>();
                            for(Ingredient ingredient:selectedRecipe.getIngredientList()){
                                String name=ingredient.getIngredientName();
                                ingredientStringList.add(name);
                            }
                            if(ingredientStringList.containsAll(leftoverList)){
                                Log.d(ConstraintLayoutStates.TAG, " applied");
                            } else{
                                continue;}
                            recipes.add(selectedRecipe);
                        }
                    }
                    Collections.sort(recipes,new CustomComparator());
                    if(source.equals("categories")){
                        if(recipes.isEmpty()){
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context,R.string.noMatch,Toast.LENGTH_SHORT).show();}});
                              }
                    }
                    if(source.equals("leftovers")){
                        if(recipes.isEmpty()){
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context,R.string.noMatch,Toast.LENGTH_SHORT).show();}});
                               }
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context,R.string.retreived,Toast.LENGTH_SHORT).show();
                            }});

                }else{
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context,R.string.dBEmpty,Toast.LENGTH_SHORT).show();}});
                         }
            }else{
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context,R.string.dataRetrievalFailed,Toast.LENGTH_SHORT).show();}});
                    }
            synchronized (thread){
                thread.notify();
            }
        });
        return recipes;
    }

    //Download and display a List of Recipes that the User either liked or created
    public ArrayList<Recipe> getFavouriteOrOwnRecipes(ArrayList<String> keylist, Context context, String id, Handler handler,Thread thread){
        i=keylist.size();
        for(String key:keylist){
            recipeRef.child(key).get().addOnCompleteListener(task -> {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    Recipe selectedRecipe=new Recipe().rebuildFromFirebase(snapshot);
                    recipes.add(selectedRecipe);
                       }else{
                    userRef.child(id).child("Privates").child(key).get().addOnCompleteListener(task1 -> {
                        if (task1.getResult().exists()) {
                            DataSnapshot snapshot2 = task1.getResult();
                            Recipe selectedRecipe=new Recipe().rebuildFromFirebase(snapshot2);
                            recipes.add(selectedRecipe);
                               }
                    }).addOnFailureListener(e -> {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();}});
                         });
                }if(i==1){
                    synchronized (thread){
                        thread.notify();
                    }

                }i=i-1;

            }).addOnFailureListener(e2 -> {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, e2.getMessage(), Toast.LENGTH_SHORT).show();}});
            });
        }
        Collections.sort(recipes,new CustomComparator());
        return recipes;
    }
}
