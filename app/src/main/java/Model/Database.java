package Model;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayoutStates;

import com.bienhuels.iwmb_cookdome.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

import View.MainActivity;
import View.RecyclerViewHolder;
import Viewmodel.CustomComparator;

public class Database {
    ArrayList<Recipe> recipes=new ArrayList<>();
    ArrayList<User>users=new ArrayList<>();
    FirebaseDatabase database=FirebaseDatabase.getInstance();
    DatabaseReference userRef=database.getReference("/Cookdome/Users");
    DatabaseReference recipeRef=database.getReference("/Cookdome/Recipes");
    int i;
    User user=new User();

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
                    handler.post(() -> {
                        thread.start();
                        Toast.makeText(context, R.string.retreived,Toast.LENGTH_SHORT).show();
                    });
                       }else{
                    handler.post(() -> Toast.makeText(context,R.string.dBEmpty,Toast.LENGTH_SHORT).show());
                }
            }else{
                handler.post(() -> Toast.makeText(context,R.string.dataRetrievalFailed,Toast.LENGTH_SHORT).show());
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
                            if(leftoverList!=null){
                                if(ingredientStringList.containsAll(leftoverList)){
                                    Log.d(ConstraintLayoutStates.TAG, " applied");
                                } else{
                                    continue;}
                            }else{
                                handler.post(() -> {
                                    Toast.makeText(context, R.string.noLeftoversSelected, Toast.LENGTH_SHORT).show();
                                    Intent backToLeftverIntent=new Intent(context, LayoutInflater.Filter.class);
                                    backToLeftverIntent.putExtra("action","");
                                    backToLeftverIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    context.startActivity(backToLeftverIntent);
                                });
                            }

                            recipes.add(selectedRecipe);
                        }
                    }
                    Collections.sort(recipes,new CustomComparator());
                    if(source.equals("categories")){
                        if(recipes.isEmpty()){
                            handler.post(() -> Toast.makeText(context,R.string.noMatch,Toast.LENGTH_SHORT).show());
                              }
                    }
                    if(source.equals("leftovers")){
                        if(recipes.isEmpty()){
                            handler.post(() -> Toast.makeText(context,R.string.noMatch,Toast.LENGTH_SHORT).show());
                               }
                    }
                    handler.post(() -> Toast.makeText(context,R.string.retreived,Toast.LENGTH_SHORT).show());

                }else{
                    handler.post(() -> Toast.makeText(context,R.string.dBEmpty,Toast.LENGTH_SHORT).show());
                         }
            }else{
                handler.post(() -> Toast.makeText(context,R.string.dataRetrievalFailed,Toast.LENGTH_SHORT).show());
                    }
            thread.start();
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
                    }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show()));
                }if(i==1){
                    thread.start();
                }i=i-1;

            }).addOnFailureListener(e2 -> handler.post(() -> Toast.makeText(context, e2.getMessage(), Toast.LENGTH_SHORT).show()));
        }
        Collections.sort(recipes,new CustomComparator());
        return recipes;
    }


    public void removeRecipeFromPublic(String key, Context context, Handler handler){
        recipeRef.child(key).removeValue().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                handler.post(() -> {  Toast.makeText(context, R.string.deletSuccess, Toast.LENGTH_SHORT).show();
                    Intent toMainIntent=new Intent(context, MainActivity.class);
                    toMainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(toMainIntent); });
            } else{handler.post(() -> Toast.makeText(context, R.string.sthWrong, Toast.LENGTH_SHORT).show());
            }
        }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show()));
    }
    public FirebaseRecyclerAdapter<User,RecyclerViewHolder> searchUsers(String searchInput,Recipe recipe,Context context,String uID){
        searchInput=searchInput.toLowerCase();
        Query query=userRef.orderByChild("name").startAt(searchInput).endAt(searchInput+"\uf8ff");
        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(query, snapshot -> new User().rebuildUser(snapshot))
                        .build();
        return new FirebaseRecyclerAdapter<User, RecyclerViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position, @NonNull User model) {
                Picasso.get()
                        .load(model.getPhoto())
                        .placeholder(R.drawable.image)
                        .fit()
                        .centerCrop()
                        .into(holder.userImage);
                holder.userName.setText(model.getName());
                holder.share.setImageResource(R.drawable.share);
                holder.share.setOnClickListener(view -> user.addToShared(recipe,model,context,uID));
            }
            @NonNull
            @Override
            public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_user,parent,false);
                return new RecyclerViewHolder(view);
            }
        };
    }
    public void addSharedPrivRecipes(Context context, FirebaseUser fbuser, Handler handler, Thread nextThread){
        String id=user.getUID(fbuser, context);
        userRef.child(id).child("Shared").child("private").get().addOnCompleteListener(task -> {
            DataSnapshot snapshot=task.getResult();
            if(snapshot.exists()){
                i=1;
                int size=Integer.parseInt(String.valueOf(snapshot.getChildrenCount()));
                for(DataSnapshot ss:snapshot.getChildren()){
                    String key=ss.getKey();
                    String userID=ss.getValue(String.class);
                    if(key!=null&&userID!=null){
                        userRef.child(userID).child("Privates").child(key).get().addOnCompleteListener(task1 -> {
                            DataSnapshot snapshot1= task1.getResult();
                            Recipe recipe=new Recipe().rebuildFromFirebase(snapshot1);
                            recipes.add(recipe);
                            if(i==size){
                                nextThread.start();
                            }
                            i++;
                        }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show()));
                    }else{
                        handler.post(() -> Toast.makeText(context, R.string.sthWrong, Toast.LENGTH_SHORT).show());
                    }
                }
            }else{
                nextThread.start();
            }
        });
    }

    public void addSharedPublRecipes(Context context, FirebaseUser fbuser, Handler handler, Thread nextThread){
        String id=user.getUID(fbuser, context);
        userRef.child(id).child("Shared").child("public").get().addOnCompleteListener(task -> {
            DataSnapshot snapshot= task.getResult();
            if(snapshot.exists()){
                i=1;
                int size=Integer.parseInt(String.valueOf(snapshot.getChildrenCount()));
                for(DataSnapshot ss:snapshot.getChildren()){
                    String key=ss.getValue(String.class);
                    if(key!=null){
                        recipeRef.child(key).get().addOnCompleteListener(task1 -> {
                            if(task1.isSuccessful()){
                                DataSnapshot snapshot1= task1.getResult();
                                Recipe recipe=new Recipe().rebuildFromFirebase(snapshot1);
                                recipes.add(recipe);
                                if(i==size){
                                    nextThread.start();
                                }
                                i++;
                            }
                        }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show()));
                    }else{  handler.post(() -> Toast.makeText(context, R.string.sthWrong, Toast.LENGTH_SHORT).show());}
                }
            }else{
                nextThread.start();
            }
        }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show()));
    }
    public ArrayList<Recipe> getRecipes(){
        return  this.recipes;
    }

    public  void setSharedWithUsers(ArrayList<String> sharedWith,Handler handler,Context context,Thread nextThread){
        i=1;
        int size=sharedWith.size();
        for(String userID:sharedWith){
            userRef.child(userID).get().addOnCompleteListener(task -> {
                DataSnapshot snapshot= task.getResult();
                user=user.rebuildUser(snapshot);
                users.add(user);
                if(i==size){
                    nextThread.start();
                }
                i++;
            }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show()));
        }
    }
    public ArrayList<User> getUsers(){
        return  this.users;
    }
}
