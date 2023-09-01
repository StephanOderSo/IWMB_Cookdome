package Model;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bienhuels.iwmb_cookdome.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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

public class User {
    String name,photo,email;
    ArrayList<String> favourites, own;
    FirebaseDatabase database=FirebaseDatabase.getInstance();
    DatabaseReference userRef=database.getReference("/Cookdome/Users");
    FirebaseAuth auth=FirebaseAuth.getInstance();
    User user;



    public User(){}
    public User(String name, String photo) {
        this.name = name;
        this.photo = photo;
    }

    public User(String name, String photo,String email) {
        this.name = name;
        this.photo = photo;
        this.email=email;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getPhoto() {
        return photo;
    }

    public synchronized void uploadToFirebase(Uri imageUri,String name,String email,String password,Context context,Handler handler){
        StorageReference storageRef= FirebaseStorage.getInstance().getReference().child("UserImages").child(imageUri.getLastPathSegment());
        storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
            while(!uriTask.isComplete());
            Uri imageUriNew= uriTask.getResult();
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener( task -> {
                        if (task.isSuccessful()) {
                            User user=new User(name,imageUriNew.toString());
                            String userID=setUID(email);
                            userRef.child(userID).setValue(user).addOnCompleteListener(task1 -> {
                                if(task1.isSuccessful()){
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(context,R.string.uploadSuccess,Toast.LENGTH_SHORT).show();
                                            //dBUser.sendEmailVerification();
                                            Intent toLoginIntent=new Intent(context, LoginActivity.class);
                                            toLoginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                            toLoginIntent.putExtra("email",email);
                                            context.startActivity(toLoginIntent);
                                        }
                                    });
                                }
                            }).addOnFailureListener(e -> {handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();}});
                            });
                        }
            }).addOnFailureListener(e -> {handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();}});
            });
        }).addOnFailureListener(e -> {handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();}});
        });
    }


    public User getUserFromFirebase(Context context, FirebaseUser fbuser, TextView nameView, TextView emailView, ImageView imageView, Handler userHandler){

        if(fbuser!=null) {
            email = fbuser.getEmail();
            String id = getUID(fbuser);
            userRef.child(id).get().addOnCompleteListener(task -> {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    name = snapshot.child("name").getValue(String.class);
                    photo = snapshot.child("photo").getValue(String.class);
                    user=new User(name,photo,email);
                    userHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Picasso.get()
                                    .load(user.getPhoto())
                                    .placeholder(R.drawable.camera)
                                    .resize(400, 400)
                                    .centerCrop()
                                    .into(imageView);
                            nameView.setText(user.getName());
                            emailView.setText(user.getEmail());
                        }
                    });
                } else {

                    userHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, R.string.noMatch, Toast.LENGTH_SHORT).show();
                            Intent toLoginIntent=new Intent(context, LoginActivity.class);
                            toLoginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            context.startActivity(toLoginIntent);
                        }
                    });
                }
            }).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());

        }else{

            userHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, R.string.signedOut, Toast.LENGTH_SHORT).show();
                    Intent toLoginIntent=new Intent(context, LoginActivity.class);
                    toLoginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(toLoginIntent);
                }
            });
        }
        return user;
    }
    public void updateUserOnFirebase(FirebaseUser fbuser, String newname, String newemail, String newpass, Uri imageUri, Context context, String email, String password) {
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task1 -> {
            if(task1.isSuccessful()){
                Log.d("TAG", "login success");
                if(fbuser!=null){
                    String id=getUID(fbuser);
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
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.d("TAG", e.getMessage());
                                    }
                                });
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
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            Log.d("TAG", e.getMessage());
                                        }
                                    });}
                            }}
                        context.startActivity(toMainIntent);

                    }else{
                        userRef.child(id).updateChildren(update).addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                if (newemail != null) {
                                    fbuser.updateEmail(newemail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(context, R.string.mailUpdated, Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                                if(newpass!=null){
                                    fbuser.updatePassword(newpass);}

                                context.startActivity(toMainIntent);
                            }else{
                                Toast.makeText(context, R.string.sthWrong, Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());}
                }
                else{
                    Intent toLoginIntent=new Intent(context, LoginActivity.class);
                    context.startActivity(toLoginIntent);
                }
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
        //Send User to sign in if no current user found
        if (currentUser == null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, R.string.signedOut, Toast.LENGTH_SHORT).show();
                    Intent loginIntent = new Intent(context, LoginActivity.class);
                    loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(loginIntent);
                }
            });
//Otherwise use UserID to find liked Recipes and add their keys to the List favlist
        } else {
            String id =getUID(currentUser);
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
                    Log.d("TAG", "noFavourites yet");
                    favourites=new ArrayList<>();
                    synchronized (thread){
                        thread.notify();
                    }
                }
            }).addOnFailureListener(e -> {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                        Intent toMainIntent=new Intent(context,MainActivity.class);
                        toMainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(toMainIntent);
                    }
                });
            });
        }return favourites;
    }

    public synchronized ArrayList<String> updateFavourites(Recipe recipe, Context context, ImageView favView, FirebaseUser currentUser, Handler handler){
        String id=getUID(currentUser);
        if (favourites.contains(recipe.getKey())) {
            userRef.child(id).child("Favourites").child(recipe.getKey()).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    favourites.remove(recipe.getKey());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            favView.setImageResource(R.drawable.unliked);
                            Toast.makeText(context, R.string.removed, Toast.LENGTH_SHORT).show();
                        }});

                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, R.string.sthWrong, Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }).addOnFailureListener(e ->
                    {handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();}});
                    });
        }else{
            userRef.child(id).child("Favourites").child(recipe.getKey()).setValue(recipe.getKey()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    favourites.add(recipe.getKey());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            favView.setImageResource(R.drawable.liked);
                            Toast.makeText(context, R.string.added, Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, R.string.added, Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }).addOnFailureListener(e -> {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }return favourites;
    }
    //Creating a list of Keys to the Recipes the user created themselves
    public synchronized ArrayList<String> getOwn(Context context, FirebaseUser currentUser, Handler handler,Thread thread){
        own =new ArrayList<>();
        //Send to login if User not found
        if (currentUser == null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, R.string.signedOut, Toast.LENGTH_SHORT).show();
                    Intent loginIntent = new Intent(context, LoginActivity.class);
                    loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(loginIntent);
                }});
//If user found, use userID to create the List of Keys from Firebase
        } else {
            String id=getUID(currentUser);
            //In case of Issues with the download a case specific Error message is displayed to the user
            userRef.child(id).child("Own").get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    DataSnapshot snapshot=task.getResult();
                    for(DataSnapshot ss:snapshot.getChildren()){
                        String key1 =ss.getKey();
                        own.add(key1);
                    } synchronized (thread){
                        Log.d("TAG", own.toString());
                        thread.notify();
                    }
                }
            }).addOnFailureListener(e ->{
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                        }});});
        }return own;
    }
    public String setUID(String email){
        email=email.replace(".","*");
        email=email.replace("$","*");
        email=email.replace("[","*");
        email=email.replace("]","*");
        return email;
    }
    public String getUID(FirebaseUser fbuser){
        String email=fbuser.getEmail();
        email=email.replace(".","*");
        email=email.replace("$","*");
        email=email.replace("[","*");
        email=email.replace("]","*");
        return email;
    }
}
