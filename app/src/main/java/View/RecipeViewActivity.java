package View;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bienhuels.iwmb_cookdome.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

import Model.Ingredient;
import Model.Recipe;
import Viewmodel.RecipeViewAdapters.IngrListAdapterwSLBtn;
import Viewmodel.RecipeViewAdapters.StepListAdapterNoBtn;

public class RecipeViewActivity extends AppCompatActivity {
    DatabaseReference databaseReference,databaseReferenceFav;
    String dBImage, id;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ArrayList<String> favlist,ownlist;
    ImageView favView;

    Recipe selectedRecipe;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_view);
        database=FirebaseDatabase.getInstance();
        databaseReferenceFav = database.getReference("/Cookdome/Users");
        getSelectedRecipe();
        favView=findViewById(R.id.favourite);
        favView.setOnClickListener(view -> updateFavouritesList(selectedRecipe));




    }
    private void getSelectedRecipe() {
        Intent previousIntent = getIntent();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("/Cookdome/Recipes");
        String key = previousIntent.getStringExtra("key");
        databaseReference.child(key).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    String dBrecipeName = String.valueOf(snapshot.child("recipeName").getValue());
                    Log.d("RecipeName", dBrecipeName);
                    String dBcat = String.valueOf(snapshot.child("category").getValue());
                    Log.d("category", dBcat);
                    int dBprepTime = Integer.parseInt(String.valueOf(snapshot.child("prepTime").getValue()));
                    Log.d("time", Integer.toString(dBprepTime));
                    int dBportions = Integer.parseInt(String.valueOf(snapshot.child("portions").getValue()));
                    Log.d("portions", Integer.toString(dBportions));
                    dBImage = snapshot.child("image").getValue(String.class);
                    Log.d("imageUrl", dBImage);

                    ArrayList<String> dBstepList = new ArrayList<>();
                    String index="0";
                    for(DataSnapshot stepSS:snapshot.child("stepList").getChildren()){
                        String stepTry=String.valueOf(snapshot.child("stepList").child(index).getValue());
                        Log.d("stepTry", stepTry);
                        dBstepList.add(stepTry);
                        int i=Integer.parseInt(index);
                        i++;
                        index= Integer.toString(i);
                        Log.d("steps", dBstepList.toString());
                    }
                    String index2="0";
                    ArrayList<String> dBdietList = new ArrayList<>();
                    for(DataSnapshot stepSS:snapshot.child("dietaryRec").getChildren()){
                        String dietTry=String.valueOf(snapshot.child("dietaryRec").child(index2).getValue());
                        int i=Integer.parseInt(index2);
                        i++;
                        index2= Integer.toString(i);
                        Log.d("diaet", dietTry);
                        dBdietList.add(dietTry);
                    }
                    ArrayList<Ingredient>dBIngredientList=new ArrayList<>();
                    for(DataSnapshot IngSS:snapshot.child("ingredientList").getChildren()){
                        Double amount=IngSS.child("amount").getValue(Double.class);
                        String unit=IngSS.child("unit").getValue(String.class);
                        String ingredientName=IngSS.child("ingredientName").getValue(String.class);
                        Ingredient ingredient=new Ingredient(amount,unit,ingredientName);
                        dBIngredientList.add(ingredient);
                        Log.d("ingredient", ingredient.toString());}
                    selectedRecipe = new Recipe(key, dBImage, dBrecipeName, dBcat, dBprepTime, dBportions, dBIngredientList, dBstepList,dBdietList);
                    Log.d("2step", "worked");
                    getUserFav();
                    getUserOwn();
                    setValues(selectedRecipe);

                } else {
                    Toast.makeText(RecipeViewActivity.this, "data retrieval failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setValues (Recipe selectedRecipe) {
        TextView textView = findViewById(R.id.recipeName);
        ImageView imageView = findViewById(R.id.imageView);
        TextView category = findViewById(R.id.category);
        TextView portions = findViewById(R.id.portions);
        TextView time = findViewById(R.id.time);
        ListView ingredientList = findViewById(R.id.ingredientList);
        ListView stepList = findViewById(R.id.stepList);
        TextView dietary= findViewById(R.id.dietary);
        textView.setText(selectedRecipe.getRecipeName());
        Picasso.get()
                .load(selectedRecipe.getImage())
                .placeholder(R.drawable.camera)
                .resize(400,400)
                .centerCrop()
                .into(imageView);
        category.setText(selectedRecipe.getCategory());
        int tempPrepTime=selectedRecipe.getPrepTime();
        time.setText(String.format(Integer.toString(tempPrepTime)));
        Integer tempPortions=selectedRecipe.getPortions();
        portions.setText(String.format(tempPortions.toString()));
        StringBuilder dietaryTxt=new StringBuilder();
        for(String diet: selectedRecipe.getDietaryRec()){
            Log.d("DIET", diet);
            String dietShort = "";
            if (diet.equals(getResources().getString(R.string.vegetar))) {
                dietShort = "VT";
            }
            if (diet.equals(getResources().getString(R.string.vegan))) {
                dietShort = "V";
            }
            if (diet.equals(getResources().getString(R.string.glutenfree))) {
                dietShort = "GF";
            }
            if (diet.equals(getResources().getString(R.string.lactosefree))) {
                dietShort = "LF";
            }
            if (diet.equals(getResources().getString(R.string.paleo))) {
                dietShort = "P";
            }
            if (diet.equals(getResources().getString(R.string.lowfat))) {
                dietShort = "LF";
            }
            dietaryTxt.append(dietShort);
            int i;
            i=selectedRecipe.getDietaryRec().indexOf(diet);
            if(i!=selectedRecipe.getDietaryRec().size()-1){
                dietaryTxt.append(" | ");
            }
        }
        Log.d("DIETARYTEXT", dietaryTxt.toString());
        if(dietaryTxt.toString().equals("")){
            dietary.setVisibility(View.GONE);
            ImageView dietaryIcon=findViewById(R.id.dietaryIcon);
            dietaryIcon.setVisibility(View.GONE);
        }else{
            dietary.setText(dietaryTxt.toString());
        }

        ArrayList<Ingredient> dBingredientList;
        dBingredientList= selectedRecipe.getIngredientList();
        IngrListAdapterwSLBtn ingredientAdapter = new IngrListAdapterwSLBtn(getApplicationContext(), 0,dBingredientList);
        ingredientList.setAdapter(ingredientAdapter);
        int totalHeight=0;
        for (int i = 0; i < ingredientAdapter.getCount(); i++) {
            View mView = ingredientAdapter.getView(i, null, ingredientList);

            mView.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),

                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            totalHeight += mView.getMeasuredHeight();}
        int ingrcount=ingredientAdapter.getCount();
        ViewGroup.LayoutParams ingrparams=ingredientList.getLayoutParams();
        ingrparams.height=totalHeight+ingredientList.getDividerHeight()*ingrcount;
        ingredientList.setLayoutParams(ingrparams);
        ingredientList.setLayoutParams(ingrparams);
        ArrayList<String>dBStepList;
        dBStepList=selectedRecipe.getStepList();
        StepListAdapterNoBtn stepAdapter = new StepListAdapterNoBtn(getApplicationContext(), 0, dBStepList);
        stepList.setAdapter(stepAdapter);
        int totalHeight2=0;
        for (int i = 0; i < stepAdapter.getCount(); i++) {
            View mView = stepAdapter.getView(i, null, stepList);

            mView.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),

                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            totalHeight2 += mView.getMeasuredHeight();}
        int stepcount=stepAdapter.getCount();
        ViewGroup.LayoutParams stepparams=stepList.getLayoutParams();
        stepparams.height=totalHeight2+stepList.getDividerHeight()*stepcount;
        stepList.setLayoutParams(stepparams);

    }
    public void getUserFav() {
        auth= FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(RecipeViewActivity.this, "You're not logged in", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(RecipeViewActivity.this, LoginActivity.class);
            startActivity(loginIntent);
        } else {
            id = currentUser.getUid();
            favlist = new ArrayList<>();
            databaseReferenceFav.child(id).child("Favourites").get().addOnCompleteListener(task -> {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    for (DataSnapshot dsS : snapshot.getChildren()) {
                        String favkey = dsS.getKey();
                        Log.d("KEY", favkey);
                        favlist.add(favkey);
                    }
                    Log.d("FAVLIST", favlist.toString());
                    if(favlist.contains(selectedRecipe.getKey())){
                        Log.d("FAVLIST", "Key in list");
                        favView.setImageResource(R.drawable.liked);
                    }else{
                        favView.setImageResource(R.drawable.unliked);
                    }
                }
            }).addOnFailureListener(e -> Toast.makeText(RecipeViewActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
    public void updateFavouritesList (Recipe recipe){
        if (favlist.contains(recipe.getKey())) {
            Log.d("TAG", "isFavourite");
            databaseReferenceFav.child(id).child("Favourites").child(recipe.getKey()).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    favView.setImageResource(R.drawable.unliked);
                    favlist.remove(recipe.getKey());
                    Toast.makeText(RecipeViewActivity.this, "removed from favourites", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RecipeViewActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> Toast.makeText(RecipeViewActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
        }else{
            Log.d("TAG", "isNOTFavourite");
            databaseReferenceFav.child(id).child("Favourites").child(recipe.getKey()).setValue(recipe).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    favView.setImageResource(R.drawable.liked);
                    favlist.add(recipe.getKey());
                    Toast.makeText(RecipeViewActivity.this, "Added to favourites", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RecipeViewActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> Toast.makeText(RecipeViewActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
    public void getUserOwn(){
        auth= FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        ownlist=new ArrayList<>();
        if (currentUser == null) {
            Toast.makeText(RecipeViewActivity.this, R.string.signedOut, Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(RecipeViewActivity.this, LoginActivity.class);
            startActivity(loginIntent);
        } else {
            id = currentUser.getUid();
            String key=selectedRecipe.getKey();
            databaseReferenceFav.child(id).child("Own").get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    DataSnapshot snapshot=task.getResult();
                    for(DataSnapshot ss:snapshot.getChildren()){
                        String key1 =ss.getKey();
                        Log.d(TAG, "KEYIS"+ key1);
                        ownlist.add(key1);
                    }
                    if(ownlist.contains(selectedRecipe.getKey())){
                        Log.d("TAG", "eigenes rezept");
                        ImageView edit=findViewById(R.id.edit);
                        edit.setVisibility(View.VISIBLE);
                        edit.setOnClickListener(view -> {
                            Intent toEditIntent=new Intent(RecipeViewActivity.this, CreateRecipeActivity.class);
                            toEditIntent.putExtra("Edit",key);
                            startActivity(toEditIntent);
                        });
                    }else{
                        Log.d(TAG, "not users recipe");
                    }

                }
            }).addOnFailureListener(e -> Log.d(TAG, "not created by user"));
        }
    }

    @Override
    public void onBackPressed() {
        Intent previousIntent = getIntent();
        if(previousIntent.hasExtra("fromCreate")){
            Log.d(TAG, "onBackPressed: main");
            Intent toMainIntent=new Intent(RecipeViewActivity.this,MainActivity.class);
            startActivity(toMainIntent);
        }else{
            Log.d(TAG, "onBackPressed: back");
            super.onBackPressed();
        }

    }
}