package Model;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import com.bienhuels.iwmb_cookdome.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import View.MainActivity;

public class Recipe {
    String key;
    String image;
    String recipeName;
    int prepTime;
    ArrayList<Ingredient> ingredientList=new ArrayList<>();
    String category;
    Integer portions;
    ArrayList<Step> stepList=new ArrayList<Step>();
    ArrayList<String> dietaryRec =new ArrayList<>();
    ArrayList<String>sharedWith=new ArrayList<>();
    Boolean priv;
    String owner;




    public Recipe(){}


    public Recipe(String key, String image, String recipeName, String category, int prepTime, int portions, ArrayList<Ingredient> ingredientList, ArrayList<Step> stepList, ArrayList<String> dietRec, Boolean priv, String owner, ArrayList<String>sharedWith) {
       this.key=key;
        this.image=image;
        this.recipeName = recipeName;
        this.prepTime = prepTime;
        this.portions=portions;
        this.ingredientList = ingredientList;
        this.category = category;
        this.stepList = stepList;
        this.dietaryRec =dietRec;
        this.priv=priv;
        this.owner=owner;
        this.sharedWith=sharedWith;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public void setPrepTime(int prepTime) {
        this.prepTime = prepTime;
    }

    public void setIngredientList(ArrayList<Ingredient> ingredientList) {
        this.ingredientList = ingredientList;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPortions(Integer portions) {
        this.portions = portions;
    }

    public void setStepList(ArrayList<Step> stepList) {
        this.stepList = stepList;
    }

    public void setDietaryRec(ArrayList<String> dietaryRec) {
        this.dietaryRec = dietaryRec;
    }

    public void setSharedWith(ArrayList<String> sharedWith) {
        this.sharedWith = sharedWith;
    }

    public void setPriv(Boolean priv) {
        this.priv = priv;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
    public String getKey() {
        return key;
    }

    public Integer getPortions() {
        return portions;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getRecipeName() {
        return recipeName;
    }


    public int getPrepTime() {
        return prepTime;
    }


    public ArrayList<Ingredient> getIngredientList() {
        return ingredientList;
    }


    public String getCategory() {
        return category;
    }

    public ArrayList<Step> getStepList() {
        return stepList;
    }

    public ArrayList<String> getDietaryRec() {
        return dietaryRec;
    }
    public Boolean getPriv() {
        return priv;
    }

    public String getOwner() {
        return owner;
    }

    public ArrayList<String> getSharedWith() {
        return sharedWith;
    }




}

