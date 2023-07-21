package View;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bienhuels.iwmb_cookdome.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;

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
    Integer portions=null,time=null;
    Float amount=null;
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
        String[] dietArray={getResources().getString(R.string.none),getResources().getString(R.string.glutenfree),getResources().getString(R.string.lactosefree),getResources().getString(R.string.vegan),getResources().getString(R.string.vegetar),getResources().getString(R.string.paleo),getResources().getString(R.string.lowfat)};
        String[] catArray={getResources().getString(R.string.breakki),getResources().getString(R.string.mainMeal),getResources().getString(R.string.dessert),getResources().getString(R.string.snack),getResources().getString(R.string.soup),getResources().getString(R.string.salad)};
        String[] unitArray={" ","cup","tsp","tbsp","ml","l","g","kg","mg","oz","pound"};
        vegi=getResources().getString(R.string.vegetar);
        vegan=getResources().getString(R.string.vegan);
        paleo=getResources().getString(R.string.paleo);
        lf=getResources().getString(R.string.lactosefree);
        gf=getResources().getString(R.string.glutenfree);
        lof=getResources().getString(R.string.lowfat);
        ingredientList=new ArrayList<>();
        stepList=new ArrayList<>();
        unit=null;
        dietRec=null;
        clickCount=1;
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
        //   spinner = (Spinner) findViewById(R.id.unit);
        //   categorySpinner=(Spinner)findViewById(R.id.category);
        ingredientView=findViewById(R.id.ingredient);
        enterStepView=findViewById(R.id.step);
        addStepBtn=findViewById(R.id.addStepbtn);
        addIngredientBtn=findViewById(R.id.addIngredientBtn);
        expandDetailsBtn=findViewById(R.id.expandDetailsBtn);
        details=findViewById(R.id.detail);
        image=findViewById(R.id.uploadImageBorder);
        databaseReference=FirebaseDatabase.getInstance().getReference("/Cookdome/Recipes");


//unfold Details
        clickCount=1;
        detailsHeader=findViewById(R.id.detailsHeader);
        expandDetailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clickCount%2 !=0){
                    expandDetailsBtn.setImageResource(R.drawable.arrow_up);
                    detailsHeader.setText(R.string.all);
                    details.setVisibility(View.VISIBLE);
                    image.setVisibility(View.GONE);

                } else {expandDetailsBtn.setImageResource(R.drawable.arrow_down);
                    image.setVisibility(View.VISIBLE);
                    detailsHeader.setText(R.string.details);
                }
                clickCount++;
            }
        });
//Select Unit Alert Dialog
        unitBtn=findViewById(R.id.unit);
        unitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder=new AlertDialog.Builder(CreateRecipeActivity.this);
                builder.setTitle("Choose Unit");
                builder.setCancelable(false);
                builder.setSingleChoiceItems(unitArray, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        unit=unitArray[i];
                        Log.d("CATEGORY", unit);
                    }
                });
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(!unit.equals(null)){
                            unitBtn.setText(unit);}
                        else{
                            Toast.makeText(CreateRecipeActivity.this, "select Unit", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.show();
            }
        });

//Category Select Alert Dialog
        catBtn=findViewById(R.id.category);
        catBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder=new AlertDialog.Builder(CreateRecipeActivity.this);
                builder.setTitle("Choose category");
                builder.setCancelable(false);
                builder.setSingleChoiceItems(catArray, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        category=catArray[i];
                        Log.d("CATEGORY", category);
                    }
                });
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(!category.equals(null)){
                            catBtn.setText(category);}
                        else{
                            Toast.makeText(CreateRecipeActivity.this, "select Category", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.show();
            }
        });

//Dietary Button
        dietaryBtn=findViewById(R.id.dietBtn);
        selectedDiet=new boolean[dietArray.length];
        dietaryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder=new AlertDialog.Builder(CreateRecipeActivity.this);
                builder.setTitle(R.string.chooseDiet);
                builder.setCancelable(false);
                builder.setMultiChoiceItems(dietArray, selectedDiet, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        String item;
                        if(b){
                            dietList.add(i);
                            item=dietArray[i];
                            dietaryRecList.add(item);
                            Collections.sort(dietList);
                        }else {
                            dietList.remove(Integer.valueOf(i));
                            item=dietArray[i];
                            if(dietaryRecList.contains(item)){
                                dietaryRecList.remove(item);
                            }

                        }
                        Log.d("liste", dietaryRecList.toString());
                    }
                });
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dietSb=new StringBuilder();
                        for(int j=0;j<dietList.size();j++){
                            dietSb.append(dietArray[dietList.get(j)]);
                            if(j!=dietList.size()-1){
                                dietSb.append(" | ");
                            }
                        }dietaryBtn.setText(dietSb.toString());
                    }
                });
                builder.setNeutralButton("Clear", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        for(int j=0;j<selectedDiet.length;j++){
                            selectedDiet[j]=false;
                            dietList.clear();
                            dietaryRecList.clear();
                            dietaryBtn.setText(getResources().getString(R.string.none));
                        }
                    }
                });
                builder.show();
            }
        });

//Recipe Image
        ActivityResultLauncher<Intent> activityResultLauncher= registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode()== Activity.RESULT_OK){
                        Intent data=result.getData();
                        imageUri=data.getData();
                        imageView.setImageURI(imageUri);
                    }else {
                        Toast.makeText(CreateRecipeActivity.this,"No Image Selected",Toast.LENGTH_SHORT).show();
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
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeRecipe(key);
                }
            });
            getSelectedRecipe(key);
        }else{
//Ingredient List
            ingredientAdapter= new IngredientListAdapter(getApplicationContext(),0,ingredientList);
            ingredientsView.setAdapter(ingredientAdapter);
//Arbeitsschritt Liste erstellen
            stepListAdapter= new StepListAdapter(getApplicationContext(),0,stepList);
            stepsView.setAdapter(stepListAdapter);
        }

//Zutat hinzufuegen
        addIngredientBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ingredientView.getText()==null){
                    Toast.makeText(CreateRecipeActivity.this,"Bitte Categorie waehlen",Toast.LENGTH_SHORT).show();
                }
                if(unitBtn.getText()==null){
                    Toast.makeText(CreateRecipeActivity.this,"Bitte Messeinheit auswaehlen",Toast.LENGTH_SHORT).show();
                }
                if(amountView.getText()==null){
                    Toast.makeText(CreateRecipeActivity.this,"Bitte Zubereitungszeit hinzufuegen",Toast.LENGTH_SHORT).show();
                }else{
                    amount=Float.parseFloat(amountView.getText().toString());
                    ingredientName=ingredientView.getText().toString().toLowerCase();
                    Ingredient ingredient=new Ingredient(amount,unit,ingredientName);
                    ingredientList.add(ingredient);
                    Log.d("INGREDIENTLIST", ingredientList.toString());
                    ingredientAdapter.notifyDataSetChanged();
                    amountView.setText("");
//            spinner.setSelection(0);
                    ingredientView.setText("");
                    getListViewSize(ingredientAdapter,ingredientsView);
                }
            }
        });



//Arbeitsschritt hinzufuegen
        addStepBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(enterStepView.getText()==null){
                    Toast.makeText(CreateRecipeActivity.this,"Bitte Arbeitsschritt eingeben",Toast.LENGTH_SHORT).show();
                }else{
                    String step=enterStepView.getText().toString();
                    stepList.add(step);
                    stepListAdapter.notifyDataSetChanged();
                    enterStepView.setText("");
                    getListViewSize(stepListAdapter,stepsView);
                    Log.d("new list",stepList.toString() );}
            }
        });
//Save Recipe
        save.setOnClickListener(view -> {
            if (imageUri==null) {
                Toast.makeText(CreateRecipeActivity.this, "Bitte Rezeptbild aussuchen", Toast.LENGTH_SHORT).show();
            }
            if(recipeNameView.getText()==null){
                Toast.makeText(CreateRecipeActivity.this,"Bitte Rezeptname hinzufuegen",Toast.LENGTH_SHORT).show();
            }
            if(category.trim().equals("")||category.equals("Kategorie")){
                Toast.makeText(CreateRecipeActivity.this,"Bitte Categorie waehlen",Toast.LENGTH_SHORT).show();
            }
            if(timeView.getText()==null){
                Toast.makeText(CreateRecipeActivity.this,"Bitte Zubereitungszeit hinzufuegen",Toast.LENGTH_SHORT).show();
            }
            if(portionsView.getText()==null){
                Toast.makeText(CreateRecipeActivity.this,"Bitte Portionenangabe hinzufuegen",Toast.LENGTH_SHORT).show();
            }
            if(ingredientsView.getCount()==0){
                Toast.makeText(CreateRecipeActivity.this,"Bitte Zutaten hinzufuegen",Toast.LENGTH_SHORT).show();
            }
            if(stepsView.getCount()==0){
                Toast.makeText(CreateRecipeActivity.this,"Bitte Zubereitungsschritte hinzufuegen",Toast.LENGTH_SHORT).show();
            }
            if(dietaryRecList.isEmpty()){
                Toast.makeText(CreateRecipeActivity.this,"Bitte Ernaehrings Spezifikation waehlen",Toast.LENGTH_SHORT).show();
            }
            else {
                recipeName=(String) recipeNameView.getText().toString();
                time = Integer.parseInt(timeView.getText().toString());}
            portions=Integer.parseInt(portionsView.getText().toString());
            progressbar.setVisibility(View.VISIBLE);
            uploadToFirebase();
            Recipe recipe=new Recipe(key,imageUri.toString(),recipeName,category,time,portions,ingredientList,stepList,dietaryRecList);
            writeToLocalStorage(key, recipe);

        });

    }



    public void createNewRecipe(Uri imageUriNew){
        DatabaseReference userref=FirebaseDatabase.getInstance().getReference("/Cookdome/Users");
        FirebaseAuth auth=FirebaseAuth.getInstance();
        String uid=auth.getCurrentUser().getUid();
        key=databaseReference.push().getKey();
        Recipe recipe=new Recipe(key,imageUriNew.toString(),recipeName,category,time,portions,ingredientList,stepList,dietaryRecList);
        databaseReference.child(key).setValue(recipe).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(CreateRecipeActivity.this,"erfolgreich hochgeladen",Toast.LENGTH_SHORT).show();
                    userref.child(uid).child("Own").child(key).setValue(key).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Log.d(TAG, "added to own recipes");}
                            else{
                                Log.d(TAG, "failed to add to own recipes");
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CreateRecipeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    Intent toRecipeViewIntent=new Intent(getApplicationContext(), RecipeViewActivity.class);
                    toRecipeViewIntent.putExtra("key",recipe.getKey());
                    startActivity(toRecipeViewIntent);

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateRecipeActivity.this,e.getMessage().toString(),Toast.LENGTH_SHORT);
            }
        });
        progressbar.setVisibility(View.INVISIBLE);

    }

    public void uploadToFirebase(){
        StorageReference storageRef=FirebaseStorage.getInstance().getReference().child("Images").child(imageUri.getLastPathSegment());
        storageRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                while(!uriTask.isComplete());
                Uri uriImage=uriTask.getResult();
                Uri imageUriNew=uriImage;
                createNewRecipe(imageUriNew);
            }

        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                progressbar.getProgress();


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateRecipeActivity.this,e.getMessage().toString(),Toast.LENGTH_SHORT);

            }
        });

    }
    public void writeToLocalStorage(String fileName, Recipe content){
        File filepath=getApplicationContext().getFilesDir();
        Log.d("FILEPATHLOCAL", filepath.getName());
        try {
            FileOutputStream writer=new FileOutputStream(new File(filepath,fileName));
            writer.write(content.toString().getBytes());
            writer.close();
            Toast.makeText(getApplicationContext(),"uploaded to local storage",Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void getListViewSize(ArrayAdapter adapter,ListView view){
        Integer totalHeight=0;
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
                .resize(400,400)
                .centerCrop()
                .into(imageView);
        String name=selectedRecipe.getRecipeName();
        Log.d("TAG", name);
        recipeNameView.setText(name);
        String time=String.valueOf(selectedRecipe.getPrepTime());
        timeView.setText(time);
        String portions=String.valueOf(selectedRecipe.getPortions());
        portionsView.setText(portions);
        String category=selectedRecipe.getCategory();
        catBtn.setText(category);
        StringBuilder dietaryTxt=new StringBuilder();
        for(String diet: recipe.getDietaryRec()){
            Log.d("DIET", diet);
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
                dietShort = "LF";
            }
            dietaryTxt.append(dietShort);
            Integer i;
            i=selectedRecipe.getDietaryRec().indexOf(diet);
            if(i!=recipe.getDietaryRec().size()-1){
                dietaryTxt.append(" | ");
            }
        }
        dietaryBtn.setText(dietaryTxt.toString());
        editIngredientAdapter ingredientAdapter = new editIngredientAdapter(getApplicationContext(), 0,selectedRecipe.getIngredientList());
        getListViewSize(ingredientAdapter,ingredientsView);
        ingredientsView.setAdapter(ingredientAdapter);
        Integer ingrcount=ingredientAdapter.getCount();
        Log.d("ingredientcount", ingrcount.toString());
        stepAdapter = new EditStepAdapter(getApplicationContext(), 0, selectedRecipe.getStepList());
        getListViewSize(stepAdapter,stepsView);
        stepsView.setAdapter(stepAdapter);
    }
    private void getSelectedRecipe(String key) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reciperef = database.getReference("/Cookdome/Recipes");
        reciperef.child(key).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        Log.d(TAG, "NEW VALUES");
                        DataSnapshot snapshot = task.getResult();
                        String dBrecipeName = String.valueOf(snapshot.child("recipeName").getValue());
                        Log.d("RecipeName", dBrecipeName);
                        String dBcat = String.valueOf(snapshot.child("category").getValue());
                        Log.d("category", dBcat);
                        Integer dBprepTime = Integer.parseInt(String.valueOf(snapshot.child("prepTime").getValue()));
                        Log.d("time", dBprepTime.toString());
                        Integer dBportions = Integer.parseInt(String.valueOf(snapshot.child("portions").getValue()));
                        Log.d("portions", dBportions.toString());
                        String dBImage = snapshot.child("image").getValue(String.class);
                        Log.d("imageUrl", dBImage);

                        ArrayList<String> dBstepList = new ArrayList<>();
                        String index="0";
                        for(DataSnapshot stepSS:snapshot.child("stepList").getChildren()){
                            String stepTry=String.valueOf(snapshot.child("stepList").child(index).getValue());
                            Log.d("stepTry", stepTry);
                            dBstepList.add(stepTry);
                            Integer i=Integer.parseInt(index);
                            i++;
                            index=(String)i.toString();
                            Log.d("steps", dBstepList.toString());
                        }
                        String index2="0";
                        ArrayList<String> dBdietList = new ArrayList<>();
                        for(DataSnapshot stepSS:snapshot.child("dietaryRec").getChildren()){
                            String dietTry=String.valueOf(snapshot.child("dietaryRec").child(index2).getValue());
                            Integer i=Integer.parseInt(index2);
                            i++;
                            index2=i.toString();
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
                        setRecipeData(selectedRecipe);

                    } else {
                        Toast.makeText(CreateRecipeActivity.this, "data retrieval failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
    public void removeRecipe(String key){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reciperef = database.getReference("/Cookdome/Recipes");
        reciperef.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(CreateRecipeActivity.this, "successfully deleted", Toast.LENGTH_SHORT).show();
                } else{
                    Toast.makeText(CreateRecipeActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateRecipeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}