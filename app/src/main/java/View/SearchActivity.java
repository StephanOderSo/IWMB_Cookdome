package View;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bienhuels.iwmb_cookdome.R;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;

import Model.Firebase;
import Model.Ingredient;
import Model.Recipe;
import Viewmodel.SearchAdapters.RecipeAdapter;
import Viewmodel.SearchAdapters.RecyclerAdapterCat;
import Viewmodel.SearchAdapters.RecyclerAdapterDietary;
import Viewmodel.SearchAdapters.RecyclerAdapterLo;
import Viewmodel.Tools;

public class SearchActivity extends AppCompatActivity {
    RecyclerView recipeSearchView;
    FloatingActionButton filter;
    String id;
    ConstraintLayout filterContainer;
    SearchView searchView;
    String source="";
    public Integer time;
    SeekBar seekBar;
    TextView valueView;
    ImageButton extendBtn1,extendBtn2,addIngredientFilter;
    Integer clickCount=1, clickCount2=1,rowsize;
    RecyclerView leftoverlistView;
    EditText insertIngredient;
    CheckBox checkBox;
    Button applyFilter,cancel;
    LinearLayout diatarySelect,catSelect;
    RecyclerView chosenCat,chosendietaryRec;
    RecyclerAdapterCat catRecyclerAdapter;
    RecyclerAdapterDietary dietaryRecyclerAdapter;
    RecyclerAdapterLo loRecyclerAdapter;
    RecipeAdapter recipeAdapter;
    public ArrayList<String> selectedCategoryList= new ArrayList<>();
    public ArrayList<String> selectedDietaryRecList=new ArrayList<>();
    public ArrayList<String> leftoverList = new ArrayList<>();
    ArrayList<Recipe>currentList;
    Context context;
    Handler handler =new Handler();
    FirebaseUser fbUser;
    Firebase firebase =new Firebase();
    Thread listThread;
    Intent previousIntent;
    Tools tools=new Tools();
    CheckBox checkBox1,checkBox2,checkBox3,checkBox4,checkBox5,cbGluten,cbLactose,cbVegan,cbVege,cbPaleo,cbFettarm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        context=getApplicationContext();
        fbUser=FirebaseAuth.getInstance().getCurrentUser();
        searchView = findViewById(R.id.search);
        searchView.clearFocus();
        recipeSearchView= findViewById(R.id.recipeSearchView);
        filter = findViewById(R.id.filterBtn);
        filterContainer = findViewById(R.id.filterContainer);
        seekBar =  findViewById(R.id.timeselect);
        valueView =  findViewById(R.id.valueView);
        cancel = findViewById(R.id.cancel);
        extendBtn1 = findViewById(R.id.extendBtn1);
        extendBtn2 = findViewById(R.id.extendBtn2);
        catSelect =  findViewById(R.id.catSelect);
        diatarySelect = findViewById(R.id.diatarySelect);
        applyFilter = findViewById(R.id.filter);

        //Category Checkboxes
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        chosenCat = findViewById(R.id.chosenCat);
        chosenCat.setLayoutManager(layoutManager);
        catRecyclerAdapter = new RecyclerAdapterCat(getApplicationContext(), selectedCategoryList);
        chosenCat.setAdapter(catRecyclerAdapter);
        checkBox = findViewById(R.id.breakki);
        checkBox1 = findViewById(R.id.soup);
        checkBox2 = findViewById(R.id.snack);
        checkBox3 = findViewById(R.id.mainmeal);
        checkBox4 = findViewById(R.id.salad);
        checkBox5 = findViewById(R.id.dessert);

// Dietary Checkboxes
        GridLayoutManager layoutManagerDietary = new GridLayoutManager(this, 3);
        layoutManagerDietary.setOrientation(LinearLayoutManager.VERTICAL);
        chosendietaryRec = findViewById(R.id.chosendietaryRec);
        chosendietaryRec.setLayoutManager(layoutManagerDietary);
        dietaryRecyclerAdapter = new RecyclerAdapterDietary(getApplicationContext(), selectedDietaryRecList);
        chosendietaryRec.setAdapter(dietaryRecyclerAdapter);
        checkBox = findViewById(R.id.breakki);
        cbGluten = findViewById(R.id.gluten);
        cbLactose = findViewById(R.id.lactose);
        cbVegan = findViewById(R.id.vegan);
        cbVege = findViewById(R.id.vege);
        cbPaleo = findViewById(R.id.paleo);
        cbFettarm = findViewById(R.id.lowfat);
//Leftovers selection
        leftoverlistView=findViewById(R.id.leftoverList);
        LinearLayoutManager linLayoutManager=new LinearLayoutManager(getApplicationContext(),RecyclerView.VERTICAL,false);
        leftoverlistView.setLayoutManager(linLayoutManager);
        leftoverlistView= findViewById(R.id.leftoverList);
        loRecyclerAdapter=new RecyclerAdapterLo(getApplicationContext(),leftoverList);
        leftoverlistView.setAdapter(loRecyclerAdapter);
        insertIngredient = findViewById(R.id.insertIngredient);
        addIngredientFilter = findViewById(R.id.addIngredientFilter);
//chose layoutmanager depending on orientation
        if(this.getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE){
            GridLayoutManager layoutManagerSearch=new GridLayoutManager(this,3);
            layoutManagerSearch.setOrientation(LinearLayoutManager.VERTICAL);
            recipeSearchView.setLayoutManager(layoutManagerSearch);
        }else{
            GridLayoutManager layoutManagerSearch=new GridLayoutManager(this,2);
            layoutManagerSearch.setOrientation(LinearLayoutManager.VERTICAL);
            recipeSearchView.setLayoutManager(layoutManagerSearch);
        }
//Set up List depending on source
        setUpList();

        //category checkboxes  (Add selected categories to displayed list)
        checkBox.setOnClickListener(view -> tools.onCheck(checkBox, getResources().getString(R.string.breakki), catRecyclerAdapter,selectedCategoryList));
        checkBox1.setOnClickListener(view -> tools.onCheck(checkBox1, getResources().getString(R.string.soup), catRecyclerAdapter,selectedCategoryList));
        checkBox2.setOnClickListener(view -> tools.onCheck(checkBox2, getResources().getString(R.string.snack), catRecyclerAdapter,selectedCategoryList));
        checkBox3.setOnClickListener(view -> tools.onCheck(checkBox3, getResources().getString(R.string.mainMeal), catRecyclerAdapter,selectedCategoryList));
        checkBox4.setOnClickListener(view -> tools.onCheck(checkBox4, getResources().getString(R.string.salad), catRecyclerAdapter,selectedCategoryList));
        checkBox5.setOnClickListener(view -> tools.onCheck(checkBox5, getResources().getString(R.string.dessert), catRecyclerAdapter,selectedCategoryList));
        //dietary checkboxes  (Add selected diets to displayed list)
        cbGluten.setOnClickListener(view -> tools.onCheck(cbGluten, getResources().getString(R.string.glutenfree), dietaryRecyclerAdapter,selectedDietaryRecList));
        cbLactose.setOnClickListener(view -> tools.onCheck(cbLactose, getResources().getString(R.string.lactosefree), dietaryRecyclerAdapter,selectedDietaryRecList));
        cbVegan.setOnClickListener(view -> tools.onCheck(cbVegan, getResources().getString(R.string.vegan), dietaryRecyclerAdapter,selectedDietaryRecList));
        cbVege.setOnClickListener(view -> tools.onCheck(cbVege, getResources().getString(R.string.vegetar), dietaryRecyclerAdapter,selectedDietaryRecList));
        cbFettarm.setOnClickListener(view -> tools.onCheck(cbFettarm, getResources().getString(R.string.lowfat), dietaryRecyclerAdapter,selectedDietaryRecList));
        cbPaleo.setOnClickListener(view -> tools.onCheck(cbPaleo, getResources().getString(R.string.paleo), dietaryRecyclerAdapter,selectedDietaryRecList));
//Searchfilter
        cancel.setContentDescription("visible");
        filter.setOnClickListener(view -> {
            filter.setVisibility(View.GONE);
            applyFilter.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.VISIBLE);
            cancel.setContentDescription("visible");
            filterContainer.setVisibility(View.VISIBLE);
            searchView = findViewById(R.id.search);
            searchView.setVisibility(View.GONE);
            recipeSearchView = findViewById(R.id.recipeSearchView);
            recipeSearchView.setVisibility(View.GONE);
        });
//unfold categories
        extendBtn1.setOnClickListener(view -> {
            extendBtn1.setImageResource(R.drawable.arrow_up);
            if (clickCount2 % 2 != 0) {
                catSelect.setVisibility(View.VISIBLE);

            } else {
                extendBtn1.setImageResource(R.drawable.arrow_down);
                catSelect.setVisibility(View.GONE);
            }
            clickCount2++;
        });
//Unfold Dietary selection
        extendBtn2.setOnClickListener(view -> {
            if (clickCount % 2 != 0) {
                extendBtn2.setImageResource(R.drawable.arrow_up);
                diatarySelect.setVisibility(View.VISIBLE);
            } else {
                extendBtn2.setImageResource(R.drawable.arrow_down);
                diatarySelect.setVisibility(View.GONE);
            }
            clickCount++;
        });

//filter for leftovers
        rowsize=3;
        addIngredientFilter.setOnClickListener(view -> {
            if(insertIngredient.getText()!=null){
                if(!insertIngredient.getText().toString().equals("")){
                    String lo=insertIngredient.getText().toString();
                    leftoverList.add(lo.toLowerCase());
                    loRecyclerAdapter.notifyItemInserted(leftoverList.indexOf(lo.toLowerCase()));
                    insertIngredient.setText("");
                }else{
                    Toast.makeText(context, R.string.enterLeftover, Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(context, R.string.enterLeftover, Toast.LENGTH_SHORT).show();
            }
        });
//Filterbutton
        applyFilter.setOnClickListener(view -> {
            ArrayList<Recipe> filteredRecipes = new ArrayList<>();
            filterSearchList(time, selectedCategoryList, selectedDietaryRecList, leftoverList, filteredRecipes);
            filter.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.GONE);
            cancel.setContentDescription("gone");
            applyFilter.setVisibility(View.GONE);
            filterContainer.setVisibility(View.GONE);
            searchView.setVisibility(View.VISIBLE);
            recipeSearchView.setVisibility(View.VISIBLE);
        });
//Cancel Button
        cancel.setOnClickListener(view -> {
            filter.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.GONE);
            cancel.setContentDescription("gone");
            applyFilter.setVisibility(View.GONE);
            filterContainer.setVisibility(View.GONE);
            searchView.setVisibility(View.VISIBLE);
            recipeSearchView.setVisibility(View.VISIBLE);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Timepicker
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                valueView.setText(String.valueOf(progress));
                time = progress;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        id=firebase.returnID(fbUser,context);
        //Intentfilter to see which activity the user is coming from (source)
        previousIntent = getIntent();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    //Filter Current list based on given String and display result
    private void typeFilter(String text){
        ArrayList<Recipe>filteredList=new ArrayList<>();
        for(Recipe recipe:currentList){
            if(recipe.getRecipeName().toLowerCase().trim().contains(text.toLowerCase())){
                filteredList.add(recipe);
            }
        }
        if(filteredList.isEmpty()){
             Toast.makeText(SearchActivity.this,R.string.noMatch,Toast.LENGTH_SHORT).show();

        }else{
           recipeAdapter.searchList(filteredList);
        }
    }

    //Applying selected Filters to filter the displayed Items
    private void filterSearchList(Integer time,ArrayList<String> categories,ArrayList<String> dietary,ArrayList<String> ingredients,ArrayList<Recipe> filteredRecipes) {
        filteredRecipes.clear();
        if(dietary.contains(getResources().getString(R.string.vegan))&&dietary.contains(getResources().getString(R.string.vegetar))){
            dietary.remove(getResources().getString(R.string.vegetar));
        }
        for(Recipe recipe:currentList){
            if (time != null){
                if(recipe.getPrepTime()<=time){
                    Log.d("timeFilter", "applied");
                }else{
                    Log.d("filter", "time didnt match");
                    continue;}}
            if(!categories.isEmpty()){
                if (categories.contains(recipe.getCategory())) {
                    Log.d("CatFilter", "applied");
                } else {
                    Log.d("filter", "cat didnt match");
                    continue;}}
            if(!dietary.isEmpty()){
                if (recipe.getDietaryRec().containsAll(dietary)) {
                    Log.d("DietFilter", "applied");
                } else {
                    Log.d("filter", "diet didnt match");
                    continue;}}
            if(!ingredients.isEmpty()){
                ArrayList<String>ingredientStringList=new ArrayList<>();
                for(Ingredient ingredient:recipe.getIngredientList()){
                    String name=ingredient.getName();
                    ingredientStringList.add(name);
                }
                if(ingredientStringList.containsAll(ingredients)){
                    Log.d("IngredientFilter", "applied");
                } else{
                    continue;}}
            filteredRecipes.add(recipe);
        }
        if(filteredRecipes.isEmpty()){
            Toast.makeText(this, R.string.noMatch, Toast.LENGTH_SHORT).show();
        }else{
            recipeAdapter.searchList(filteredRecipes);
        }
    }
    //Search Function
    //pass entered String to Method typeFilter() at each text-change
    public void setUpList(){
        Runnable recyclerRunnable= () -> handler.post(() -> {
            currentList=firebase.returnRecipes();
            recyclerconfig(currentList);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    typeFilter(s);
                    return false;
                }
            });
        });
        Thread recyclerThread=new Thread(recyclerRunnable);
        Runnable ownRunnable= (() -> {firebase.getFavouriteOrOwnRecipes(firebase.getUser().getOwnRecipes(),context,id, handler,recyclerThread);});
        Thread ownThread=new Thread(ownRunnable);

        Runnable getSharedList= () -> {
            currentList= firebase.returnRecipes();
            recyclerThread.start();
        };
        Thread getSharedListThread=new Thread(getSharedList);

        Runnable listRunnable= () -> {
            // User clicked search Icon
            if(previousIntent.hasExtra("search")) {
                firebase.getAllRecipes(context, handler,recyclerThread);
            }
            //User selected a category
            if (previousIntent.hasExtra("filter")) {
                String catFilter = previousIntent.getStringExtra("filter");
                selectedCategoryList.add(catFilter);
                source="categories";
                firebase.getSelectedRecipes(catFilter,source,context,previousIntent, handler,recyclerThread);
            }
            //User clicked leftovers Button
            if (previousIntent.hasExtra("leftovers")) {
                source="leftovers";
                ArrayList<String> lo=previousIntent.getStringArrayListExtra("leftovers");
                Log.d("TAG", lo.toString());
                String catFilter="";
                firebase.getSelectedRecipes(catFilter,source,context,previousIntent, handler,recyclerThread);
            }
            //User clicked Own-recipes/liked Recipes
            if(previousIntent.hasExtra("select")){
                source=previousIntent.getStringExtra("select");
                if(source!=null){
                    if(source.equals("ownRecipes")){
                        firebase.setOwnRecipeKeys(context,fbUser, handler,ownThread);

                    }
                    if(source.equals("likedRecipes")){
                        firebase.getFavouriteOrOwnRecipes(firebase.getUser().getFavouriteRecipes(),context,id, handler,recyclerThread);
                    }
                }
            }
            if(previousIntent.hasExtra("shared")){
                source="shared";
                firebase.getSharedRecipes(context,fbUser,handler,getSharedListThread);
            }
        };
        listThread=new Thread(listRunnable);

        Runnable favRunnable= () -> firebase.setFavouriteRecipeKeys(context,fbUser, handler,listThread);
        Thread favThread=new Thread(favRunnable);
        favThread.start();
    }

    //Configuring the Recyclerview to display the given List of Recipes
    public void recyclerconfig(ArrayList<Recipe> list){
        ArrayList<String> stringArray=new ArrayList<>(Arrays.asList(getResources().getString(R.string.vegetar),getResources().getString(R.string.vegan),getResources().getString(R.string.glutenfree),getResources().getString(R.string.lactosefree),getResources().getString(R.string.paleo),getResources().getString(R.string.lowfat)));
        recipeAdapter = new RecipeAdapter(getApplicationContext(),list,firebase.getUser().getFavouriteRecipes(),id,source,stringArray,firebase);
        recipeSearchView.setAdapter(recipeAdapter);
    }
    //If user presses return on their phone he is lead back to the main activity
    @Override
    public void onBackPressed() {
        if(cancel.getContentDescription().equals("gone")){
            Intent toMainIntent=new Intent(SearchActivity.this, MainActivity.class);
            startActivity(toMainIntent);}
        if(cancel.getContentDescription().equals("visible")){
            filter.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.GONE);
            cancel.setContentDescription("gone");
            applyFilter.setVisibility(View.GONE);
            filterContainer.setVisibility(View.GONE);
            searchView.setVisibility(View.VISIBLE);
            recipeSearchView.setVisibility(View.VISIBLE);
        }

    }
}