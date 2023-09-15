package View;

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
    public ImageButton remove,share;
    public TextView recipe_name,userName;
    public ImageView recipe_image,diet_show,time_show,favourite,userImage,removeRecipe;
    public CardView recipeItem;




    public RecyclerViewHolder(@NonNull View itemView) {
        super(itemView);
        //Search/RecipeItem
        recipe_name=itemView.findViewById(R.id.recipe_name);
        recipe_image=(itemView.findViewById(R.id.recipe_image));
        recipeItem=(itemView.findViewById(R.id.recipeItem));
        recipe_time=(itemView.findViewById(R.id.recipe_time));
        recipe_diet=(itemView.findViewById(R.id.recipe_diet));
        diet_show=(itemView.findViewById(R.id.dietshow));
        time_show=(itemView.findViewById(R.id.timeshow));
        favourite=(itemView.findViewById(R.id.favouritebtn));
        removeRecipe=(itemView.findViewById(R.id.removeRecipeBtn));
        //removableItem
        remove=(itemView.findViewById(R.id.remove));
        textView2=itemView.findViewById(R.id.textView2);
        //Users/User Cell
        userImage=(itemView.findViewById(R.id.userImage));
        userName=(itemView.findViewById(R.id.userName));
        share=(itemView.findViewById(R.id.share));

    }
}
