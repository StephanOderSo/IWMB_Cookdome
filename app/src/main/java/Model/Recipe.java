package Model;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.bienhuels.iwmb_cookdome.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import View.LoginActivity;
import View.MainActivity;
import View.RecipeViewActivity;

public class Recipe {
    String key;

    private String image;
    private String recipeName;
    private int prepTime;
    private ArrayList<Ingredient> ingredientList;
    private String category;
    private Integer portions;
    private ArrayList<String> StepList;
    private ArrayList<String> dietaryRec;
    FirebaseDatabase database=FirebaseDatabase.getInstance();
    FirebaseAuth auth;
    DatabaseReference userRef=database.getReference("/Cookdome/Users");
    DatabaseReference recipeRef=database.getReference("/Cookdome/Recipes");


    public Recipe(){}


    public Recipe(String key, String image, String recipeName, String category, int prepTime, int portions, ArrayList<Ingredient> ingredientList, ArrayList<String> stepList, ArrayList<String> dietRec) {
       this.key=key;
        this.image=image;
        this.recipeName = recipeName;
        this.prepTime = prepTime;
        this.portions=portions;
        this.ingredientList = ingredientList;
        this.category = category;
        this.StepList = stepList;
        this.dietaryRec=dietRec;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String id) {
        this.key=key;
    }

    public Integer getPortions() {
        return portions;
    }

    public void setPortions(Integer portions) {
        this.portions = portions;
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

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public int getPrepTime() {
        return prepTime;
    }

    public void setPrepTime(int prepTime) {
        this.prepTime = prepTime;
    }

    public ArrayList<Ingredient> getIngredientList() {
        return ingredientList;
    }

    public void setIngredientList(ArrayList<Ingredient> ingredientList) {
        this.ingredientList = ingredientList;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public ArrayList<String> getStepList() {
        return StepList;
    }


    public void setStepList(ArrayList<String> stepList) {
        StepList = stepList;
    }

    public ArrayList<String> getDietaryRec() {
        return dietaryRec;
    }

    public void setDietaryRec(ArrayList<String> dietaryRec) {
        this.dietaryRec = dietaryRec;
    }
    public void uploadToFirebase(Uri imageUri, Context context, String recipeName, String category, int time, int portions, ArrayList<Ingredient> ingredientList, ArrayList<String> stepList, ArrayList<String> dietaryRecList, String priv){
        StorageReference storageRef= FirebaseStorage.getInstance().getReference().child("Images").child(imageUri.getLastPathSegment());
        storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
            while(!uriTask.isComplete()) {
            }
            Uri imageUriNew=uriTask.getResult();
            auth= FirebaseAuth.getInstance();
            FirebaseUser user=auth.getCurrentUser();
            String uid;
            String key;
            if(user!=null){
                uid=user.getUid();
                key=recipeRef.push().getKey();
                Recipe recipe=new Recipe(key,imageUriNew.toString(),recipeName,category,time,portions,ingredientList,stepList,dietaryRecList);
                String publics=context.getResources().getString(R.string.publics);
                if(priv.equals(publics)){
                    recipeRef.child(key).setValue(recipe).addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            Toast.makeText(context,R.string.uploadSuccess,Toast.LENGTH_SHORT).show();
                            userRef.child(uid).child("Own").child(key).setValue(key).addOnCompleteListener(task1 -> {
                                if(task1.isSuccessful()){
                                    Log.d(TAG, "Added");}
                                else{
                                    Log.d(TAG, "failed");
                                }
                            }).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    }).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(),Toast.LENGTH_SHORT));
                }else{
                    userRef.child(uid).child("Privates").child(key).setValue(recipe).addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            Toast.makeText(context,R.string.uploadSuccess,Toast.LENGTH_SHORT).show();
                            userRef.child(uid).child("Own").child(key).setValue(key).addOnCompleteListener(task1 -> {
                                if(task1.isSuccessful()){
                                    Log.d(TAG, "Added to privates");}
                                else{
                                    Log.d(TAG, "failed to add to privates");
                                }
                            }).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    });
                    removeFromFirebase(key,context);

                }
                Intent toRecipeViewIntent=new Intent(context, RecipeViewActivity.class);
                toRecipeViewIntent.putExtra("key",recipe.getKey());
                toRecipeViewIntent.putExtra("fromCreate",0);
                toRecipeViewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.getApplicationContext().startActivity(toRecipeViewIntent);


            }else {
                Intent toLoginIntent=new Intent(context, LoginActivity.class);
                toLoginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(toLoginIntent);
            }
        });
    }
    public Recipe rebuildFromFirebase(String key, DataSnapshot snapshot, Recipe selectedRecipe) {
        String dBrecipeName = String.valueOf(snapshot.child("recipeName").getValue());
        String dBcat = String.valueOf(snapshot.child("category").getValue());
        int dBprepTime = Integer.parseInt(String.valueOf(snapshot.child("prepTime").getValue()));
        int dBportions = Integer.parseInt(String.valueOf(snapshot.child("portions").getValue()));
        String dBImage = snapshot.child("image").getValue(String.class);

        ArrayList<String> dBstepList = new ArrayList<>();
        String index="0";
        for(DataSnapshot stepSS:snapshot.child("stepList").getChildren()){
            String stepTry=String.valueOf(snapshot.child("stepList").child(index).getValue());
            dBstepList.add(stepTry);
            int i=Integer.parseInt(index);
            i++;
            index= Integer.toString(i);
        }
        String index2="0";
        ArrayList<String> dBdietList = new ArrayList<>();
        for(DataSnapshot stepSS:snapshot.child("dietaryRec").getChildren()){
            String dietTry=String.valueOf(snapshot.child("dietaryRec").child(index2).getValue());
            int i=Integer.parseInt(index2);
            i++;
            index2= Integer.toString(i);
            dBdietList.add(dietTry);
        }
        ArrayList<Ingredient>dBIngredientList=new ArrayList<>();
        for(DataSnapshot IngSS:snapshot.child("ingredientList").getChildren()){
            Double amount;
            if(IngSS.child("amount").getValue(Double.class)!=null){
                amount=IngSS.child("amount").getValue(Double.class);
            }else{
                amount=0.0;
            }
            String unit=IngSS.child("unit").getValue(String.class);
            String ingredientName=IngSS.child("ingredientName").getValue(String.class);
            Ingredient ingredient=new Ingredient(amount,unit,ingredientName);
            dBIngredientList.add(ingredient);
            selectedRecipe = new Recipe(key, dBImage, dBrecipeName, dBcat, dBprepTime, dBportions, dBIngredientList, dBstepList,dBdietList);
        }return selectedRecipe;

    }

    public void removeFromFirebase(String key,Context context){
        recipeRef.child(key).removeValue().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                Toast.makeText(context, R.string.deletSuccess, Toast.LENGTH_SHORT).show();
                Intent toMainIntent=new Intent(context, MainActivity.class);
                toMainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(toMainIntent);
            } else{
                Toast.makeText(context, R.string.sthWrong, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

}

