package View;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import android.content.Intent;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bienhuels.iwmb_cookdome.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.Collections;
import Model.Ingredient;
import Model.Recipe;
import Viewmodel.CustomComparator;
import Viewmodel.SearchAdapters.RecipeAdapter;
import Viewmodel.SearchAdapters.RecyclerAdapterCat;
import Viewmodel.SearchAdapters.RecyclerAdapterDietary;
import Viewmodel.SearchAdapters.RecyclerAdapterLo;

public class SearchActivity extends AppCompatActivity {
    RecyclerView recipeSearchView;
    FloatingActionButton filter;
    DatabaseReference databaseReference, dbRefUsers;
    FirebaseDatabase database;
    FirebaseAuth auth;
    String id;
    ConstraintLayout filterContainer;
    SearchView searchView;
    String source;
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
    ArrayList<String>favlist,ownlist;
    Recipe selectedRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getUserFav();
        setContentView(R.layout.activity_search);


//Intentfilter to see which activity the user is coming from
        Intent previousIntent = getIntent();
        //User selected a category
        if (previousIntent.hasExtra("filter")) {
            String catFilter = previousIntent.getStringExtra("filter");
            selectedCategoryList.add(catFilter);
            source="categories";
            fetchList(catFilter, source);
        }
        //User clicked leftovers Button
        if (previousIntent.hasExtra("action")) {
            source="leftovers";
            String catFilter="";
            fetchList(catFilter,source);
        }
        //User clicked search Icon
        if(previousIntent.hasExtra("search")) {
            setupList();
        }
        //User clicked Own-recipes
        if(previousIntent.hasExtra("select")){
            source=previousIntent.getStringExtra("select");
            if(source.equals("ownRecipes")){
                getUserOwn();
            }
        }
//Searchfilter
        filter = findViewById(R.id.filterBtn);
        filterContainer = findViewById(R.id.filterContainer);
        cancel = findViewById(R.id.cancel);
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
        //Timepicker
        seekBar =  findViewById(R.id.timeselect);
        valueView =  findViewById(R.id.valueView);
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

//unfold categories
        extendBtn1 = findViewById(R.id.extendBtn1);
        extendBtn2 = findViewById(R.id.extendBtn2);
        catSelect =  findViewById(R.id.catSelect);
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
        diatarySelect = findViewById(R.id.diatarySelect);
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

//Category Checkboxes (Add selected categories to displayed list)
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        chosenCat = findViewById(R.id.chosenCat);
        chosenCat.setLayoutManager(layoutManager);
        catRecyclerAdapter = new RecyclerAdapterCat(getApplicationContext(), selectedCategoryList);
        chosenCat.setAdapter(catRecyclerAdapter);
        checkBox = findViewById(R.id.breakki);
        CheckBox checkBox1 = findViewById(R.id.soup);
        CheckBox checkBox2 = findViewById(R.id.snack);
        CheckBox checkBox3 = findViewById(R.id.mainmeal);
        CheckBox checkBox4 = findViewById(R.id.salad);
        CheckBox checkBox5 = findViewById(R.id.dessert);
        checkBox.setOnClickListener(view -> onCheck(checkBox, getResources().getString(R.string.breakki), catRecyclerAdapter));
        checkBox1.setOnClickListener(view -> onCheck(checkBox1, getResources().getString(R.string.soup), catRecyclerAdapter));
        checkBox2.setOnClickListener(view -> onCheck(checkBox2, getResources().getString(R.string.snack), catRecyclerAdapter));
        checkBox3.setOnClickListener(view -> onCheck(checkBox3, getResources().getString(R.string.mainMeal), catRecyclerAdapter));
        checkBox4.setOnClickListener(view -> onCheck(checkBox4, getResources().getString(R.string.salad), catRecyclerAdapter));
        checkBox5.setOnClickListener(view -> onCheck(checkBox5, getResources().getString(R.string.dessert), catRecyclerAdapter));


// Dietary Checkboxes (Add selected Dietary Requirements to displayed List)
        GridLayoutManager layoutManagerDietary = new GridLayoutManager(this, 3);
        layoutManagerDietary.setOrientation(LinearLayoutManager.VERTICAL);
        chosendietaryRec = findViewById(R.id.chosendietaryRec);
        chosendietaryRec.setLayoutManager(layoutManagerDietary);
        dietaryRecyclerAdapter = new RecyclerAdapterDietary(getApplicationContext(), selectedDietaryRecList);
        chosendietaryRec.setAdapter(dietaryRecyclerAdapter);
        checkBox = findViewById(R.id.breakki);
        CheckBox cbGluten = findViewById(R.id.gluten);
        CheckBox cbLactose = findViewById(R.id.lactose);
        CheckBox cbVegan = findViewById(R.id.vegan);
        CheckBox cbVege = findViewById(R.id.vege);
        CheckBox cbPaleo = findViewById(R.id.paleo);
        CheckBox cbFettarm = findViewById(R.id.lowfat);
        cbGluten.setOnClickListener(view -> onCheckDietary(cbGluten, getResources().getString(R.string.glutenfree), dietaryRecyclerAdapter));
        cbLactose.setOnClickListener(view -> onCheckDietary(cbLactose, getResources().getString(R.string.lactosefree), dietaryRecyclerAdapter));
        cbVegan.setOnClickListener(view -> onCheckDietary(cbVegan, getResources().getString(R.string.vegan), dietaryRecyclerAdapter));
        cbVege.setOnClickListener(view -> onCheckDietary(cbVege, getResources().getString(R.string.vegetar), dietaryRecyclerAdapter));
        cbFettarm.setOnClickListener(view -> onCheckDietary(cbFettarm, getResources().getString(R.string.lowfat), dietaryRecyclerAdapter));
        cbPaleo.setOnClickListener(view -> onCheckDietary(cbPaleo, getResources().getString(R.string.paleo), dietaryRecyclerAdapter));

//Leftovers selection
        leftoverlistView=findViewById(R.id.leftoverList);
        LinearLayoutManager linLayoutManager=new LinearLayoutManager(getApplicationContext(),RecyclerView.VERTICAL,false);
        leftoverlistView.setLayoutManager(linLayoutManager);
        leftoverlistView= findViewById(R.id.leftoverList);
        loRecyclerAdapter=new RecyclerAdapterLo(getApplicationContext(),leftoverList);
        leftoverlistView.setAdapter(loRecyclerAdapter);
        insertIngredient = findViewById(R.id.insertIngredient);
        addIngredientFilter = findViewById(R.id.addIngredientFilter);
        rowsize=3;
        addIngredientFilter.setOnClickListener(view -> {
            String lo=insertIngredient.getText().toString();
            leftoverList.add(lo.toLowerCase());
            loRecyclerAdapter.notifyItemInserted(leftoverList.indexOf(lo.toLowerCase()));
            insertIngredient.setText("");
        });
//Filterbutton
        applyFilter = findViewById(R.id.filter);

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
//Search Function
        //pass entered String to Method typeFilter() at each text-change
        searchView = findViewById(R.id.search);
        searchView.clearFocus();
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
            Toast.makeText(this,R.string.noMatch,Toast.LENGTH_SHORT).show();
        }else{
            recipeAdapter.searchList(filteredList);
        }
    }
    //retreive recipes from firebase that meet the source criteria
    private void fetchList(String catFilter,String source){
        currentList=new ArrayList<>();
        recyclerconfig(currentList);
        databaseReference.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    for(DataSnapshot dsS:snapshot.getChildren()){
                        createRecipe(dsS);
                        if(source.equals("categories")){
                            if (selectedRecipe.getCategory().equals(catFilter)) {
                                currentList.add(selectedRecipe);
                            }
                        }
                        if(source.equals("leftovers")){
                            Intent previousIntent=getIntent();
                            leftoverList=previousIntent.getStringArrayListExtra("action");
                            ArrayList<String>ingredientStringList=new ArrayList<>();
                            for(Ingredient ingredient:selectedRecipe.getIngredientList()){
                                String name=ingredient.getIngredientName();
                                ingredientStringList.add(name);
                            }
                            if(ingredientStringList.containsAll(leftoverList)){
                                Log.d(TAG, " applied");

                            } else{
                                continue;}
                            currentList.add(selectedRecipe);
                        }
                    }
                    Collections.sort(currentList,new CustomComparator());
                    recipeAdapter.notifyItemInserted(currentList.indexOf(selectedRecipe));
                    if(source.equals("categories")){
                        if(currentList.isEmpty()){
                            Toast.makeText(SearchActivity.this,R.string.noMatch,Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(source.equals("leftovers")){
                        if(currentList.isEmpty()){
                            Toast.makeText(SearchActivity.this,R.string.noMatch,Toast.LENGTH_SHORT).show();
                        }
                    }
                    Toast.makeText(SearchActivity.this,R.string.retreived,Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(SearchActivity.this,R.string.dBEmpty,Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(SearchActivity.this,R.string.dataRetrievalFailed,Toast.LENGTH_SHORT).show();
            }
            Log.d(TAG, currentList.toString());
        });
    }

    //Retreive All recipes from firebase and display them
    private void setupList() {
        currentList=new ArrayList<>();
        recyclerconfig(currentList);
        databaseReference.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    for(DataSnapshot dsS:snapshot.getChildren()){
                        createRecipe(dsS);
                        currentList.add(selectedRecipe);
                    }
                    Collections.sort(currentList,new CustomComparator());
                    recipeAdapter.notifyDataSetChanged();
                    Toast.makeText(SearchActivity.this,R.string.retreived,Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(SearchActivity.this,R.string.dBEmpty,Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(SearchActivity.this,R.string.dataRetrievalFailed,Toast.LENGTH_SHORT).show();
            }
        });


    }
    //Create a List of selected Checkbox Items and update the view accordingly
    //Category Checkboxes
//Function to add checked items to List and remove them when theyre unchecked
    private void onCheck(CheckBox checkbox,String selectedFilter,RecyclerAdapterCat recyclerAdapter){
        if(checkbox.isChecked()){
            selectedCategoryList.add(selectedFilter);
            recyclerAdapter.notifyItemInserted(selectedCategoryList.indexOf(selectedFilter));
        }
        else{
            selectedCategoryList.remove(selectedFilter);
            recyclerAdapter.notifyDataSetChanged();
        }
    }
    //Dietary Checkboxes
    //Function to add checked items to List and remove them when theyre unchecked
    private void onCheckDietary(CheckBox checkbox,String selectedFilter,RecyclerAdapterDietary recyclerAdapter){
        if(checkbox.isChecked()){
            selectedDietaryRecList.add(selectedFilter);
            recyclerAdapter.notifyItemInserted(selectedDietaryRecList.indexOf(selectedFilter));
        }
        else{
            selectedDietaryRecList.remove(selectedFilter);
            recyclerAdapter.notifyDataSetChanged();
        }
    }
    //Applying selected Filters to filter the displayed Items
    private void filterSearchList(Integer time,ArrayList<String> categories,ArrayList<String> dietary,ArrayList<String> ingredients,ArrayList<Recipe> filteredRecipes) {
        filteredRecipes.clear();
        if(dietary.contains(getResources().getString(R.string.vegan))&&dietary.contains(getResources().getString(R.string.vegetar))){
            dietary.remove(getResources().getString(R.string.vegetar));
        }
        for(Recipe recipe:currentList){
            Log.d("Recipe", recipe.getRecipeName());
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
                    String name=ingredient.getIngredientName();
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
    //Generate a list of Keys to the Recipes the user liked
    public void getUserFav() {
        database = FirebaseDatabase.getInstance();
        dbRefUsers = database.getReference("/Cookdome/Users");
        auth= FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
//Send User to sign in if no current user found
        if (currentUser == null) {
            Toast.makeText(SearchActivity.this, R.string.signedOut, Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(SearchActivity.this, LoginActivity.class);
            startActivity(loginIntent);
        }
//Otherwise use UserID to find liked Recipes and add their keys to the List favlist
        else {
            id = currentUser.getUid();
            favlist = new ArrayList<>();
            dbRefUsers.child(id).child("Favourites").get().addOnCompleteListener(task -> {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    // dBRecipeList= snapshot.getValue(listType);
                    for (DataSnapshot dsS : snapshot.getChildren()) {
                        String favkey = dsS.getKey();
                        favlist.add(favkey);
                    }
                    if(source!=null){
                        if(source.equals("likedRecipes")){
                            getOwnFavList(favlist);}
                    }
                }
            }).addOnFailureListener(e -> Toast.makeText(SearchActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
    //Creating a list of Keys to the Recipes the user created themselves
    public void getUserOwn() {
        database = FirebaseDatabase.getInstance();
        dbRefUsers = database.getReference("/Cookdome/Users");
        auth= FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
//Send to login if User not found
        if (currentUser == null) {
            Toast.makeText(SearchActivity.this, R.string.signedOut, Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(SearchActivity.this, LoginActivity.class);
            startActivity(loginIntent);
//If user found, use userID to create the List of Keys from Firebase
        } else {
            id = currentUser.getUid();
            ownlist = new ArrayList<>();
            //In case of Issues with the download a case specific Error message is displayed to the user
            dbRefUsers.child(id).child("Own").get().addOnCompleteListener(task -> {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    for (DataSnapshot dsS : snapshot.getChildren()) {
                        String ownkey = dsS.getKey();
                        ownlist.add(ownkey);
                    }
                    getOwnFavList(ownlist);
                }
            }).addOnFailureListener(e -> Toast.makeText(SearchActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
    //Download and display a List of Recipes that the User either liked or created
    public void getOwnFavList(ArrayList<String> keylist){
        currentList=new ArrayList<>();
        recyclerconfig(currentList);
        for(String key:keylist){
            databaseReference.child(key).get().addOnCompleteListener(task -> {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    createRecipe(snapshot);
                    currentList.add(selectedRecipe);
                    recipeAdapter.notifyItemInserted(currentList.indexOf(selectedRecipe));
                }else{
                    dbRefUsers.child(id).child("Privates").child(key).get().addOnCompleteListener(task1 -> {
                        if (task1.getResult().exists()) {
                            DataSnapshot snapshot2 = task1.getResult();
                            createRecipe(snapshot2);
                            currentList.add(selectedRecipe);
                            recipeAdapter.notifyItemInserted(currentList.indexOf(selectedRecipe));
                        }
                    }).addOnFailureListener(e -> Toast.makeText(SearchActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
                }

            }).addOnFailureListener(e2 -> Toast.makeText(SearchActivity.this, e2.getMessage(), Toast.LENGTH_SHORT).show());;
        }
    }

    //Mapping the firebase Data structure of a Recipe back to a recipe-Object
    public void createRecipe(DataSnapshot dsS){
        String dBKey = dsS.child("key").getValue(String.class);
        String dBrecipeName = dsS.child("recipeName").getValue(String.class);
        String dBcat = String.valueOf(dsS.child("category").getValue());
        int dBprepTime = Integer.parseInt(String.valueOf(dsS.child("prepTime").getValue()));
        int dBportions = Integer.parseInt(String.valueOf(dsS.child("portions").getValue()));
        String dBImage = dsS.child("image").getValue(String.class);
        ArrayList<String> dBstepList = new ArrayList<>();
        String index="0";
        for(DataSnapshot stepSS:dsS.child("stepList").getChildren()){
            String stepTry=String.valueOf(dsS.child("stepList").child(index).getValue());
            dBstepList.add(stepTry);
            int i=Integer.parseInt(index);
            i++;
            index= Integer.toString(i);
        }
        String index2="0";
        ArrayList<String> dBdietList = new ArrayList<>();
        for(DataSnapshot stepSS:dsS.child("dietRec").getChildren()){
            String dietTry=String.valueOf(dsS.child("dietRec").child(index2).getValue());
            int i=Integer.parseInt(index2);
            i++;
            index2= Integer.toString(i);
            dBdietList.add(dietTry);
        }
        ArrayList<Ingredient>dBIngredientList=new ArrayList<>();
        for(DataSnapshot IngSS:dsS.child("ingredientList").getChildren()){
            Double amount=IngSS.child("amount").getValue(Double.class);
            if(amount==null){
                amount=0.0;
            }
            String unit=IngSS.child("unit").getValue(String.class);
            String ingredientName=IngSS.child("ingredientName").getValue(String.class);

            Ingredient ingredient=new Ingredient(amount,unit,ingredientName);
                dBIngredientList.add(ingredient);}

        selectedRecipe = new Recipe(dBKey, dBImage, dBrecipeName, dBcat, dBprepTime, dBportions, dBIngredientList, dBstepList,dBdietList);
    }
    //Configuring the Recyclerview to display the given List of Recipes
    public void recyclerconfig(ArrayList<Recipe> list){
        recipeSearchView= findViewById(R.id.recipeSearchView);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("/Cookdome/Recipes");
        GridLayoutManager layoutManagerSearch=new GridLayoutManager(this,2);
        layoutManagerSearch.setOrientation(LinearLayoutManager.VERTICAL);
        recipeSearchView.setLayoutManager(layoutManagerSearch);
        recipeAdapter = new RecipeAdapter(getApplicationContext(),list,favlist,id);
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