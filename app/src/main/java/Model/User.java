package Model;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bienhuels.iwmb_cookdome.R
        ;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import View.LoginActivity;
import View.MainActivity;
import View.RecipeViewActivity;

public class User {
    String name,photo,email,id;
    ArrayList<String> favourites;
    ArrayList<String> own;
    ArrayList<Ingredient>shoppingList=new ArrayList<>();




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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId(){return id;}

    public void setID(String id){
        this.id=id;
    }
    public String getPhoto() {
        return photo;
    }
    public  void setPhoto(String url){
        this.photo=url;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<String> getFavourites() {
        return favourites;
    }

    public void setFavourites(ArrayList<String> favourites) {
        this.favourites = favourites;
    }

    public ArrayList<String> getOwn() {
        return own;
    }

    public void setOwn(ArrayList<String> own) {
        this.own = own;
    }

    public ArrayList<Ingredient> getShoppingList() {
        return shoppingList;
    }

    public void setShoppingList(ArrayList<Ingredient> shoppingList) {
        this.shoppingList = shoppingList;
    }
}
