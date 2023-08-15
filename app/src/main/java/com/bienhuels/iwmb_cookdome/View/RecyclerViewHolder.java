package com.bienhuels.iwmb_cookdome.View;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bienhuels.iwmb_cookdome.R;


public class RecyclerViewHolder extends RecyclerView.ViewHolder {
    public TextView textView2,recipe_time,recipe_diet;
    public ImageButton remove;
    public TextView recipe_name;
    public ImageView recipe_image,diet_show,time_show,favourite;
    public CardView recipeItem;




    public RecyclerViewHolder(@NonNull View itemView) {
        super(itemView);
        //Search/RecipeItem
        textView2=itemView.findViewById(R.id.textView2);
        recipe_name=itemView.findViewById(R.id.recipe_name);
        recipe_image=(itemView.findViewById(R.id.recipe_image));
        recipeItem=(itemView.findViewById(R.id.recipeItem));
        recipe_time=(itemView.findViewById(R.id.recipe_time));
        recipe_diet=(itemView.findViewById(R.id.recipe_diet));
        diet_show=(itemView.findViewById(R.id.dietshow));
        time_show=(itemView.findViewById(R.id.timeshow));
        favourite=(itemView.findViewById(R.id.favouritebtn));
        remove=(itemView.findViewById(R.id.remove));


    }
}
