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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import View.EditProfileActivity;
import View.LoginActivity;
import View.MainActivity;

public class User {
    String name,photo,email;
    FirebaseDatabase database=FirebaseDatabase.getInstance();
    DatabaseReference userRef=database.getReference("/Cookdome/Users");
    FirebaseAuth auth=FirebaseAuth.getInstance();
    User user;
    Handler userHandler=new Handler();


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


    public User getUserinfo(Context context,FirebaseUser fbuser,TextView nameView,TextView emailView,ImageView imageView){

        if(fbuser!=null) {
            String id = fbuser.getUid();
            email = fbuser.getEmail();
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
                    Handler toastHandler=new Handler();
                    toastHandler.post(new Runnable() {
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
            Handler toastHandler=new Handler();
            toastHandler.post(new Runnable() {
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
    public void setUserInfo(){

    }


    public void updateUser(FirebaseUser fbuser, String newname, String newemail, String newpass, Uri imageUri, Context context) {
        if(fbuser!=null){
            String id=fbuser.getUid();
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
    }
    public void loginUser(String email,String password,Context context){
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Toast.makeText(context, R.string.loginsuccess, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
            }else{
                Toast.makeText(context, R.string.sthWrong, Toast.LENGTH_SHORT).show();
                try{
                    throw Objects.requireNonNull(task.getException());
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
}
