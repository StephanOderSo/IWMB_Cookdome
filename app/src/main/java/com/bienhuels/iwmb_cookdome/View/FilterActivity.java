package View;

import static android.view.View.GONE;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bienhuels.iwmb_cookdome.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Random;

import Model.Ingredient;
import Model.Recipe;
import Viewmodel.SearchAdapters.RecyclerAdapterCat;
import Viewmodel.SearchAdapters.RecyclerAdapterDietary;
import Viewmodel.SearchAdapters.RecyclerAdapterLo;

public class FilterActivity extends AppCompatActivity {
    public Integer time,rowsize;
    SeekBar seekBar;
    TextView valueView;
    ImageButton extendBtn1;
    ImageButton extendBtn2;
    ImageButton addIngredientFilter;
    Integer clickCount=1, clickCount2=1;

    RecyclerView restelistView;
    EditText insertIngredient;
    CheckBox checkBox;
    Button applyFilter;
    LinearLayout diatarySelect;
    LinearLayout catSelect;
    RecyclerView chosenCat;
    RecyclerView chosendietaryRec;
    RecyclerAdapterCat catRecyclerAdapter;
    RecyclerAdapterLo loRecyclerAdapter;
    RecyclerAdapterDietary dietaryRecyclerAdapter;
    public ArrayList<String> selectedCategoryList= new ArrayList<>();
    public ArrayList<String> selectedDietaryRecList= new ArrayList<>();
    public static ArrayList<String> resteList = new ArrayList<>();
    ArrayList<Recipe> dBRecipeList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        Intent previousIntent=getIntent();
        if(previousIntent.hasExtra("action")){
            ConstraintLayout subContainer=findViewById(R.id.subContainer);
            subContainer.setVisibility(GONE);
            CardView headerCard=findViewById(R.id.headercard);
            headerCard.setVisibility(View.VISIBLE);
        }
//Timepicker
        seekBar= findViewById(R.id.timeselect);
        valueView= findViewById(R.id.valueView);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                valueView.setText(String.valueOf(progress));
                time=progress;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

//KategorienCheckliste entfalten

        extendBtn1=findViewById(R.id.extendBtn1);
        extendBtn2=findViewById(R.id.extendBtn2);
        catSelect= findViewById(R.id.catSelect);
        extendBtn1.setOnClickListener(view -> {
            extendBtn1.setImageResource(R.drawable.arrow_up);
            if (clickCount2%2 !=0){
                catSelect.setVisibility(View.VISIBLE);
            } else {extendBtn1.setImageResource(R.drawable.arrow_down);
                catSelect.setVisibility(GONE);

            }
            clickCount2++;
        });
//ErnaehrungsCheckliste entfalten
        diatarySelect= findViewById(R.id.diatarySelect);
        extendBtn2.setOnClickListener(view -> {
            if (clickCount%2 !=0){
                extendBtn2.setImageResource(R.drawable.arrow_up);
                diatarySelect.setVisibility(View.VISIBLE);

            } else {extendBtn2.setImageResource(R.drawable.arrow_down);
                diatarySelect.setVisibility(GONE);

            }
            clickCount++;
        });

//Kategorie Checkboxen
        GridLayoutManager layoutManager=new GridLayoutManager(this,3);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        chosenCat=findViewById(R.id.chosenCat);
        chosenCat.setLayoutManager(layoutManager);
        catRecyclerAdapter=new RecyclerAdapterCat(getApplicationContext(),selectedCategoryList);
        chosenCat.setAdapter(catRecyclerAdapter);
        checkBox=findViewById(R.id.breakki);
        CheckBox checkBox1=findViewById(R.id.soup);
        CheckBox checkBox2=findViewById(R.id.snacks);
        CheckBox checkBox3=findViewById(R.id.mainmeal);
        CheckBox checkBox4=findViewById(R.id.salad);
        CheckBox checkBox5=findViewById(R.id.dessert);
        checkBox.setOnClickListener(view -> onCheck(checkBox,getResources().getString(R.string.breakki),catRecyclerAdapter));
        checkBox1.setOnClickListener(view -> onCheck(checkBox1,getResources().getString(R.string.soup),catRecyclerAdapter));
        checkBox2.setOnClickListener(view -> onCheck(checkBox2, getResources().getString(R.string.snack),catRecyclerAdapter));
        checkBox3.setOnClickListener(view -> onCheck(checkBox3,getResources().getString(R.string.mainMeal),catRecyclerAdapter));
        checkBox4.setOnClickListener(view -> onCheck(checkBox4,getResources().getString(R.string.salad),catRecyclerAdapter));
        checkBox5.setOnClickListener(view -> onCheck(checkBox5,getResources().getString(R.string.dessert),catRecyclerAdapter));


// Ernaehrungsselektion und Checkboxen
        GridLayoutManager layoutManagerDietary=new GridLayoutManager(this,3);
        layoutManagerDietary.setOrientation(LinearLayoutManager.VERTICAL);
        chosendietaryRec=findViewById(R.id.chosendietaryRec);
        chosendietaryRec.setLayoutManager(layoutManagerDietary);
        dietaryRecyclerAdapter=new RecyclerAdapterDietary(getApplicationContext(),selectedDietaryRecList);
        chosendietaryRec.setAdapter(dietaryRecyclerAdapter);
        checkBox=findViewById(R.id.breakki);
        CheckBox cbGluten=findViewById(R.id.gluten);
        CheckBox cbLactose=findViewById(R.id.lactose);
        CheckBox cbVegan=findViewById(R.id.vegan);
        CheckBox cbVege=findViewById(R.id.vege);
        CheckBox cbPaleo=findViewById(R.id.paleo);
        CheckBox cbFettarm=findViewById(R.id.lowfat);
        cbGluten.setOnClickListener(view -> onCheckDietary(cbGluten,getResources().getString(R.string.glutenfree),dietaryRecyclerAdapter));
        cbLactose.setOnClickListener(view -> onCheckDietary(cbLactose,getResources().getString(R.string.lactosefree),dietaryRecyclerAdapter));
        cbVegan.setOnClickListener(view -> onCheckDietary(cbVegan,getResources().getString(R.string.vegan),dietaryRecyclerAdapter));
        cbVege.setOnClickListener(view -> onCheckDietary(cbVege,getResources().getString(R.string.vegetar),dietaryRecyclerAdapter));
        cbFettarm.setOnClickListener(view -> onCheckDietary(cbFettarm,getResources().getString(R.string.lowfat),dietaryRecyclerAdapter));
        cbPaleo.setOnClickListener(view -> onCheckDietary(cbPaleo,getResources().getString(R.string.paleo),dietaryRecyclerAdapter));

//Leftover list

        restelistView= findViewById(R.id.leftoversList);
        StaggeredGridLayoutManager gridM=new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.HORIZONTAL);
        restelistView.setLayoutManager(gridM);
        loRecyclerAdapter=new RecyclerAdapterLo(getApplicationContext(),resteList);
        restelistView.setAdapter(loRecyclerAdapter);
        insertIngredient=findViewById(R.id.insertIngredient);
        addIngredientFilter=findViewById(R.id.addIngredientFilter);
        rowsize=3;
        addIngredientFilter.setOnClickListener(view -> {
            String lo=insertIngredient.getText().toString();
            resteList.add(lo.toLowerCase());
            int listsize=resteList.size();
            int rows=gridM.getSpanCount();
            if(listsize>rowsize&&listsize%3==1){
                rows=rows+1;
                gridM.setSpanCount(rows);
                rowsize=rowsize+3;
            }

            loRecyclerAdapter.notifyDataSetChanged();
            insertIngredient.setText("");
        });
//Fertigbutton
        applyFilter=findViewById(R.id.filter);
        applyFilter.setOnClickListener(view -> {
            if(previousIntent.hasExtra("action")){
                Intent newIntent=new Intent(FilterActivity.this,SearchActivity.class);
                newIntent.putExtra("action",resteList);
                startActivity(newIntent);
                finish();
            }else{
                getRandomRecipe();}
        });

//Cancel Button
        Button cancel=findViewById(R.id.cancel);
        cancel.setOnClickListener(view -> {
            Intent toMainIntent=new Intent(FilterActivity.this,MainActivity.class);
            startActivity(toMainIntent);
        });
    }
    private void onCheck(CheckBox checkbox, String selectedFilter, RecyclerAdapterCat recyclerAdapter){
        if(checkbox.isChecked()){
            selectedCategoryList.add(selectedFilter);
            recyclerAdapter.notifyItemInserted(selectedCategoryList.indexOf(selectedFilter));
        }
        else{
            selectedCategoryList.remove(selectedFilter);
            recyclerAdapter.notifyDataSetChanged();
        }
    }
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
    private void getRandomRecipe() {
        dBRecipeList= new ArrayList<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference("/Cookdome/Recipes");
        databaseReference.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    for(DataSnapshot dsS:snapshot.getChildren()){
                        String dBKey = dsS.child("key").getValue(String.class);
                        String dBrecipeName = dsS.child("recipeName").getValue(String.class);
                        String dBcat = String.valueOf(dsS.child("category").getValue());
                        int dBprepTime = Integer.parseInt(String.valueOf(dsS.child("prepTime").getValue()));
                        int dBportions = Integer.parseInt(String.valueOf(dsS.child("portions").getValue()));
                        String dBImage = dsS.child("image").getValue(String.class);
                        ArrayList<String> dBstepList = new ArrayList<>();
                        String index="0";
                        for(DataSnapshot ignored :dsS.child("stepList").getChildren()){
                            String stepTry=String.valueOf(dsS.child("stepList").child(index).getValue());
                            dBstepList.add(stepTry);
                            int i=Integer.parseInt(index);
                            i++;
                            index= Integer.toString(i);
                        }
                        String index2="0";
                        ArrayList<String> dBdietList = new ArrayList<>();
                        for(DataSnapshot ignored :dsS.child("dietRec").getChildren()){
                            String dietTry=String.valueOf(dsS.child("dietRec").child(index2).getValue());
                            int i=Integer.parseInt(index2);
                            i++;
                            index2= Integer.toString(i);
                            dBdietList.add(dietTry);
                        }


                        ArrayList<Ingredient>dBIngredientList=new ArrayList<>();
                        try {
                        for(DataSnapshot IngSS:dsS.child("ingredientList").getChildren()) {
                                Double amount = IngSS.child("amount").getValue(Double.class);
                                String unit = IngSS.child("unit").getValue(String.class);
                                String ingredientName = IngSS.child("ingredientName").getValue(String.class);
                                Ingredient ingredient = new Ingredient(amount, unit, ingredientName);
                                dBIngredientList.add(ingredient);
                            }

                            Recipe selectedRecipe = new Recipe(dBKey, dBImage, dBrecipeName, dBcat, dBprepTime, dBportions, dBIngredientList, dBstepList, dBdietList);
                            dBRecipeList.add(selectedRecipe);
                        }catch(Exception e){
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                    Toast.makeText(FilterActivity.this,R.string.retreived,Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(FilterActivity.this,R.string.dBEmpty,Toast.LENGTH_SHORT).show();
                }
                filterSearchList(time,selectedCategoryList,selectedDietaryRecList,resteList);
            }else{
                Toast.makeText(FilterActivity.this,R.string.dataRetrievalFailed,Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void filterSearchList(Integer time,ArrayList<String> categories,ArrayList<String> dietary,ArrayList<String> ingredients) {
        ArrayList<Recipe> filteredRecipes = new ArrayList<>();
        if(dietary.contains(getResources().getString(R.string.vegan))&&dietary.contains(getResources().getString(R.string.vegetar))){
            dietary.remove(getResources().getString(R.string.vegetar));
        }
        for(Recipe recipe:dBRecipeList){
            if (time != null){
                if(recipe.getPrepTime()<=time){
                    Log.d("timeFilter", "applied");
                }else{
                    continue;}}
            if(!categories.isEmpty()){
                if (categories.contains(recipe.getCategory())) {
                    Log.d("CatFilter", "applied");
                } else {
                    continue;}}
            if(!dietary.isEmpty()){
                if (recipe.getDietaryRec().containsAll(dietary)) {
                    Log.d("TAG", recipe.getRecipeName()+" meets all selected dietary requirements");
                } else {
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
            Random random_method = new Random();
            int index = random_method.nextInt(filteredRecipes.size());
            Recipe selectedRecipe= filteredRecipes.get(index);
            Intent intent = new Intent(FilterActivity.this, RecipeViewActivity.class);
            intent.putExtra("key",selectedRecipe.getKey());
            startActivity(intent);
            finish();

        }

    }
    //If user presses return on their phone he is lead back to the main activity
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent toMainIntent=new Intent(FilterActivity.this,MainActivity.class);
        startActivity(toMainIntent);
    }
}