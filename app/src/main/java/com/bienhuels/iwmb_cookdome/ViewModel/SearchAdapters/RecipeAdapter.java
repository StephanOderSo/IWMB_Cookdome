package com.bienhuels.iwmb_cookdome.Viewmodel.SearchAdapters;

import static android.content.ContentValues.TAG;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.view.View.GONE;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bienhuels.iwmb_cookdome.R;
import com.bienhuels.iwmb_cookdome.View.RecipeViewActivity;
import com.bienhuels.iwmb_cookdome.Model.Recipe;
import com.bienhuels.iwmb_cookdome.View.RecyclerViewHolder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RecipeAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {
    private Context context;
    private ArrayList<Recipe> list;
    FirebaseAuth auth;
    FirebaseDatabase database=FirebaseDatabase.getInstance();
    DatabaseReference databaseReference;
    ArrayList<String>favlist;
    String id;
    String liked="liked";
    String unliked="unliked";

    public RecipeAdapter(Context context, ArrayList<Recipe> list, ArrayList<String>favlist, String id) {
        this.context = context;
        this.list = list;
        this.favlist=favlist;
        this.id=id;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater.from(context).inflate(R.layout.recipe_cell, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        database = FirebaseDatabase.getInstance();
        databaseReference=database.getReference("/Cookdome/Users");
        Recipe recipe = (Recipe) list.get(position);
        holder.recipe_name.setText(recipe.getRecipeName());
        holder.time_show.setImageResource(R.drawable.time_white);
        holder.diet_show.setImageResource(R.drawable.check_white);
        holder.recipe_time.setText(String.valueOf(recipe.getPrepTime()));
        if(favlist.contains(recipe.getKey())){
            Log.d("FAVLIST", "Key in list");
            holder.favourite.setImageResource(R.drawable.liked);
            holder.favourite.setContentDescription(liked);
        }else{
            holder.favourite.setImageResource(R.drawable.unliked);
            holder.favourite.setContentDescription(unliked);
        }
        holder.favourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth = FirebaseAuth.getInstance();
                addToFavouritesList(recipe, holder.favourite);
            }
        });
        StringBuilder dietaryTxt = new StringBuilder();
        if (recipe.getDietaryRec() == null) {
            Log.d("ifcondition", "nolist");
            holder.recipe_diet.setVisibility(GONE);
            holder.diet_show.setVisibility(GONE);
        } else if (recipe.getDietaryRec().isEmpty()) {
            Log.d("ifcondition", "empty");
            holder.recipe_diet.setVisibility(GONE);
            holder.diet_show.setVisibility(GONE);
        } else {
            for (String diet : recipe.getDietaryRec()) {
                String dietShort = "";

                if (diet.equals(context.getResources().getString(R.string.vegetar))) {
                    dietShort = "VT";
                }
                if (diet.equals(context.getResources().getString(R.string.vegan))) {
                    dietShort = "V";
                }
                if (diet.equals(context.getResources().getString(R.string.glutenfree))) {
                    dietShort = "GF";
                }
                if (diet.equals(context.getResources().getString(R.string.lactosefree))) {
                    dietShort = "LF";
                }
                if (diet.equals(context.getResources().getString(R.string.paleo))) {
                    dietShort = "P";
                }
                if (diet.equals(context.getResources().getString(R.string.lowfat))) {
                    dietShort = "LF";
                }
                if (diet.equals(context.getResources().getString(R.string.none))) {
                    Log.d("ifcondition", "---");
                    holder.recipe_diet.setVisibility(GONE);
                    holder.diet_show.setVisibility(GONE);
                    break;
                }
                dietaryTxt.append(dietShort);
                Integer i;
                i = recipe.getDietaryRec().indexOf(diet);
                if (i != recipe.getDietaryRec().size() - 1) {
                    dietaryTxt.append(" | ");
                }
            }
        }

        holder.recipe_diet.setText(dietaryTxt);
        Picasso.get()
                .load(recipe.getImage())
                .placeholder(R.drawable.camera)
                .resize(100, 100)
                .centerCrop()
                .into(holder.recipe_image);
        holder.recipeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, RecipeViewActivity.class);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("key", recipe.getKey());
                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void searchList(ArrayList<Recipe> searchList) {
        list = searchList;
        notifyDataSetChanged();
    }
    public void addToFavouritesList (Recipe recipe, ImageView view){
        Log.d(TAG, view.getContentDescription().toString());
                    if (view.getContentDescription().equals(liked)) {
                        Log.d(TAG, "DID IT!!");
                        databaseReference.child(id).child("Favourites").child(recipe.getKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    favlist.remove(id);
                                    view.setContentDescription(unliked);
                                    view.setImageResource(R.drawable.unliked);
                                    Toast.makeText(context, "removed from favourites", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else{
                        databaseReference.child(id).child("Favourites").child(recipe.getKey()).setValue(recipe).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        favlist.add(id);
                                        view.setContentDescription(liked);
                                        view.setImageResource(R.drawable.liked);
                                        Toast.makeText(context, "Added to favourites", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                    }
    }
}


