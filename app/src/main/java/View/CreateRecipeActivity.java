package View;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bienhuels.iwmb_cookdome.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

import Model.Ingredient;
import Model.Recipe;
import Model.User;
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
    String recipeName,category,ingredientName,dietRec,unit,text;
    EditText ingredientView,enterStepView,portionsView, amountView,timeView;
    ArrayList<Ingredient> ingredientList=new ArrayList<>();
    ArrayList<String> stepList=new ArrayList<>();
    FloatingActionButton addIngredientBtn,addStepBtn;
    ConstraintLayout details, image;
    TextView dietaryBtn;
    Integer clickCount;
    boolean[] selectedDiet;
    ArrayList<Integer> dietIntList =new ArrayList<>();
    ArrayList<String>dietaryRecList=new ArrayList<>();
    TextView catBtn,unitBtn;
    IngredientListAdapter ingredientAdapter;
    StepListAdapter stepListAdapter;
    String priv;
    Recipe selectedRecipe=new Recipe();
    Switch privateswitch;
    Handler handler=new Handler();
    FirebaseUser fbuser;
    User user=new User();
    Button save;
    Context context;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recipe);
        fbuser=FirebaseAuth.getInstance().getCurrentUser();
//create Arraylists for Alert Dialog selections
        String[] dietArray={getResources().getString(R.string.glutenfree),getResources().getString(R.string.lactosefree),getResources().getString(R.string.vegan),getResources().getString(R.string.vegetar),getResources().getString(R.string.paleo),getResources().getString(R.string.lowfat)};
        String[] catArray={getResources().getString(R.string.breakki),getResources().getString(R.string.mainMeal),getResources().getString(R.string.dessert),getResources().getString(R.string.snack),getResources().getString(R.string.soup),getResources().getString(R.string.salad)};
        String[] unitArray={" ","cup","tsp","tbsp","ml","l","g","kg","mg","oz","pound"};
//assign strings to variables for access outside of onCreate
        //Intentfilter for correct Adapter and content
        context=getApplicationContext();
        Intent previousIntent=getIntent();
        ingredientsView=findViewById(R.id.ingredientlist);
        stepsView=findViewById(R.id.stepList);
        //Initialise lists and variables
        ingredientList=new ArrayList<>();
        stepList=new ArrayList<>();
        unit="";
        dietRec="";
        clickCount=1;

        if (previousIntent.hasExtra("Edit")){
            Button delete=findViewById(R.id.delete);
            delete.setVisibility(View.VISIBLE);
            String key=previousIntent.getStringExtra("Edit");
            delete.setOnClickListener(view -> {
                Runnable removeRun=new Runnable() {
                    @Override
                    public void run() { user.removeRecipe(key,context,handler,fbuser);}};
                Thread removeThread=new Thread(removeRun);
                removeThread.start();

            });
            Runnable setDataRun=new Runnable() {
                @Override
                public void run() {
                    synchronized (Thread.currentThread()){

                            try {
                                Thread.currentThread().wait();
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    selectedRecipe=selectedRecipe.getRecipe();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            setRecipeData(selectedRecipe,dietArray);
                        }});
                }
            };
            Thread setDataThread=new Thread(setDataRun);
            setDataThread.start();

            Runnable runnable=new Runnable() {
                @Override
                public void run() {
                    selectedRecipe.downloadSelectedRecipe(key,context,handler,setDataThread,fbuser);
                }
            };
            Thread getRThread=new Thread(runnable);
            getRThread.start();

        }else{
//Ingredient List

            ingredientAdapter= new IngredientListAdapter(getApplicationContext(),0,ingredientList);
            ingredientsView.setAdapter(ingredientAdapter);
//Step List

            stepListAdapter= new StepListAdapter(getApplicationContext(),0,stepList);
            stepsView.setAdapter(stepListAdapter);
        }

//Assign variables to View-objects
        save = findViewById(R.id.save);
        imageView=findViewById(R.id.uploadImage);
        recipeNameView=findViewById(R.id.recipeName);
        progressbar=findViewById(R.id.progressBar);
        progressbar.setVisibility(View.INVISIBLE);
        timeView = findViewById(R.id.preptime);
        portionsView=findViewById(R.id.portionen);
        amountView=findViewById(R.id.amount);
        ingredientView=findViewById(R.id.ingredient);
        enterStepView=findViewById(R.id.step);
        addStepBtn=findViewById(R.id.addStepbtn);
        addIngredientBtn=findViewById(R.id.addIngredientBtn);
        details=findViewById(R.id.detail);
        image=findViewById(R.id.uploadImageBorder);
        unitBtn=findViewById(R.id.unit);
        privateswitch=findViewById(R.id.privateswitch);



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
                    dietIntList.add(i);
                    item=dietArray[i];
                    dietaryRecList.add(item);
                    Collections.sort(dietIntList);
                }else {
                    dietIntList.remove(Integer.valueOf(i));
                    item=dietArray[i];
                    dietaryRecList.remove(item);
                }
            });
            builder.setPositiveButton("OK", (dialogInterface, i) -> {
                if(dietaryRecList.isEmpty()){
                    Toast.makeText(CreateRecipeActivity.this, R.string.chooseDiet, Toast.LENGTH_SHORT).show();
                }else{
                    Runnable runnable=new Runnable() {
                        @Override
                        public void run() {
                            buildDietString();
                        }
                    };
                    Thread stringBuildThread=new Thread(runnable);
                    stringBuildThread.start();
                }
           });
            builder.setNeutralButton("Clear", (dialogInterface, i) -> {
                for(int j=0;j<selectedDiet.length;j++){
                    selectedDiet[j]=false;
                    dietIntList.clear();
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
//Private or Public switch
        privateswitch.setOnClickListener(view -> {
            if(!privateswitch.isChecked()){
                privateswitch.setText(R.string.privates);
            }else{
                privateswitch.setText(R.string.publics);
            }
        });

//Save Recipe
        save.setOnClickListener(view -> {
           save.setVisibility(View.GONE);
           saveRecipe();
        });
    }
    public void saveRecipe(){
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
            priv=privateswitch.getText().toString();
            Handler handler=new Handler();
            FirebaseUser fbuser= FirebaseAuth.getInstance().getCurrentUser();
            Runnable uploadRunnable=new Runnable() {
                @Override
                public void run() {
                    synchronized (Thread.currentThread()){
                        try {
                            Thread.currentThread().wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    selectedRecipe.uploadUpdate(context,priv,handler,fbuser);
                }
            };
            Thread uploadThread=new Thread(uploadRunnable);
            uploadThread.start();
            Runnable setRun=new Runnable() {
                @Override
                public void run() {
                    selectedRecipe.setRecipe(imageUri,recipeName,category,time,portions,ingredientList,stepList,dietaryRecList,uploadThread,context,handler);
                }
            };
            Thread setThread=new Thread(setRun);
            setThread.start();
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
        int newTotal=totalHeight;
        Handler adjustListSizeHandler=new Handler();
        adjustListSizeHandler.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams params=view.getLayoutParams();
                params.height=newTotal+view.getDividerHeight()*adapter.getCount();
                view.setLayoutParams(params);
            }
        });

    }
    public void setRecipeData(Recipe recipe,String[] dietArray){
        String name=recipe.getRecipeName();
        String time=String.valueOf(recipe.getPrepTime());
        String portions=String.valueOf(recipe.getPortions());
        String category=recipe.getCategory();

        handler.post(new Runnable() {
            @Override
            public void run() {
                Picasso.get()
                        .load(recipe.getImage())
                        .placeholder(R.drawable.camera)
                        .fit()
                        .centerCrop()
                        .into(imageView);
                recipeNameView.setText(name);
                timeView.setText(time);
                portionsView.setText(portions);
                catBtn.setText(category);
                dietaryRecList=recipe.getDietaryRec();
                buildDietString();
                setDietInclCheck(dietArray);
                stepList=recipe.getStepList();
                ingredientList=recipe.getIngredientList();
                editIngredientAdapter ingredientAdapter = new editIngredientAdapter(getApplicationContext(), 0,ingredientList);
                EditStepAdapter stepAdapter = new EditStepAdapter(getApplicationContext(), 0,stepList);
                ingredientsView.setAdapter(ingredientAdapter);
                stepsView.setAdapter(stepAdapter);
                getListViewSize(stepAdapter,stepsView);
                getListViewSize(ingredientAdapter,ingredientsView);
            }
        });

    }
    //Preselect Dietary Checkboxes according to Recipe data
 public void setDietInclCheck(String[] dietArray){
        int j=0;
        for(int i=dietArray.length;j<i;j++){
            String s= dietArray[j];
            if(dietaryRecList.contains(s)){
                dietIntList.add(j);
                selectedDiet[j]=true;
            }else{
                selectedDiet[j]=false;
            }
        }
 }
   public void buildDietString(){
       StringBuilder dietaryTxt=new StringBuilder();
       for(String diet: dietaryRecList){
           String dietShort = "";
           if (diet.equals(getString(R.string.vegetar))) {
               dietShort = "VT";
           }
           if (diet.equals(getString(R.string.vegan))) {
               dietShort = "V";
           }
           if (diet.equals(getString(R.string.glutenfree))) {
               dietShort = "GF";
           }
           if (diet.equals(getString(R.string.lactosefree))) {
               dietShort = "LF";
           }
           if (diet.equals(getString(R.string.paleo))) {
               dietShort = "P";
           }
           if (diet.equals(getString(R.string.lowfat))) {
               dietShort = "LoF";
           }
           dietaryTxt.append(dietShort);
           int i;
           i=dietaryRecList.indexOf(diet);
           if(i!=dietaryRecList.size()-1){
               dietaryTxt.append(" | ");
           }
       }
       if (dietaryTxt.toString().equals("")){
           text=getString(R.string.diet);
       }else{
           text=dietaryTxt.toString();
       }
       handler.post(new Runnable() {
           @Override
           public void run() {
               dietaryBtn.setText(text);
           }
       });
   }

    public void updateListview(){
            getListViewSize(ingredientAdapter,ingredientsView);
        }
    @Override
    public void onBackPressed(){
       Intent toMainIntent=new Intent(CreateRecipeActivity.this,MainActivity.class);
       startActivity(toMainIntent);
       finish();
    }

}