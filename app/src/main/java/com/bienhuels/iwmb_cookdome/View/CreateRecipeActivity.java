package View;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bienhuels.iwmb_cookdome.R;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import Model.Ingredient;
import Model.Recipe;
import Viewmodel.CreateRecipeAdapters.EditStepAdapter;
import Viewmodel.CreateRecipeAdapters.IngredientListAdapter;
import Viewmodel.CreateRecipeAdapters.StepListAdapter;
import Viewmodel.CreateRecipeAdapters.editIngredientAdapter;

public class CreateRecipeActivity extends AppCompatActivity {
    private ImageView imageView;
    EditText recipeNameView;
    ProgressBar progressbar;
    Uri imageUri;
    ListView ingredientsView,stepsView;
    Integer portions,time;
    Float amount;
    String recipeName,category,ingredientName,key,dietRec,unit;
    EditText ingredientView,enterStepView,portionsView, amountView,timeView;
    public ArrayList<Ingredient> ingredientList;
    public ArrayList<String> stepList;
    FloatingActionButton addIngredientBtn,addStepBtn;
    ImageButton expandDetailsBtn;
    ConstraintLayout details, image;
    TextView dietaryBtn;
    Integer clickCount;
    boolean[] selectedDiet;
    EditStepAdapter stepAdapter;
    ArrayList<Integer>dietList=new ArrayList<>();
    ArrayList<String>dietaryRecList=new ArrayList<>();
    StringBuilder dietSb;
    TextView catBtn,unitBtn;
    TextView detailsHeader;
    IngredientListAdapter ingredientAdapter;
    StepListAdapter stepListAdapter;
    String vegi,vegan,paleo,gf,lf,lof;
    Recipe selectedRecipe;


    /****/
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recipe);
//create Arraylists for Alert Dialog selections
        String[] dietArray={getResources().getString(R.string.none),getResources().getString(R.string.glutenfree),getResources().getString(R.string.lactosefree),getResources().getString(R.string.vegan),getResources().getString(R.string.vegetar),getResources().getString(R.string.paleo),getResources().getString(R.string.lowfat)};
        String[] catArray={getResources().getString(R.string.breakki),getResources().getString(R.string.mainMeal),getResources().getString(R.string.dessert),getResources().getString(R.string.snack),getResources().getString(R.string.soup),getResources().getString(R.string.salad)};
        String[] unitArray={" ","cup","tsp","tbsp","ml","l","g","kg","mg","oz","pound"};
//assign strings to variables for access outside of onCreate
        vegi=getResources().getString(R.string.vegetar);
        vegan=getResources().getString(R.string.vegan);
        paleo=getResources().getString(R.string.paleo);
        lf=getResources().getString(R.string.lactosefree);
        gf=getResources().getString(R.string.glutenfree);
        lof=getResources().getString(R.string.lowfat);
//Initialise lists and variables
        ingredientList=new ArrayList<>();
        stepList=new ArrayList<>();
        unit="";
        dietRec="";
        clickCount=1;
//Assign variables to View-objects
        Button save = findViewById(R.id.save);
        imageView=findViewById(R.id.uploadImage);
        recipeNameView=findViewById(R.id.recipeName);
        progressbar=findViewById(R.id.progressBar);
        progressbar.setVisibility(View.INVISIBLE);
        timeView = findViewById(R.id.preptime);
        portionsView=findViewById(R.id.portionen);
        amountView=findViewById(R.id.amount);
        ingredientsView=findViewById(R.id.ingredientlist);
        stepsView=findViewById(R.id.stepList);
        ingredientView=findViewById(R.id.ingredient);
        enterStepView=findViewById(R.id.step);
        addStepBtn=findViewById(R.id.addStepbtn);
        addIngredientBtn=findViewById(R.id.addIngredientBtn);
        expandDetailsBtn=findViewById(R.id.expandDetailsBtn);
        details=findViewById(R.id.detail);
        image=findViewById(R.id.uploadImageBorder);
        unitBtn=findViewById(R.id.unit);
        detailsHeader=findViewById(R.id.detailsHeader);
//Initialise Database reference to Recipes folder in Firebase external Database
        databaseReference=FirebaseDatabase.getInstance().getReference("/Cookdome/Recipes");

//unfold Details (clickcount serves to determin whether Details are currently unfolded or not)
        clickCount=1;
        expandDetailsBtn.setOnClickListener(view -> {
            //if uneven clickcount-> unfold details, remove imageview, change Button image and text
            if (clickCount%2 !=0){
                expandDetailsBtn.setImageResource(R.drawable.arrow_up);
                detailsHeader.setText(R.string.all);
                details.setVisibility(View.VISIBLE);
                image.setVisibility(View.GONE);
            //otherwise bring imageview back and change Button image and text back
            } else {
                expandDetailsBtn.setImageResource(R.drawable.arrow_down);
                image.setVisibility(View.VISIBLE);
                detailsHeader.setText(R.string.details);
            }
            //Either way increase click count
            clickCount++;
        });
//Select Unit Alert Dialog

        unitBtn.setOnClickListener(view -> {
            AlertDialog.Builder builder=new AlertDialog.Builder(CreateRecipeActivity.this);
            builder.setTitle(R.string.chooseUnit);
            builder.setCancelable(false);
            builder.setSingleChoiceItems(unitArray, -1, (dialogInterface, i) -> unit=unitArray[i]);
            builder.setPositiveButton("OK", (dialogInterface, i) -> {
                if(unit!=null&&!unit.equals("")){
                    unitBtn.setText(unit);}
                else{
                    Toast.makeText(CreateRecipeActivity.this, R.string.chooseUnit, Toast.LENGTH_SHORT).show();
                }
            });
            builder.show();
        });

//Category Select Alert Dialog
        catBtn=findViewById(R.id.category);
        catBtn.setOnClickListener(view -> {
            AlertDialog.Builder builder=new AlertDialog.Builder(CreateRecipeActivity.this);
            builder.setTitle("Choose category");
            builder.setCancelable(false);
            builder.setSingleChoiceItems(catArray, -1, (dialogInterface, i) -> category=catArray[i]);
            builder.setPositiveButton("OK", (dialogInterface, i) -> {
                if(category!=null){
                    catBtn.setText(category);}
                else{
                    Toast.makeText(CreateRecipeActivity.this, R.string.chooseCat, Toast.LENGTH_SHORT).show();
                }
            });
            builder.show();
        });

//Dietary Button
        dietaryBtn=findViewById(R.id.dietBtn);
        selectedDiet=new boolean[dietArray.length];
        dietaryBtn.setOnClickListener(view -> {
            AlertDialog.Builder builder=new AlertDialog.Builder(CreateRecipeActivity.this);
            builder.setTitle(R.string.chooseDiet);
            builder.setCancelable(false);
            builder.setMultiChoiceItems(dietArray, selectedDiet, (dialogInterface, i, b) -> {
                String item;
                if(b){
                    dietList.add(i);
                    item=dietArray[i];
                    dietaryRecList.add(item);
                    Collections.sort(dietList);
                }else {
                    dietList.remove(Integer.valueOf(i));
                    item=dietArray[i];
                    dietaryRecList.remove(item);
                }
            });
            builder.setPositiveButton("OK", (dialogInterface, i) -> {
                dietSb=new StringBuilder();
                if(dietaryRecList.isEmpty()){
                    Toast.makeText(CreateRecipeActivity.this, R.string.chooseDiet, Toast.LENGTH_SHORT).show();
                }else{
                for(String diet: dietaryRecList){
                    String dietShort = "";
                    if (diet.equals(vegi)) {
                        dietShort = "VT";
                    }
                    if (diet.equals(vegan)) {
                        dietShort = "V";
                    }
                    if (diet.equals(gf)) {
                        dietShort = "GF";
                    }
                    if (diet.equals(lf)) {
                        dietShort = "LF";
                    }
                    if (diet.equals(paleo)) {
                        dietShort = "P";
                    }
                    if (diet.equals(lof)) {
                        dietShort = "LoF";
                    }

                    dietSb.append(dietShort);
                    i=dietRec.indexOf(diet);
                    if(i!=dietaryRecList.size()-1){
                        dietSb.append(" | ");
                    }
                if(dietSb.toString().equals("")){
                    dietaryBtn.setText(R.string.none);
                }else{
                dietaryBtn.setText(dietSb.toString());}
                }
                }

           });
            builder.setNeutralButton("Clear", (dialogInterface, i) -> {
                for(int j=0;j<selectedDiet.length;j++){
                    selectedDiet[j]=false;
                    dietList.clear();
                    dietaryRecList.clear();
                    dietaryBtn.setText(getResources().getString(R.string.none));
                }
            });
            builder.show();
        });

//Recipe Image
        ActivityResultLauncher<Intent> activityResultLauncher= registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode()== Activity.RESULT_OK){
                        Intent data=result.getData();
                        assert data != null;
                        if(data.getData()!=null){
                            imageUri=data.getData();
                            imageView.setImageURI(imageUri);
                        }else{
                            Toast.makeText(this, R.string.no_image_selected, Toast.LENGTH_SHORT).show();
                        }

                    }else {
                        Toast.makeText(CreateRecipeActivity.this,R.string.no_image_selected,Toast.LENGTH_SHORT).show();
                    }
                }
        );
        imageView.setOnClickListener(view -> {
            Intent photoPicker = new Intent();
            photoPicker.setAction(Intent.ACTION_GET_CONTENT);
            photoPicker.setType("*/*");
            activityResultLauncher.launch(photoPicker);

        });

//Intentfilter for correct Adapter and content
        Intent previousIntent=getIntent();
        if (previousIntent.hasExtra("Edit")){
            Button delete=findViewById(R.id.delete);
            delete.setVisibility(View.VISIBLE);
            String key=previousIntent.getStringExtra("Edit");
            delete.setOnClickListener(view -> removeRecipe(key));
            getSelectedRecipe(key);
        }else{
//Ingredient List
            ingredientAdapter= new IngredientListAdapter(getApplicationContext(),0,ingredientList);
            ingredientsView.setAdapter(ingredientAdapter);
//Step List
            stepListAdapter= new StepListAdapter(getApplicationContext(),0,stepList);
            stepsView.setAdapter(stepListAdapter);
        }

//add Ingredient Button
        addIngredientBtn.setOnClickListener(view -> {
            if(ingredientView.getText().toString().equals("")){
                Toast.makeText(CreateRecipeActivity.this,R.string.enterIngr,Toast.LENGTH_SHORT).show();
            }
            if(unit==null||unit.equals("")){
                Toast.makeText(CreateRecipeActivity.this,R.string.chooseUnit,Toast.LENGTH_SHORT).show();
            }
            if(amountView.getText().toString().equals("")||amountView.getText()==null){
                Toast.makeText(CreateRecipeActivity.this,R.string.enterAmount,Toast.LENGTH_SHORT).show();
            }else{
                amount=Float.parseFloat(amountView.getText().toString());
                ingredientName=ingredientView.getText().toString().toLowerCase();
                Ingredient ingredient=new Ingredient(amount,unit,ingredientName);
                ingredientList.add(ingredient);
                ingredientAdapter.notifyDataSetChanged();
                amountView.setText("");
                ingredientView.setText("");
                getListViewSize(ingredientAdapter,ingredientsView);
            }
        });



//Add step
        addStepBtn.setOnClickListener(view -> {
            if(enterStepView.getText()==null||enterStepView.getText().toString().equals("")){
                Toast.makeText(CreateRecipeActivity.this,R.string.enterStep,Toast.LENGTH_SHORT).show();
            }else{
                String step=enterStepView.getText().toString();
                stepList.add(step);
                stepListAdapter.notifyDataSetChanged();
                enterStepView.setText("");
                getListViewSize(stepListAdapter,stepsView);
                }
        });
//Save Recipe
        save.setOnClickListener(view -> {

           save.setVisibility(View.GONE);

            if (imageUri==null) {
                Toast.makeText(CreateRecipeActivity.this, R.string.no_image_selected, Toast.LENGTH_SHORT).show();
                save.setVisibility(View.VISIBLE);
            }
            if(recipeNameView.getText().toString().equals("")){
                Toast.makeText(CreateRecipeActivity.this,R.string.no_name_selected,Toast.LENGTH_SHORT).show();
                save.setVisibility(View.VISIBLE);
            }
            if(category==null){
                Toast.makeText(CreateRecipeActivity.this,R.string.chooseCat,Toast.LENGTH_SHORT).show();
                save.setVisibility(View.VISIBLE);
            }
            if(timeView.getText().toString().equals("")){
                Toast.makeText(CreateRecipeActivity.this,R.string.enterPreptime,Toast.LENGTH_SHORT).show();
                save.setVisibility(View.VISIBLE);
            }
            if(portionsView.getText().toString().equals("")){
                Toast.makeText(CreateRecipeActivity.this,R.string.enterPortions,Toast.LENGTH_SHORT).show();
                save.setVisibility(View.VISIBLE);
            }
            if(ingredientsView.getCount()==0){
                Toast.makeText(CreateRecipeActivity.this,R.string.addIngredients,Toast.LENGTH_SHORT).show();
                save.setVisibility(View.VISIBLE);
            }
            if(stepsView.getCount()==0){
                Toast.makeText(CreateRecipeActivity.this,R.string.addSteps,Toast.LENGTH_SHORT).show();
                save.setVisibility(View.VISIBLE);
            }
            if(dietaryRecList.isEmpty()){
                Toast.makeText(CreateRecipeActivity.this,R.string.chooseDiet,Toast.LENGTH_SHORT).show();
                save.setVisibility(View.VISIBLE);}
            else {
                recipeName= recipeNameView.getText().toString();
                time = Integer.parseInt(timeView.getText().toString());
            portions=Integer.parseInt(portionsView.getText().toString());
            progressbar.setVisibility(View.VISIBLE);
            uploadToFirebase();
            Recipe recipe=new Recipe(key,imageUri.toString(),recipeName,category,time,portions,ingredientList,stepList,dietaryRecList);
            writeToLocalStorage(key, recipe);
            }

        });

    }



    public void createNewRecipe(Uri imageUriNew){
        DatabaseReference userref=FirebaseDatabase.getInstance().getReference("/Cookdome/Users");
        FirebaseAuth auth=FirebaseAuth.getInstance();
        String uid="";
        try{
            uid= Objects.requireNonNull(auth.getCurrentUser()).getUid();
        }catch(NullPointerException e){
            Intent toLoginIntent=new Intent(this,LoginActivity.class);
            startActivity(toLoginIntent);
        }
        key=databaseReference.push().getKey();
        Recipe recipe=new Recipe(key,imageUriNew.toString(),recipeName,category,time,portions,ingredientList,stepList,dietaryRecList);
        String finalUid = uid;
        databaseReference.child(key).setValue(recipe).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Toast.makeText(CreateRecipeActivity.this,R.string.uploadSuccess,Toast.LENGTH_SHORT).show();
                userref.child(finalUid).child("Own").child(key).setValue(key).addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful()){
                        Log.d(TAG, "Added");}
                    else{
                        Log.d(TAG, "failed");
                    }
                }).addOnFailureListener(e -> Toast.makeText(CreateRecipeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
                Intent toRecipeViewIntent=new Intent(getApplicationContext(), RecipeViewActivity.class);
                toRecipeViewIntent.putExtra("key",recipe.getKey());
                toRecipeViewIntent.putExtra("fromCreate",0);
                startActivity(toRecipeViewIntent);

            }
        }).addOnFailureListener(e -> Toast.makeText(CreateRecipeActivity.this, e.getMessage(),Toast.LENGTH_SHORT));
        progressbar.setVisibility(View.INVISIBLE);
    }


    public void uploadToFirebase(){
        StorageReference storageRef=FirebaseStorage.getInstance().getReference().child("Images").child(imageUri.getLastPathSegment());
        storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
            while(!uriTask.isComplete()) {
                Log.d(TAG,  "waiting");
            }
            Uri imageUriNew=uriTask.getResult();
            createNewRecipe(imageUriNew);
        }).addOnProgressListener(snapshot -> progressbar.getProgress()).addOnFailureListener(e -> Toast.makeText(CreateRecipeActivity.this, e.getMessage(),Toast.LENGTH_SHORT));

    }
    public void writeToLocalStorage(String fileName, Recipe content){
        File filepath=getApplicationContext().getFilesDir();
        Log.d("FILEPATHLOCAL", filepath.getName());
        try {
            FileOutputStream writer=new FileOutputStream(new File(filepath,fileName));
            writer.write(content.toString().getBytes());
            writer.close();
            Toast.makeText(getApplicationContext(),R.string.uploadSuccess,Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
//Edit Recipe functions
    public void getListViewSize(ArrayAdapter adapter, ListView view){
        int totalHeight=0;
        for ( int i=0;i<adapter.getCount();i++) {
            View mView=adapter.getView(i,null,view);
            mView.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            totalHeight += mView.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params=view.getLayoutParams();
        params.height=totalHeight+view.getDividerHeight()*adapter.getCount();
        view.setLayoutParams(params);
    }
    public void setRecipeData(Recipe recipe){
        Picasso.get()
                .load(selectedRecipe.getImage())
                .placeholder(R.drawable.camera)
                .fit()
                .centerCrop()
                .into(imageView);
        String name=selectedRecipe.getRecipeName();
        recipeNameView.setText(name);
        String time=String.valueOf(selectedRecipe.getPrepTime());
        timeView.setText(time);
        String portions=String.valueOf(selectedRecipe.getPortions());
        portionsView.setText(portions);
        String category=selectedRecipe.getCategory();
        catBtn.setText(category);
        dietaryRecList=recipe.getDietaryRec();
        StringBuilder dietaryTxt=new StringBuilder();
        for(String diet: dietaryRecList){
            String dietShort = "";
            if (diet.equals(vegi)) {
                dietShort = "VT";
            }
            if (diet.equals(vegan)) {
                dietShort = "V";
            }
            if (diet.equals(gf)) {
                dietShort = "GF";
            }
            if (diet.equals(lf)) {
                dietShort = "LF";
            }
            if (diet.equals(paleo)) {
                dietShort = "P";
            }
            if (diet.equals(lof)) {
                dietShort = "LoF";
            }
            if(diet.equals("None")){
                dietShort="None";
            }
            dietaryTxt.append(dietShort);
            int i;
            i=dietaryRecList.indexOf(diet);
            if(i!=dietaryRecList.size()-1){
                dietaryTxt.append(" | ");
            }
        }dietaryBtn.setText(dietaryTxt.toString());
        editIngredientAdapter ingredientAdapter = new editIngredientAdapter(getApplicationContext(), 0,selectedRecipe.getIngredientList());
        getListViewSize(ingredientAdapter,ingredientsView);
        ingredientsView.setAdapter(ingredientAdapter);
        stepAdapter = new EditStepAdapter(getApplicationContext(), 0, selectedRecipe.getStepList());
        getListViewSize(stepAdapter,stepsView);
        stepsView.setAdapter(stepAdapter);
    }
    private void getSelectedRecipe(String key) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reciperef = database.getReference("/Cookdome/Recipes");
        reciperef.child(key).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
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
                        try{
                        amount=IngSS.child("amount").getValue(Double.class);
                        }catch(NullPointerException e){amount=0.0;}
                        String unit=IngSS.child("unit").getValue(String.class);
                        String ingredientName=IngSS.child("ingredientName").getValue(String.class);
                        Ingredient ingredient=new Ingredient(amount,unit,ingredientName);
                        dBIngredientList.add(ingredient);
                        }
                    selectedRecipe = new Recipe(key, dBImage, dBrecipeName, dBcat, dBprepTime, dBportions, dBIngredientList, dBstepList,dBdietList);
                    setRecipeData(selectedRecipe);

                } else {
                    Toast.makeText(CreateRecipeActivity.this, R.string.dataRetrFailed, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void removeRecipe(String key){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reciperef = database.getReference("/Cookdome/Recipes");
        reciperef.child(key).removeValue().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                Toast.makeText(CreateRecipeActivity.this, R.string.deletSuccess, Toast.LENGTH_SHORT).show();
            } else{
                Toast.makeText(CreateRecipeActivity.this, R.string.sthWrong, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(CreateRecipeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public void updateListview(){
            getListViewSize(ingredientAdapter,ingredientsView);
        }

}