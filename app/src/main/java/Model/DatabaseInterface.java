package Model;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public interface DatabaseInterface {

    ArrayList<Recipe> getAllRecipes(Context context, Handler handler, Thread thread);


    //retreive recipes from firebase that meet the source criteria
    ArrayList<Recipe> getSelectedRecipes(String catFilter, String source, Context context, Intent previousIntent, Handler handler, Thread thread);

    //Download and display a List of Recipes that the User either liked or created
    ArrayList<Recipe> getFavouriteOrOwnRecipes(ArrayList<String> keylist, Context context, String id, Handler handler, Thread thread);

    void removePublicRecipe(String key, Context context, Handler handler);

    void downloadSharedPublRecipes(Context context, FirebaseUser fbuser, Handler handler, Thread nextThread);
}
