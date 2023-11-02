package Model;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;

public interface DatabaseInterface {


    //Recipe-Step Images
    void uploadStepMedia(Uri media, Thread thread, Step step, ProgressBar progressBar);

    //Recipes
    void saveRecipe(Uri imageUri, String recipeName, String category, int time, int portions, ArrayList<Ingredient> ingredientList, ArrayList<Step> stepList, ArrayList<String> dietaryRecList, Context context, Handler handler, Boolean priv, String owner, FirebaseUser fbuser);

    void downloadRecipe(String key, Context context, Handler handler, Thread setDatathread, FirebaseUser fbUser);

    Recipe returnRecipe();

    void sharepublicRecipe(String userID, String key, Context context);

    void sharePrivateRecipe(String userID, String key, String owner, Context context);

    void removeUserFromShareList(String userID, String key, Context context, String owner, Boolean priv, Handler handler, Thread nextThread);

    //Mapping the firebase Data structure of a Recipe back to a recipe-Object
    Recipe rebuildRecipe(DataSnapshot snapshot);

    //User
    String returnID(FirebaseUser fbuser, Context context);

    void uploadUser(Uri imageUri, String name, String email, String password, Context context, Handler handler, ProgressBar progressBar);

    void downloadUser(Context context, FirebaseUser fbuser, Handler userHandler, Thread nextThread);

    User  getUser();

    void update(FirebaseUser fbuser, String newname, String newemail, String newpass, Uri imageUri, Context context, String email, String password);

    //Generate a list of Keys to the Recipes the user liked
    void getFavourites(Context context, FirebaseUser fbuser, Handler handler, Thread thread);

    void updateFavourites(Recipe recipe, Context context, ImageView favView, FirebaseUser fbuser, Handler handler);

    //Creating a list of Keys to the Recipes the user created themselves
    void setOwnList(Context context, FirebaseUser currentUser, Handler handler, Thread thread);

    void addToOwn(Context context, String uid, String key, Handler handler);

    void removeRecipe(Recipe recipe, Context context, Handler handler, FirebaseUser fbuser);

    void addToShoppingList(Context context, FirebaseUser fbuser, Handler handler, Thread thread, Ingredient ingredient);

    void removeIngredientFromShoppingList(FirebaseUser user, Context context, Ingredient ingredient, Handler handler);

    ArrayList<Ingredient>getShoppingList(Context context, String uID, Thread thread);

    void unshareRecipe(String key, String id, Boolean priv, Context context, Handler handler);

    void shareRecipe(Recipe recipe, User model, Context context, String uID);

    void login(Context context);

    ArrayList<Recipe> getAllRecipes(Context context, Handler handler, Thread thread);


    //retreive recipes from firebase that meet the source criteria
    ArrayList<Recipe> getSelectedRecipes(String catFilter, String source, Context context, Intent previousIntent, Handler handler, Thread thread);

    //Download and display a List of Recipes that the User either liked or created
    ArrayList<Recipe> getFavouriteOrOwnRecipes(ArrayList<String> keylist, Context context, String id, Handler handler, Thread thread);

    void removePublicRecipe(String key, Context context, Handler handler);

    void downloadSharedPublRecipes(Context context, FirebaseUser fbuser, Handler handler, Thread nextThread);
}
