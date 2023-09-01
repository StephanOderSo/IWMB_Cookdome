package Viewmodel.SearchAdapters;

import static android.content.ContentValues.TAG;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.view.View.GONE;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bienhuels.iwmb_cookdome.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import Model.Recipe;
import View.RecipeViewActivity;
import View.RecyclerViewHolder;

public class RecipeAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {
    private final Context context;
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
        Recipe recipe = list.get(position);
        holder.recipe_name.setText(recipe.getRecipeName());
        holder.time_show.setImageResource(R.drawable.time_white);
        holder.diet_show.setImageResource(R.drawable.check_white);
        holder.recipe_time.setText(String.valueOf(recipe.getPrepTime()));
        if(favlist.contains(recipe.getKey())){
            holder.favourite.setImageResource(R.drawable.liked);
            holder.favourite.setContentDescription(liked);
        }else{
            holder.favourite.setImageResource(R.drawable.unliked);
            holder.favourite.setContentDescription(unliked);
        }
        holder.favourite.setOnClickListener(view -> {
            auth = FirebaseAuth.getInstance();
            addToFavouritesList(recipe, holder.favourite, recipe.getKey());
        });
        StringBuilder dietaryTxt = new StringBuilder();
        if (recipe.getDietaryRec() == null) {
            holder.recipe_diet.setVisibility(GONE);
            holder.diet_show.setVisibility(GONE);
        } else if (recipe.getDietaryRec().isEmpty()) {
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
                    holder.recipe_diet.setVisibility(GONE);
                    holder.diet_show.setVisibility(GONE);
                    break;
                }
                dietaryTxt.append(dietShort);
                int i;
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
                .fit()
                .centerCrop()
                .into(holder.recipe_image);
        holder.recipeItem.setOnClickListener(view -> {
            Intent intent = new Intent(context, RecipeViewActivity.class);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("key", recipe.getKey());
            context.startActivity(intent);
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
    public void addToFavouritesList (Recipe recipe, ImageView view,String key){
                    if (view.getContentDescription().equals(liked)) {
                        databaseReference.child(id).child("Favourites").child(recipe.getKey()).removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                favlist.remove(key);
                                Log.d(TAG, favlist.toString());
                                view.setContentDescription(unliked);
                                view.setImageResource(R.drawable.unliked);
                                Toast.makeText(context, R.string.removed, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, R.string.sthWrong, Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
                    }else{
                        databaseReference.child(id).child("Favourites").child(recipe.getKey()).setValue(recipe).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                favlist.add(key);
                                view.setContentDescription(liked);
                                view.setImageResource(R.drawable.liked);
                                Toast.makeText(context, R.string.added, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, R.string.sthWrong, Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
    }
    public  void changeList(ArrayList <Recipe> list){
        this.list=list;
        notifyDataSetChanged();
    }
}


