package Model;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bienhuels.iwmb_cookdome.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import View.LoginActivity;
import View.MainActivity;
import View.RecipeViewActivity;

public class User {
    String name,photo,email,id;
    ArrayList<String> favourites;
    ArrayList<String> own;
    ArrayList<Ingredient>shoppingList=new ArrayList<>();
    DatabaseReference userRef=FirebaseDatabase.getInstance().getReference("/Cookdome/Users");
    FirebaseAuth auth=FirebaseAuth.getInstance();
    User user;




    public User(){}

    public User(String name, String photo,String id) {
        this.name = name.toLowerCase();
        this.photo = photo;
        this.id=id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getId(){return id;}

    public void setId(String id){
        this.id=id;
    }
    public String getPhoto() {
        return photo;
    }

    public synchronized void uploadToFirebase(Uri imageUri,String name,String email,String password,Context context,Handler handler){
        StorageReference storageRef= FirebaseStorage.getInstance().getReference().child("UserImages");
        storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
            while(!uriTask.isComplete());
            Uri imageUriNew= uriTask.getResult();
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener( task -> {
                        if (task.isSuccessful()) {
                            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task12 -> {
                                FirebaseUser fbuser=auth.getCurrentUser();
                                id=getUID(fbuser,context);
                                auth.signOut();
                                User user=new User(name.toLowerCase(),imageUriNew.toString(),id);
                                userRef.child(id).setValue(user).addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful()){
                                        handler.post(() -> {
                                            Toast.makeText(context,R.string.uploadSuccess,Toast.LENGTH_SHORT).show();
                                            //dBUser.sendEmailVerification();
                                            Intent toLoginIntent=new Intent(context, LoginActivity.class);
                                            toLoginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                            toLoginIntent.putExtra("email",email);
                                            context.startActivity(toLoginIntent);
                                        });
                                    }
                                }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show()));
                            });
                        }
            }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show()));
        }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show()));
    }


    public User downloadFromFirebase(Context context, FirebaseUser fbuser, TextView nameView, TextView emailView, ImageView imageView, Handler userHandler){
        if(fbuser!=null) {
            email = fbuser.getEmail();
            id = getUID(fbuser,context);
            userRef.child(id).get().addOnCompleteListener(task -> {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    name = snapshot.child("name").getValue(String.class);
                    photo = snapshot.child("photo").getValue(String.class);
                    user=new User(name,photo,email);
                    userHandler.post(() -> {
                        Picasso.get()
                                .load(user.getPhoto())
                                .placeholder(R.drawable.camera)
                                .resize(400, 400)
                                .centerCrop()
                                .into(imageView);
                        nameView.setText(user.getName());
                        emailView.setText(fbuser.getEmail());
                    });
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
        return user;
    }
    public void updateOnFirebase(FirebaseUser fbuser, String newname, String newemail, String newpass, Uri imageUri, Context context, String email, String password) {
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task1 -> {
            if(task1.isSuccessful()){
                    id=getUID(fbuser,context);
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
                        userRef.child(id).updateChildren(update).addOnCompleteListener(task -> {
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
    //Generate a list of Keys to the Recipes the user liked
    public synchronized ArrayList<String> getFavourites(Context context, FirebaseUser currentUser, Handler handler,Thread thread) {
            id =getUID(currentUser,context);
            favourites = new ArrayList<>();
            userRef.child(id).child("Favourites").get().addOnCompleteListener(task -> {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    for (DataSnapshot dsS : snapshot.getChildren()) {
                        String favkey = dsS.getKey();
                        favourites.add(favkey);
                    }
                    synchronized (thread){
                        thread.notify();
                    }
                    //checkFav(favView,key,handler);
                }else{
                    favourites=new ArrayList<>();
                    synchronized (thread){
                        thread.notify();
                    }
                }
            }).addOnFailureListener(e -> handler.post(() -> {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                Intent toMainIntent=new Intent(context,MainActivity.class);
                toMainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(toMainIntent);
            }));
        return favourites;
    }

    public synchronized ArrayList<String> updateFavourites(Recipe recipe, Context context, ImageView favView, FirebaseUser currentUser, Handler handler,ArrayList<String>favourites){
        id=getUID(currentUser,context);
        if (favourites.contains(recipe.getKey())) {
            userRef.child(id).child("Favourites").child(recipe.getKey()).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    favourites.remove(recipe.getKey());
                    handler.post(() -> {
                        favView.setImageResource(R.drawable.unliked);
                        Toast.makeText(context, R.string.removed, Toast.LENGTH_SHORT).show();
                    });

                } else { handler.post(() -> Toast.makeText(context, R.string.sthWrong, Toast.LENGTH_SHORT).show());
                }
            }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show()));
        }else{
            userRef.child(id).child("Favourites").child(recipe.getKey()).setValue(recipe.getKey()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    favourites.add(recipe.getKey());
                    handler.post(() -> {
                        favView.setImageResource(R.drawable.liked);
                        Toast.makeText(context, R.string.added, Toast.LENGTH_SHORT).show();
                    });

                } else {
                    handler.post(() -> Toast.makeText(context, R.string.added, Toast.LENGTH_SHORT).show());
                }
            }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show()));
        }return favourites;
    }
    //Creating a list of Keys to the Recipes the user created themselves
    public synchronized ArrayList<String> getOwn(Context context, FirebaseUser currentUser, Handler handler,Thread thread){
        own =new ArrayList<>();
        String id=getUID(currentUser,context);
            userRef.child(id).child("Own").get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    DataSnapshot snapshot=task.getResult();
                    for(DataSnapshot ss:snapshot.getChildren()){
                        String key1 =ss.getKey();
                        own.add(key1);
                    } synchronized (thread){
                        thread.notify();
                    }
                }
            }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show()));
        return own;
    }
    public void addToOwn(Context context,String uid,String key,Handler handler){
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
    public void removeRecipe(Recipe recipe, Context context, Handler handler, FirebaseUser fbuser){
        String uid=getUID(fbuser,context);
        String key=recipe.getKey();
        String priv;
        if(recipe.getPriv()){
            priv="private";
        }else{
            priv="public";
        }
        for(String uID:recipe.getSharedWith()){
            removeFromShared(key,uID,priv,context,handler);
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
                Database database=new Database();
                database.removeFromPublicList(key,context,handler);
            }
        });


    }

    public String getUID(FirebaseUser fbuser,Context context){
        if(fbuser!=null){
            id=fbuser.getUid();
        }else{
            loginIntent(context);
        }
        return id;
    }




    public synchronized void addToShoppingList(Context context, FirebaseUser fbuser, Handler handler, Thread thread, Ingredient ingredient){
        String uID=getUID(fbuser,context);
        userRef.child(uID).child("Shoppinglist").child((ingredient.getIngredientName())+":"+ingredient.getUnit()).get().addOnCompleteListener(task -> {
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
                userRef.child(uID).child("Shoppinglist").child((ingredient.getIngredientName())+":"+ingredient.getUnit()).setValue(updatedIngredient).addOnCompleteListener(task12 -> {
                    if(task12.isSuccessful()){
                        synchronized (thread){
                            thread.notify();
                        }
                    }
                }).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());

            }else{
                userRef.child(uID).child("Shoppinglist").child((ingredient.getIngredientName())+":"+ingredient.getUnit()).setValue(ingredient).addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful()){
                        synchronized (thread){
                            thread.notify();
                        }
                    }
                }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show()));

            }
        });
    }
    public synchronized void removeFromShoppingList(FirebaseUser user,Context context,Ingredient ingredient,Handler handler){
        String uID=getUID(user,context);
            userRef.child(uID).child("Shoppinglist").child((ingredient.getIngredientName())+":"+ingredient.getUnit()).removeValue().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    handler.post(() -> Toast.makeText(context, R.string.removed, Toast.LENGTH_SHORT).show());
                }
            }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show()));
    }
    public synchronized ArrayList<Ingredient>getShoppingList(Context context,String uID,Thread thread){
        userRef.child(uID).child("Shoppinglist").get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                DataSnapshot snapshot=task.getResult();
                for(DataSnapshot IngSS:snapshot.getChildren()){
                    Double amount=IngSS.child("amount").getValue(Double.class);
                    String unit=IngSS.child("unit").getValue(String.class);
                    String ingredientName=IngSS.child("ingredientName").getValue(String.class);
                    if (amount != null) {
                        Ingredient ingredient=new Ingredient(amount,unit,ingredientName);
                        shoppingList.add(ingredient);
                    }else{
                        amount=0.0;
                        Ingredient ingredient=new Ingredient(amount,unit,ingredientName);
                        shoppingList.add(ingredient);
                    }
                    }
            } synchronized (thread){
                thread.notify();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            synchronized (thread){
                thread.notify();
            }
        });
        return shoppingList;
    }
    public  void removeFromShared(String key,String id,String priv,Context context,Handler handler){
        userRef.child(id).child("Shared").child(priv).child(key).removeValue().addOnCompleteListener(task -> {
        }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show()));
    }
    public void addToShared(Recipe recipe,User model,Context context,String uID){
        if(!recipe.getPriv()){
            userRef.child(model.getId()).child("Shared").child("public").child(recipe.getKey()).setValue(recipe.getKey()).addOnCompleteListener(task -> {
                recipe.addUserToSharedListPublic(model.getId(),recipe.getKey(),context);
                Intent torecipeViewIntent=new Intent(context, RecipeViewActivity.class);
                torecipeViewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                torecipeViewIntent.putExtra("key",recipe.getKey());
            }).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show());
        }else{
            boolean own=uID.equals(recipe.getOwner());
            if(own){
                userRef.child(model.getId()).child("Shared").child("private").child(recipe.getKey()).setValue(uID).addOnCompleteListener(task -> {
                    recipe.addUserToSharedListPrivate(model.getId(), recipe.getKey(),recipe.getOwner(), context);
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

    public void loginIntent(Context context){
        Toast.makeText(context, R.string.signedOut, Toast.LENGTH_SHORT).show();
        Intent loginIntent = new Intent(context, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(loginIntent);
    }
}
