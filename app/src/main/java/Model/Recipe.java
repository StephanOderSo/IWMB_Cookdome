package Model;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.widget.Toast;

import com.bienhuels.iwmb_cookdome.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import View.MainActivity;

public class Recipe implements Database {
    String key;
    private String image;
    private String recipeName;
    private int prepTime;
    private ArrayList<Ingredient> ingredientList=new ArrayList<>();
    private String category;
    private Integer portions;
    private ArrayList<Step> stepList=new ArrayList<Step>();
    private ArrayList<String> dietaryRec =new ArrayList<>();
    ArrayList<String>sharedWith=new ArrayList<>();
    Boolean priv;
    String owner;
    DatabaseReference userRef=FirebaseDatabase.getInstance().getReference("/Cookdome/Users");
    DatabaseReference recipeRef=FirebaseDatabase.getInstance().getReference("/Cookdome/Recipes");
    StorageReference storageRef= FirebaseStorage.getInstance().getReference().child("Images");
    User user=new User();
    Recipe selectedRecipe;



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
    public void setRecipe(Uri imageUri, String recipeName, String category, int time, int portions, ArrayList<Ingredient> ingredientList, ArrayList<Step> stepList, ArrayList<String> dietaryRecList, Thread nextThread, Context context, Handler handler, Boolean priv, String owner){
        if(imageUri!=null){
            storageRef.child(imageUri.getLastPathSegment()).putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                while(!uriTask.isComplete());
                this.image=uriTask.getResult().toString();
                this.recipeName=recipeName;
                this.category=category;
                this.prepTime=time;
                this.portions=portions;
                this.dietaryRec =dietaryRecList;
                this.ingredientList=ingredientList;
                this.stepList=stepList;
                this.priv=priv;
                this.owner=owner;
                nextThread.start();
            }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show()));
        }else{
            this.recipeName=recipeName;
            this.category=category;
            this.prepTime=time;
            this.portions=portions;
            this.dietaryRec =dietaryRecList;
            this.ingredientList=ingredientList;
            this.stepList=stepList;
            this.priv=priv;
            this.owner=owner;
            synchronized (nextThread){
                nextThread.start();
            }
        }
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


    public void upload(Context context, Boolean priv, Handler handler, FirebaseUser fbuser){
        if(this.key==null){
            key=recipeRef.push().getKey();
        }
        Recipe recipe=new Recipe(key,image,recipeName,category,prepTime,portions,ingredientList,stepList, dietaryRec,priv,owner,sharedWith);
        String uid=user.setID(fbuser,context);
        if(!priv){
            recipeRef.child(key).setValue(recipe).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    handler.post(() -> Toast.makeText(context,R.string.uploadSuccess,Toast.LENGTH_SHORT).show());
                    user.addToOwn(context,uid,key,handler);
                }
            }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show()));
        }else{
            userRef.child(uid).child("Privates").child(key).setValue(recipe).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    handler.post(() -> Toast.makeText(context,R.string.uploadSuccess,Toast.LENGTH_SHORT).show());
                    user.addToOwn(context,uid,key,handler);
                }
            });
            removePublicRecipe(key,context,handler);
        }
    }
    public void download(String key, Context context, Handler handler, Thread setDatathread, FirebaseUser fbUser){
        recipeRef.child(key).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    this.selectedRecipe=rebuildFromFirebase(snapshot);
                    setDatathread.start();
                } else {
                    String id=user.setID(fbUser,context);
                    userRef.child(id).child("Privates").child(key).get().addOnCompleteListener(task1 -> {
                        if (task1.getResult().exists()) {
                            DataSnapshot snapshot = task1.getResult();
                            this.selectedRecipe=rebuildFromFirebase(snapshot);
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

    public Recipe getRecipe(){
        return selectedRecipe;
    }


    //Mapping the firebase Data structure of a Recipe back to a recipe-Object
    public Recipe rebuildFromFirebase( DataSnapshot snapshot) {
        Recipe selectedRecipe;
        key = snapshot.child("key").getValue(String.class);
        recipeName = String.valueOf(snapshot.child("recipeName").getValue());
        category = String.valueOf(snapshot.child("category").getValue());
        prepTime = Integer.parseInt(String.valueOf(snapshot.child("prepTime").getValue()));
        portions = Integer.parseInt(String.valueOf(snapshot.child("portions").getValue()));
        image = snapshot.child("image").getValue(String.class);
        priv=snapshot.child("priv").getValue(Boolean.class);
        owner=snapshot.child("owner").getValue(String.class);
        String index="0";
        for(DataSnapshot stepSS:snapshot.child("stepList").getChildren()){
            Step stepTry=new Step (String.valueOf(snapshot.child("stepList").child(index).child("step").getValue()));
            if(snapshot.child("stepList").child(index).hasChild("media")){
                stepTry.setMedia(String.valueOf(snapshot.child("stepList").child(index).child("media").getValue(String.class)));
            }
            stepList.add(stepTry);
            int i=Integer.parseInt(index);
            i++;
            index= Integer.toString(i);
        }
        String index2="0";
        for(DataSnapshot sS:snapshot.child("dietaryRec").getChildren()){
            String dietTry=String.valueOf(snapshot.child("dietaryRec").child(index2).getValue());
            int i=Integer.parseInt(index2);
            i++;
            index2= Integer.toString(i);
            dietaryRec.add(dietTry);
        }
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
        }
        for(DataSnapshot ss:snapshot.child("sharedWith").getChildren()){
            String userID=ss.getValue(String.class);
            sharedWith.add(userID);
        }
            selectedRecipe = new Recipe(key, image, recipeName, category, prepTime, portions, ingredientList, stepList, dietaryRec,priv,owner,sharedWith);
        return selectedRecipe;

    }
    public void addUserToSharedListPublic(String userID, String key, Context context){
        sharedWith.add(userID);
        recipeRef.child(key).child("sharedWith").child(userID).setValue(userID).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Toast.makeText(context, R.string.shared, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show());
    }
    public void addUserToSharedListPrivate(String userID, String key, String owner,Context context){
        sharedWith.add(userID);
        userRef.child(owner).child("Privates").child(key).child("sharedWith").child(userID).setValue(userID).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Toast.makeText(context, R.string.shared, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show());
    }
    public void removeUserFromShareList(String userID,String key,Context context,String owner,Boolean priv,Handler handler,Thread nextThread){
        if(priv){
            userRef.child(owner).child("Privates").child(key).child("sharedWith").child(userID).removeValue().addOnCompleteListener(task -> {
                nextThread.start();
            }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show()));
        }else{
            recipeRef.child(key).child("sharedWith").child(userID).removeValue().addOnCompleteListener(task -> {
                nextThread.start();
            }).addOnFailureListener(e -> handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show()));
        }
        sharedWith.remove(userID);
    }

}

