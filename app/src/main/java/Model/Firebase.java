package Model;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayoutStates;

import com.bienhuels.iwmb_cookdome.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import View.MainActivity;
import View.LoginActivity;
import View.RecipeViewActivity;
import View.RecyclerViewHolder;
import Viewmodel.CustomComparator;

public class Firebase implements DatabaseInterface {
    ArrayList<Recipe> recipes=new ArrayList<>();
    ArrayList<User>users=new ArrayList<>();
    FirebaseDatabase database=FirebaseDatabase.getInstance();
    DatabaseReference userRef=database.getReference("/Cookdome/Users");
    DatabaseReference recipeRef=database.getReference("/Cookdome/Recipes");
    StorageReference storageRef= FirebaseStorage.getInstance().getReference().child("Images");
    FirebaseAuth auth=FirebaseAuth.getInstance();
    int i;
    User user=new User();
    Recipe recipe=new Recipe();

//Recipe-Step Images
    @Override
public void uploadStepMedia(Uri media, Thread thread, Step step, ProgressBar progressBar) {
    progressBar.setVisibility(View.VISIBLE);
    storageRef.child(user.getId()).child("StepImages").child(media.getLastPathSegment()).putFile(media).addOnSuccessListener(taskSnapshot -> {
        Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
        while(!uriTask.isComplete());
        step.media = uriTask.getResult().toString();
        progressBar.setVisibility(View.GONE);
        thread.start();
    });
}

//Recipes
@Override
    public void saveRecipe(Uri imageUri, String recipeName, String category, int time, int portions, ArrayList<Ingredient> ingredientList, ArrayList<Step> stepList, ArrayList<String> dietaryRecList, Context context, Handler handler, Boolean priv, String owner, FirebaseUser fbuser){
        Runnable uploadRunnable= (() -> {
            String key;
            if(recipe.getKey()==null){
                key=recipeRef.push().getKey();
                recipe.setKey(key);
            }else{
                key=recipe.getKey();
            }
            String uid=returnID(fbuser,context);
            if(!priv){
                recipeRef.child(key).setValue(recipe).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        handler.post(() -> Toast.makeText(context,R.string.uploadSuccess,Toast.LENGTH_SHORT).show());
                        addToOwn(context,uid,key,handler);
                    }
                }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show()));
            }else{
                userRef.child(uid).child("Privates").child(key).setValue(recipe).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        handler.post(() -> Toast.makeText(context,R.string.uploadSuccess,Toast.LENGTH_SHORT).show());
                        addToOwn(context,uid,key,handler);
                    }
                });
                removePublicRecipe(key,context,handler);
                if(recipe.getSharedWith()!=null){
                    for(String uID:recipe.getSharedWith()){
                        unshareRecipe(key,uID,false,context,handler);
                        recipeRef.child(key).child("sharedWith").child(uID).removeValue().addOnCompleteListener(task -> {
                            ArrayList<String> list=recipe.getSharedWith();
                            list.remove(uID);
                            recipe.setSharedWith(list);
                        }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show()));}}
            }});
        Thread uploadThread=new Thread(uploadRunnable);
        Runnable setRun= (() -> {
            if(imageUri!=null){
                storageRef.child(fbuser.getUid()).child("RecipeImages").child(imageUri.getLastPathSegment()).putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                    while(!uriTask.isComplete());
                    recipe.setImage(uriTask.getResult().toString());
                    recipe.setRecipeName(recipeName);
                    recipe.setCategory(category);
                    recipe.setPrepTime(time);
                    recipe.setPortions(portions);
                    recipe.setDietaryRec(dietaryRecList);
                    recipe.setIngredientList(ingredientList);
                    recipe.setStepList(stepList);
                    recipe.setPriv(priv);
                    recipe.setOwner(owner);
                    uploadThread.start();
                }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show()));
            }else{
                recipe.setRecipeName(recipeName);
                recipe.setCategory(category);
                recipe.setPrepTime(time);
                recipe.setPortions(portions);
                recipe.setDietaryRec(dietaryRecList);
                recipe.setIngredientList(ingredientList);
                recipe.setStepList(stepList);
                recipe.setPriv(priv);
                recipe.setOwner(owner);
                uploadThread.start();
            }});
        Thread setThread=new Thread(setRun);
        setThread.start();
    }

    @Override
    public void downloadRecipe(String key, Context context, Handler handler, Thread setDatathread, FirebaseUser fbUser){
        recipeRef.child(key).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    rebuildRecipe(snapshot);
                    setDatathread.start();
                } else {
                    String id=returnID(fbUser,context);
                    userRef.child(id).child("Privates").child(key).get().addOnCompleteListener(task1 -> {
                        if (task1.getResult().exists()) {
                            DataSnapshot snapshot = task1.getResult();
                            rebuildRecipe(snapshot);
                            setDatathread.start();
                        }else{
                            handler.post(() -> {
                                Toast.makeText(context, R.string.sthWrong, Toast.LENGTH_SHORT).show();
                                Intent toMainIntent=new Intent(context, MainActivity.class);
                                toMainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                context.startActivity(toMainIntent);
                            });
                        }
                    }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show()));
                }
            }
        }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show()));
    }
    @Override
    public Recipe returnRecipe(){
        return recipe;
    }
    @Override
    public void sharepublicRecipe(String userID, String key, Context context){
        ArrayList<String>sharedWith=recipe.getSharedWith();
        sharedWith.add(userID);
        recipe.setSharedWith(sharedWith);
        recipeRef.child(key).child("sharedWith").child(userID).setValue(userID).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Toast.makeText(context, R.string.shared, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show());
    }
    @Override
    public void sharePrivateRecipe(String userID, String key, String owner, Context context){
        ArrayList<String>sharedWith=recipe.getSharedWith();
        sharedWith.add(userID);
        recipe.setSharedWith(sharedWith);
        userRef.child(owner).child("Privates").child(key).child("sharedWith").child(userID).setValue(userID).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Toast.makeText(context, R.string.shared, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show());
    }
    @Override
    public void removeUserFromShareList(String userID, String key, Context context, String owner, Boolean priv, Handler handler, Thread nextThread){
        if(priv){
            userRef.child(owner).child("Privates").child(key).child("sharedWith").child(userID).removeValue().addOnCompleteListener(task -> {
                nextThread.start();
            }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show()));
        }else{
            recipeRef.child(key).child("sharedWith").child(userID).removeValue().addOnCompleteListener(task -> {
                nextThread.start();
            }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show()));
        }
        ArrayList<String>sharedWith=recipe.getSharedWith();
        sharedWith.remove(userID);
        recipe.setSharedWith(sharedWith);
    }


    //Mapping the firebase Data structure of a Recipe back to a recipe-Object
    @Override
    public Recipe rebuildRecipe(DataSnapshot snapshot) {
        Recipe selectedRecipe=new Recipe();
        selectedRecipe.setKey(snapshot.child("key").getValue(String.class));
        selectedRecipe.setRecipeName(String.valueOf(snapshot.child("recipeName").getValue()));
        selectedRecipe.setCategory(String.valueOf(snapshot.child("category").getValue()));
        selectedRecipe.setPrepTime(Integer.parseInt(String.valueOf(snapshot.child("prepTime").getValue()))) ;
        selectedRecipe.setPortions(Integer.parseInt(String.valueOf(snapshot.child("portions").getValue())));
        selectedRecipe.setImage(snapshot.child("image").getValue(String.class));
        selectedRecipe.setPriv(snapshot.child("priv").getValue(Boolean.class));
        selectedRecipe.setOwner(snapshot.child("owner").getValue(String.class));
        String index="0";
        ArrayList<Step>stepList= new ArrayList<>();
        for(DataSnapshot stepSS:snapshot.child("stepList").getChildren()){
            Step stepTry=new Step (String.valueOf(snapshot.child("stepList").child(index).child("step").getValue()));
            if(snapshot.child("stepList").child(index).hasChild("media")){
                stepTry.setMedia(String.valueOf(snapshot.child("stepList").child(index).child("media").getValue(String.class)));
            }
            stepList.add(stepTry);
            selectedRecipe.setStepList(stepList);
            int i=Integer.parseInt(index);
            i++;
            index= Integer.toString(i);
        }
        String index2="0";
        ArrayList<String>dietaryRec=new ArrayList<>();
        for(DataSnapshot sS:snapshot.child("dietaryRec").getChildren()){
            String dietTry=String.valueOf(snapshot.child("dietaryRec").child(index2).getValue());
            int i=Integer.parseInt(index2);
            i++;
            index2= Integer.toString(i);
            dietaryRec.add(dietTry);
            selectedRecipe.setDietaryRec(dietaryRec);
        }
        ArrayList<Ingredient>ingredientList=new ArrayList<>();
        for(DataSnapshot IngSS:snapshot.child("ingredientList").getChildren()){
            Double amount;
            if(IngSS.child("amount").getValue(Double.class)!=null){
                amount=IngSS.child("amount").getValue(Double.class);
            }else{
                amount=0.0;
            }
            String unit=IngSS.child("unit").getValue(String.class);
            String ingredientName=IngSS.child("ingredientName").getValue(String.class);
            if (amount == null) { amount=0.0;}

            Ingredient ingredient=new Ingredient(amount,unit,ingredientName);
            ingredientList.add(ingredient);
            selectedRecipe.setIngredientList(ingredientList);
        }
        ArrayList<String>sharedWith=new ArrayList<>();
        for(DataSnapshot ss:snapshot.child("sharedWith").getChildren()){
            String userID=ss.getValue(String.class);
            sharedWith.add(userID);
            selectedRecipe.setSharedWith(sharedWith);
        }
        recipe=selectedRecipe;
        return selectedRecipe;
    }
//User
@Override
public String returnID(FirebaseUser fbuser, Context context){
    if(fbuser!=null){
        user.setID(fbuser.getUid());
    }else{
        login(context);
    }
    return user.getId();
}
    @Override
public synchronized void uploadUser(Uri imageUri, String name, String email, String password, Context context, Handler handler, ProgressBar progressBar){
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener( task -> {
            if (task.isSuccessful()) {
                auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task12 -> {
                    if(task12.isSuccessful()){
                        while(!task12.isComplete());
                        FirebaseUser fbuser=auth.getCurrentUser();
                        user.setID(returnID(fbuser,context));
                        auth.signOut();
                        storageRef.child(user.getId()).child("UserImages").child(imageUri.getLastPathSegment()).putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                            Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                            while(!uriTask.isComplete());
                            Uri imageUriNew= uriTask.getResult();
                            user.setPhoto(imageUriNew.toString());
                            user.setName(name.toLowerCase());
                            Map<String, Object> users = new HashMap<>();
                            users.put("name",   name);
                            users.put("photo",user.getPhoto());
                            users.put("id",user.getId());
                            userRef.child(user.getId()).setValue(users).addOnCompleteListener(task1 -> {
                                if(task1.isSuccessful()){
                                    handler.post(() -> {
                                        Toast.makeText(context,R.string.uploadSuccess,Toast.LENGTH_SHORT).show();
                                        //dBUser.sendEmailVerification();
                                        Intent toLoginIntent=new Intent(context, LoginActivity.class);
                                        toLoginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                        toLoginIntent.putExtra("email",email);
                                        context.startActivity(toLoginIntent);
                                    });
                                }else{
                                    Log.d("TAG", "failed ");
                                }
                            }).addOnFailureListener(e -> {handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show());
                            progressBar.setVisibility(View.GONE);});

                        }).addOnFailureListener(e -> {handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show());
                            progressBar.setVisibility(View.GONE);});
                    }
                }).addOnFailureListener(e -> {handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show());
                    progressBar.setVisibility(View.GONE);});
            }
        }).addOnFailureListener(e -> {handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show());
            progressBar.setVisibility(View.GONE);});

}

    @Override
    public void downloadUser(Context context, FirebaseUser fbuser, Handler userHandler, Thread nextThread){
        if(fbuser!=null) {
            user.setEmail(fbuser.getEmail());
            user.setID(returnID(fbuser,context));
            userRef.child(user.getId()).get().addOnCompleteListener(task -> {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    user.setName(snapshot.child("name").getValue(String.class));
                    user.setPhoto(snapshot.child("photo").getValue(String.class));
                    nextThread.start();
                } else {

                    userHandler.post(() -> {
                        Toast.makeText(context, R.string.noMatch, Toast.LENGTH_SHORT).show();
                        Intent toLoginIntent=new Intent(context, LoginActivity.class);
                        toLoginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(toLoginIntent);
                    });
                }
            }).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());

        }else{

            userHandler.post(() -> {
                Toast.makeText(context, R.string.signedOut, Toast.LENGTH_SHORT).show();
                Intent toLoginIntent=new Intent(context, LoginActivity.class);
                toLoginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(toLoginIntent);
            });
        }
    }
    @Override
    public  User  getUser(){
        return user;
    }
    @Override
    public void update(FirebaseUser fbuser, String newname, String newemail, String newpass, Uri imageUri, Context context, String email, String password) {
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task1 -> {
            if(task1.isSuccessful()){
                user.setID(returnID(fbuser,context));
                Intent toMainIntent=new Intent(context, MainActivity.class);
                toMainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                HashMap<String,Object> update=new HashMap<>();
                if(newname!=null){
                    if(!newname.equals("")){
                        update.put("name",newname);}
                }
                if(imageUri!=null){
                    update.put("photo",imageUri.toString());}
                //If name and image arent changed (because they are stored in User dataclass)
                if(update.size()==0){
                    if (newemail != null) {
                        if (!newemail.equals("")) {
                            fbuser.updateEmail(newemail).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(context, R.string.updateSuccess, Toast.LENGTH_SHORT).show();
                                }else { context.startActivity(toMainIntent);}
                            }).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
                        }}
                    if(newpass!=null){
                        if(!newpass.equals("")) {
                            if (newpass.length()<8) {
                                Toast.makeText(context, R.string.passwordLength, Toast.LENGTH_SHORT).show();}
                            else{
                                fbuser.updatePassword(newpass).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, R.string.updateSuccess, Toast.LENGTH_SHORT).show();
                                        context.startActivity(toMainIntent);
                                    }
                                }).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());}
                        }}
                    context.startActivity(toMainIntent);

                }else{
                    userRef.child(user.getId()).updateChildren(update).addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            if (newemail != null) {
                                fbuser.updateEmail(newemail).addOnCompleteListener(task2 -> Toast.makeText(context, R.string.mailUpdated, Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
                            }
                            if(newpass!=null){
                                fbuser.updatePassword(newpass);}

                            context.startActivity(toMainIntent);
                        }else{
                            Toast.makeText(context, R.string.sthWrong, Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());}
            }else{
                Toast.makeText(context, R.string.sthWrong, Toast.LENGTH_SHORT).show();
                try{
                    throw Objects.requireNonNull(task1.getException());
                }catch (FirebaseAuthInvalidCredentialsException e){
                    Toast.makeText(context, R.string.invalidCred, Toast.LENGTH_SHORT).show();
                }catch (FirebaseAuthInvalidUserException e){
                    Toast.makeText(context, R.string.UserNotExist, Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    @Override
    //Generate a list of Keys to the Recipes the user liked
    public synchronized void getFavourites(Context context, FirebaseUser fbuser, Handler handler, Thread thread) {
        user.setID(returnID(fbuser,context));
        userRef.child(user.getId()).child("Favourites").get().addOnCompleteListener(task -> {
            if (task.getResult().exists()) {
                DataSnapshot snapshot = task.getResult();
                ArrayList<String>favourites = new ArrayList<>();
                for (DataSnapshot dsS : snapshot.getChildren()) {
                    String favkey = dsS.getKey();
                    favourites.add(favkey);
                    user.setFavourites(favourites);
                }
                thread.start();
            }else{
                ArrayList<String>favourites=new ArrayList<>();
                user.setFavourites(favourites);
                thread.start();
            }
        }).addOnFailureListener(e -> handler.post(() -> {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            Intent toMainIntent=new Intent(context,MainActivity.class);
            toMainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(toMainIntent);
        }));

    }
    @Override
    public synchronized void updateFavourites(Recipe recipe, Context context, ImageView favView, FirebaseUser fbuser, Handler handler){
        ArrayList<String>favourites=user.getFavourites();
        user.setID(returnID(fbuser,context));
        if (favourites.contains(recipe.getKey())) {
            userRef.child(user.id).child("Favourites").child(recipe.getKey()).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    favourites.remove(recipe.getKey());
                    user.setFavourites(favourites);
                    handler.post(() -> {
                        favView.setImageResource(R.drawable.unliked);
                        Toast.makeText(context, R.string.removed, Toast.LENGTH_SHORT).show();
                    });

                } else { handler.post(() -> Toast.makeText(context, R.string.sthWrong, Toast.LENGTH_SHORT).show());
                }
            }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show()));
        }else{
            userRef.child(user.id).child("Favourites").child(recipe.getKey()).setValue(recipe.getKey()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    favourites.add(recipe.getKey());
                    user.setFavourites(favourites);
                    handler.post(() -> {
                        favView.setImageResource(R.drawable.liked);
                        Toast.makeText(context, R.string.added, Toast.LENGTH_SHORT).show();
                    });

                } else {
                    handler.post(() -> Toast.makeText(context, R.string.added, Toast.LENGTH_SHORT).show());
                }
            }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show()));
        }
    }
    //Creating a list of Keys to the Recipes the user created themselves
    @Override
    public synchronized void setOwnList(Context context, FirebaseUser currentUser, Handler handler, Thread thread){
        String id= returnID(currentUser,context);
        userRef.child(id).child("Own").get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                DataSnapshot snapshot=task.getResult();
                ArrayList<String>own =new ArrayList<>();
                for(DataSnapshot ss:snapshot.getChildren()){
                    String key1 =ss.getKey();
                    own.add(key1);
                    if(own.size()== snapshot.getChildrenCount()-1){
                        user.setOwn(own);
                        thread.start();
                    }
                }
            }
        }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show()));
    }
    @Override
    public void addToOwn(Context context, String uid, String key, Handler handler){
        userRef.child(uid).child("Own").child(key).setValue(key).addOnCompleteListener(task1 -> {
            if(task1.isSuccessful()){
                handler.post(() -> {Intent toRecipeViewIntent=new Intent(context, RecipeViewActivity.class);
                    toRecipeViewIntent.putExtra("key",key);
                    toRecipeViewIntent.putExtra("fromCreate",0);
                    toRecipeViewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.getApplicationContext().startActivity(toRecipeViewIntent);  });}
            else{
                Log.d(TAG, "failed");
            }
        }).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    @Override
    public void removeRecipe(Recipe recipe, Context context, Handler handler, FirebaseUser fbuser){
        String uid= returnID(fbuser,context);
        String key=recipe.getKey();
        for(String uID:recipe.getSharedWith()){
            unshareRecipe(key,uID,recipe.getPriv(),context,handler);
        }
        userRef.child(uid).child("Own").child(key).removeValue().addOnCompleteListener(task -> {
            if(recipe.getPriv()){
                userRef.child("Privates").child(key).removeValue().addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful()){
                        handler.post(() -> {  Toast.makeText(context, R.string.deletSuccess, Toast.LENGTH_SHORT).show();
                            Intent toMainIntent=new Intent(context, MainActivity.class);
                            toMainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            context.startActivity(toMainIntent); });
                    }
                }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show()));
            }
            else{
                Firebase database=new Firebase();
                database.removePublicRecipe(key,context,handler);
            }
        });
    }
    @Override
    public synchronized void addToShoppingList(Context context, FirebaseUser fbuser, Handler handler, Thread thread, Ingredient ingredient){
        String uID= returnID(fbuser,context);
        userRef.child(uID).child("Shoppinglist").child((ingredient.getName())+":"+ingredient.getUnit()).get().addOnCompleteListener(task -> {
            if (task.getResult().exists()) {
                DataSnapshot snapshot = task.getResult();
                Double amount=snapshot.child("amount").getValue(Double.class);
                String unit=snapshot.child("unit").getValue(String.class);
                String name=snapshot.child("ingredientName").getValue(String.class);
                if (amount != null) {
                    amount += ingredient.getAmount();}else{
                    amount=ingredient.getAmount();
                }
                Ingredient updatedIngredient=new Ingredient(amount,unit,name);
                userRef.child(uID).child("Shoppinglist").child((ingredient.getName())+":"+ingredient.getUnit()).setValue(updatedIngredient).addOnCompleteListener(task12 -> {
                    if(task12.isSuccessful()){thread.start();
                    }
                }).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());

            }else{
                userRef.child(uID).child("Shoppinglist").child((ingredient.getName())+":"+ingredient.getUnit()).setValue(ingredient).addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful()){
                        thread.start();
                        handler.post(() -> Toast.makeText(context, R.string.added, Toast.LENGTH_SHORT).show());
                    }
                }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show()));

            }
        });
    }
    @Override
    public synchronized void removeIngredientFromShoppingList(FirebaseUser user, Context context, Ingredient ingredient, Handler handler){
        String uID= returnID(user,context);
        userRef.child(uID).child("Shoppinglist").child((ingredient.getName())+":"+ingredient.getUnit()).removeValue().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                handler.post(() -> Toast.makeText(context, R.string.removed, Toast.LENGTH_SHORT).show());
            }
        }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show()));
    }
    @Override
    public synchronized ArrayList<Ingredient>getShoppingList(Context context, String uID, Thread thread){
        userRef.child(uID).child("Shoppinglist").get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                DataSnapshot snapshot=task.getResult();
                for(DataSnapshot IngSS:snapshot.getChildren()){
                    Double amount=IngSS.child("amount").getValue(Double.class);
                    String unit=IngSS.child("unit").getValue(String.class);
                    String ingredientName=IngSS.child("ingredientName").getValue(String.class);
                    if (amount != null) {
                        Ingredient ingredient=new Ingredient(amount,unit,ingredientName);
                        ArrayList<Ingredient>shoppingList=user.getShoppingList();
                        shoppingList.add(ingredient);
                        user.setShoppingList(shoppingList);
                    }else{
                        amount=0.0;
                        Ingredient ingredient=new Ingredient(amount,unit,ingredientName);
                        ArrayList<Ingredient>shoppingList=user.getShoppingList();
                        shoppingList.add(ingredient);
                        user.setShoppingList(shoppingList);
                    }
                }
            } thread.start();
        }).addOnFailureListener(e -> {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            thread.start();
        });
        return user.shoppingList;
    }
    @Override
    public  void unshareRecipe(String key, String id, Boolean priv, Context context, Handler handler){
        String privString;
        if(priv){
            privString="private";
        }else{
            privString="public";
        }
        userRef.child(id).child("Shared").child(privString).child(key).removeValue().addOnCompleteListener(task -> {
        }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show()));
    }
    @Override
    public void shareRecipe(Recipe recipe, User model, Context context, String uID){
        if(!recipe.getPriv()){
            userRef.child(model.getId()).child("Shared").child("public").child(recipe.getKey()).setValue(recipe.getKey()).addOnCompleteListener(task -> {
                sharepublicRecipe(model.getId(),recipe.getKey(),context);
                Intent torecipeViewIntent=new Intent(context, RecipeViewActivity.class);
                torecipeViewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                torecipeViewIntent.putExtra("key",recipe.getKey());
            }).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show());
        }else{
            boolean own=uID.equals(recipe.getOwner());
            if(own){
                userRef.child(model.getId()).child("Shared").child("private").child(recipe.getKey()).setValue(uID).addOnCompleteListener(task -> {
                    sharePrivateRecipe(model.getId(), recipe.getKey(),recipe.getOwner(), context);
                    Intent torecipeViewIntent=new Intent(context, RecipeViewActivity.class);
                    torecipeViewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    torecipeViewIntent.putExtra("key",recipe.getKey());
                    context.startActivity(torecipeViewIntent);
                }).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show());
            }else{
                Toast.makeText(context, R.string.cantShare, Toast.LENGTH_LONG).show();
                Intent torecipeViewIntent=new Intent(context, RecipeViewActivity.class);
                torecipeViewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                torecipeViewIntent.putExtra("key",recipe.getKey());
                context.startActivity(torecipeViewIntent);
            }
        }
    }

    public User rebuildUser(DataSnapshot snapshot){
        user.setPhoto(Objects.requireNonNull(snapshot.child("name").getValue(String.class)).toLowerCase());
        user.setName(snapshot.child("photo").getValue(String.class));
        user.setID(snapshot.child("id").getValue(String.class));
        return user;
    }

    @Override
    public void login(Context context){
        Toast.makeText(context, R.string.signedOut, Toast.LENGTH_SHORT).show();
        Intent loginIntent = new Intent(context, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(loginIntent);
    }

    @Override
    public synchronized ArrayList<Recipe> getAllRecipes(Context context, Handler handler, Thread thread) {
        recipeRef.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    for(DataSnapshot dsS:snapshot.getChildren()){
                        Recipe recipe= rebuildRecipe(dsS);
                        recipes.add(recipe);
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
    @Override
    public ArrayList<Recipe> getSelectedRecipes(String catFilter, String source, Context context, Intent previousIntent,Handler handler,Thread thread){
        recipeRef.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    for(DataSnapshot dsS:snapshot.getChildren()){
                        recipe= rebuildRecipe(dsS);
                        if(source.equals("categories")){
                            if (recipe.getCategory().equals(catFilter)) {
                                recipes.add(recipe);
                            }
                        }
                        if(source.equals("leftovers")){
                            ArrayList<String> leftoverList=previousIntent.getStringArrayListExtra("action");
                            ArrayList<String>ingredientStringList=new ArrayList<>();
                            for(Ingredient ingredient:recipe.getIngredientList()){
                                String name=ingredient.getName();
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

                            recipes.add(recipe);
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
    @Override
    public ArrayList<Recipe> getFavouriteOrOwnRecipes(ArrayList<String> keylist, Context context, String id, Handler handler,Thread thread){
        i=keylist.size();
        for(String key:keylist){
            recipeRef.child(key).get().addOnCompleteListener(task -> {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    recipe= rebuildRecipe(snapshot);
                    recipes.add(recipe);
                       }else{
                    userRef.child(id).child("Privates").child(key).get().addOnCompleteListener(task1 -> {
                        if (task1.getResult().exists()) {
                            DataSnapshot snapshot2 = task1.getResult();
                            recipe= rebuildRecipe(snapshot2);
                            recipes.add(recipe);
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

@Override
    public void removePublicRecipe(String key, Context context, Handler handler){
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
                        .setQuery(query, this::rebuildUser)
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
                holder.share.setOnClickListener(view -> shareRecipe(recipe,model,context,uID));
            }
            @NonNull
            @Override
            public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_user,parent,false);
                return new RecyclerViewHolder(view);
            }
        };
    }

    public void downloadSharedPrivRecipes(Context context, FirebaseUser fbuser, Handler handler, Thread nextThread){
        String id=returnID(fbuser, context);
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
                            recipe= rebuildRecipe(snapshot1);
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
@Override
    public void downloadSharedPublRecipes(Context context, FirebaseUser fbuser, Handler handler, Thread nextThread){
        String id=returnID(fbuser, context);
        userRef.child(id).child("Shared").child("public").get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                DataSnapshot snapshot= task.getResult();
            if(snapshot.exists()){
                if(snapshot.hasChildren()) {
                    i = 1;
                    int size = Integer.parseInt(String.valueOf(snapshot.getChildrenCount()));
                    for (DataSnapshot ss : snapshot.getChildren()) {
                        String key = ss.getValue(String.class);
                        if (key != null) {
                            recipeRef.child(key).get().addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    DataSnapshot snapshot1 = task1.getResult();
                                    recipe = rebuildRecipe(snapshot1);
                                    recipes.add(recipe);
                                    if (i == size) {
                                        downloadSharedPrivRecipes(context,fbuser,handler,nextThread);
                                    }
                                    i++;
                                }
                            }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show()));
                        } else {
                            handler.post(() -> Toast.makeText(context, R.string.sthWrong, Toast.LENGTH_SHORT).show());
                        }
                    }
                }}}else{
                downloadSharedPrivRecipes(context,fbuser,handler,nextThread);
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
                user=rebuildUser(snapshot);
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
