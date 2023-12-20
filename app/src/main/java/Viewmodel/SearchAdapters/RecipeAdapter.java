package Viewmodel.SearchAdapters;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.view.View.GONE;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bienhuels.iwmb_cookdome.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import Model.Firebase;
import Model.Recipe;
import View.RecipeViewActivity;
import View.RecyclerViewHolder;
import Viewmodel.Tools;

public class RecipeAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {
    private final Context context;
    private ArrayList<Recipe> list;
    FirebaseAuth auth=FirebaseAuth.getInstance();
    FirebaseDatabase database=FirebaseDatabase.getInstance();
    DatabaseReference databaseReference;
    ArrayList<String>favlist;
    String id;
    String liked="liked";
    String unliked="unliked";
    String source;
    ArrayList<String>stringArray;
    Firebase firebase;

    public RecipeAdapter(Context context, ArrayList<Recipe> list, ArrayList<String>favlist, String id,String source,ArrayList<String>stringArray,Firebase firebase) {
        this.context = context;
        this.list = list;
        this.favlist=favlist;
        this.id=id;
        this.source=source;
        this.stringArray=stringArray;
        this.firebase=firebase;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater.from(context).inflate(R.layout.cell_recipe, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        database = FirebaseDatabase.getInstance();
        databaseReference=database.getReference("/Cookdome/Users");
        FirebaseUser fbUser=auth.getCurrentUser();
        Handler handler=new Handler();
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
            Runnable favRunnable= () -> firebase.updateFavouriteRecipes(recipe,context,holder.favourite,fbUser,handler);
            Thread favThread=new Thread(favRunnable);
            favThread.start();
        });

        if (recipe.getDietaryRec() == null) {
            holder.recipe_diet.setVisibility(GONE);
            holder.diet_show.setVisibility(GONE);
        } else if (recipe.getDietaryRec().isEmpty()) {
            holder.recipe_diet.setVisibility(GONE);
            holder.diet_show.setVisibility(GONE);
        } else {
            Runnable textbuild= () -> {
                Tools tools=new Tools();
                StringBuilder dietaryTxt = tools.setDietString(recipe,stringArray);
                handler.post(() -> holder.recipe_diet.setText(dietaryTxt.toString()));

            };
            Thread textBuildThread=new Thread(textbuild);
            textBuildThread.start();
        }
        Picasso.get()
                .load(recipe.getImage())
                .placeholder(R.drawable.image)
                .fit()
                .centerCrop()
                .into(holder.recipe_image);
        holder.recipeItem.setOnClickListener(view -> {
            Intent intent = new Intent(context, RecipeViewActivity.class);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("key", recipe.getKey());
            context.startActivity(intent);
        });

        if(source.equals("shared")){
            holder.removeRecipe.setVisibility(View.VISIBLE);
            holder.removeRecipe.setImageResource(R.drawable.remove_filled);
            holder.removeRecipe.setOnClickListener(view -> {
                Runnable dataRun=()-> handler.post(() -> notifyItemRemoved(position));
                Thread updateDataThread=new Thread(dataRun);

                Runnable run= () -> {
                    firebase.unshareRecipe(recipe.getKey(),id,recipe.getPriv(),context,handler);
                    firebase.removeUserFromShareList(id, recipe.getKey(),context,recipe.getOwner(),recipe.getPriv(),handler,updateDataThread);
                    list.remove(recipe);
                };
                Thread removeThread=new Thread(run);
                removeThread.start();
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void searchList(ArrayList<Recipe> searchList) {
        list = searchList;
        notifyDataSetChanged();
    }
}


