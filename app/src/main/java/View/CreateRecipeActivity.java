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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bienhuels.iwmb_cookdome.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

import Model.Database;
import Model.Ingredient;
import Model.Recipe;
import Model.User;
import Viewmodel.CreateRecipeAdapters.EditStepAdapter;
import Viewmodel.CreateRecipeAdapters.IngredientListAdapter;
import Viewmodel.CreateRecipeAdapters.ShareListAdapter;
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
    String recipeName,category,ingredientName,text;
    String dietRec="";
    String unit="";
    EditText ingredientView,enterStepView,portionsView, amountView,timeView;
    ArrayList<Ingredient> ingredientList=new ArrayList<>();
    ArrayList<String> stepList=new ArrayList<>();
    FloatingActionButton addIngredientBtn,addStepBtn;
    ConstraintLayout image;
    TextView dietaryBtn;
    boolean[] selectedDiet;
    ArrayList<Integer> dietIntList =new ArrayList<>();
    ArrayList<String>dietaryRecList=new ArrayList<>();
    TextView catBtn,unitBtn;
    IngredientListAdapter ingredientAdapter;
    StepListAdapter stepListAdapter;
    Boolean priv;
    Recipe selectedRecipe=new Recipe();
    SwitchCompat privateswitch;
    Handler handler=new Handler();
    FirebaseUser fbuser;
    User user=new User();
    Button save;
    Context context;
    String uID;
    TextView shareText;
    ListView sharedView;
    ArrayList<User>sharedList;
    ShareListAdapter shareAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//Connect Logic to View/layout
        setContentView(R.layout.activity_create_recipe);

//Get current Users unique ID for database actions
        fbuser=FirebaseAuth.getInstance().getCurrentUser();
        context=getApplicationContext();
        uID= user.getUID(fbuser,context);

//create Arraylists for Alert Dialogs
        String[] dietArray={getResources().getString(R.string.glutenfree),getResources().getString(R.string.lactosefree),getResources().getString(R.string.vegan),getResources().getString(R.string.vegetar),getResources().getString(R.string.paleo),getResources().getString(R.string.lowfat)};
        String[] catArray={getResources().getString(R.string.breakki),getResources().getString(R.string.mainMeal),getResources().getString(R.string.dessert),getResources().getString(R.string.snack),getResources().getString(R.string.soup),getResources().getString(R.string.salad)};
        String[] unitArray={" ","cup","tsp","tbsp","ml","l","g","kg","mg","oz","pound"};

 //assign variables to related Views
        ingredientsView=findViewById(R.id.ingredientlist);
        stepsView=findViewById(R.id.stepList);
        sharedView =findViewById(R.id.sharedList);
        sharedView.setVisibility(View.VISIBLE);
        Button delete=findViewById(R.id.delete);

//Intentfilter for correct Adapter and content depending on source
        Intent previousIntent=getIntent();
//If Edit Recipe has been clicked to lead to this activity
        if (previousIntent.hasExtra("Edit")){
            //get the passed along key of Recipe thats being edited
            String key=previousIntent.getStringExtra("Edit");

//Thread to return Recipe Data and apply to view, waiting to be activated within downloading Thread
            Runnable setDataRun= () -> {
                selectedRecipe=selectedRecipe.getRecipe();
                handler.post(() -> setRecipeData(selectedRecipe,dietArray));
            };
            Thread getRecipeThread=new Thread(setDataRun);
//Thread to download Recipe from Firebase
            Runnable runnable= () -> selectedRecipe.downloadSelectedRecipe(key,context,handler,getRecipeThread,fbuser);
            Thread downloadRThread=new Thread(runnable);
            downloadRThread.start();

 //Making delete button visible and implementing delete-from-firebase method when clicked
            delete.setVisibility(View.VISIBLE);
            delete.setOnClickListener(view -> {
                Runnable removeRun= () -> user.removeRecipe(selectedRecipe,context,handler,fbuser);
                Thread removeThread=new Thread(removeRun);
                removeThread.start();
            });
//making shared-with header visible
            shareText=findViewById(R.id.sharedHeader);
            shareText.setVisibility(View.VISIBLE);
        }else{
//Set up Ingredient List with simple Adapter
            ingredientAdapter= new IngredientListAdapter(getApplicationContext(),0,ingredientList);
            ingredientsView.setAdapter(ingredientAdapter);
//Set up Step List with simple Adapter
            stepListAdapter= new StepListAdapter(getApplicationContext(),0,stepList);
            stepsView.setAdapter(stepListAdapter);
        }

//Assign variables to View-objects
        save = findViewById(R.id.save);
        imageView=findViewById(R.id.uploadImage);
        recipeNameView=findViewById(R.id.recipeName);
        timeView = findViewById(R.id.preptime);
        portionsView=findViewById(R.id.portionen);
        amountView=findViewById(R.id.amount);
        ingredientView=findViewById(R.id.ingredient);
        enterStepView=findViewById(R.id.step);
        addStepBtn=findViewById(R.id.addStepbtn);
        addIngredientBtn=findViewById(R.id.addIngredientBtn);
        image=findViewById(R.id.uploadImageBorder);
        unitBtn=findViewById(R.id.unit);
        privateswitch=findViewById(R.id.privateswitch);



//create Alert Dialog to select Ingredient-Unit when Unit button is clicked (select one)
        unitBtn.setOnClickListener(view -> {
            AlertDialog.Builder builder=new AlertDialog.Builder(CreateRecipeActivity.this);
            builder.setTitle(R.string.chooseUnit);
            builder.setCancelable(false);
            builder.setSingleChoiceItems(unitArray, -1, (dialogInterface, i) -> unit=unitArray[i]);
            //when OK is clicked the selection is applied to button-text
            builder.setPositiveButton("OK", (dialogInterface, i) -> {
                if(unit!=null&&!unit.equals("")){
                    unitBtn.setText(unit);}
                //if nothing is selected user is requested to choose a Unit
                else{
                    Toast.makeText(CreateRecipeActivity.this, R.string.chooseUnit, Toast.LENGTH_SHORT).show();
                }
            });
            builder.show();
        });

//Alert Dialog to select a category (select one
        catBtn=findViewById(R.id.category);
        catBtn.setOnClickListener(view -> {
            AlertDialog.Builder builder=new AlertDialog.Builder(CreateRecipeActivity.this);
            builder.setTitle("Choose category");
            builder.setCancelable(false);
            builder.setSingleChoiceItems(catArray, -1, (dialogInterface, i) -> category=catArray[i]);
            builder.setPositiveButton("OK", (dialogInterface, i) -> {
                //when OK is clicked the selection is applied to button-text
                if(category!=null){
                    catBtn.setText(category);}
                //if nothing is selected user is requested to choose a Category
                else{
                    Toast.makeText(CreateRecipeActivity.this, R.string.chooseCat, Toast.LENGTH_SHORT).show();
                }
            });
            builder.show();
        });

//Alert Dialog to select dietary requirements
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
                    Runnable runnable= this::buildDietString;
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
                priv=true;
            }else{
                privateswitch.setText(R.string.publics);
                priv=false;
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
            Handler handler=new Handler();
            FirebaseUser fbuser= FirebaseAuth.getInstance().getCurrentUser();
            Runnable uploadRunnable= () -> selectedRecipe.uploadUpdate(context,priv,handler,fbuser);
            Thread uploadThread=new Thread(uploadRunnable);
            Runnable setRun= () -> selectedRecipe.setRecipe(imageUri,recipeName,category,time,portions,ingredientList,stepList,dietaryRecList,uploadThread,context,handler,priv,uID);
            Thread setThread=new Thread(setRun);
            setThread.start();
        }
    }

    public void updateListview(){
        getListViewSize(ingredientAdapter,ingredientsView);
    }
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
        handler.post(() -> {
            ViewGroup.LayoutParams params=view.getLayoutParams();
            params.height=newTotal+view.getDividerHeight()*adapter.getCount();
            view.setLayoutParams(params);
        });

    }

    //Insert Data of downloaded Recipe into Create Recipe Views
    public void setRecipeData(Recipe recipe,String[] dietArray){
        String name=recipe.getRecipeName();
        String time=String.valueOf(recipe.getPrepTime());
        String portions=String.valueOf(recipe.getPortions());
        String category=recipe.getCategory();
        handler.post(() -> {
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
            //seperate thread to build dietary string
            Runnable run= this::buildDietString;
            Thread buidThread=new Thread(run);
            buidThread.start();
            setDietInclCheck(dietArray);
            stepList=recipe.getStepList();
            ingredientList=recipe.getIngredientList();
            //get List of Users the recipe was shared with and pass to List-adapter once activated by download-thread
            Database database=new Database();
            Runnable getRun= () -> {
                sharedList=new ArrayList<>();
                sharedList=database.getUsers();
                shareAdapter=new ShareListAdapter(context,0,sharedList,recipe,handler);
                handler.post(() -> {
                    sharedView.setAdapter(shareAdapter);
                    getListViewSize(shareAdapter,sharedView);
                });
            };
            Thread getUserListThread=new Thread(getRun);
            //start new Thread to get Users the recipe was shared with from firebase
            database.setSharedWithUsers(recipe.getSharedWith(),handler,context,getUserListThread);

            //setup adapters for lists and pass acquired Lists
            editIngredientAdapter ingredientAdapter = new editIngredientAdapter(getApplicationContext(), 0,ingredientList);
            EditStepAdapter stepAdapter = new EditStepAdapter(getApplicationContext(), 0,stepList);
            ingredientsView.setAdapter(ingredientAdapter);
            stepsView.setAdapter(stepAdapter);
            //recalculate ListSize to accomodate Scrollview
            getListViewSize(stepAdapter,stepsView);
            getListViewSize(ingredientAdapter,ingredientsView);

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

 //Create one String containing all dietary requirements, shortened and separated by "|"
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
       handler.post(() -> dietaryBtn.setText(text));
   }




    @Override
    public void onBackPressed(){
       Intent toMainIntent=new Intent(CreateRecipeActivity.this,MainActivity.class);
       startActivity(toMainIntent);
       finish();
    }

}