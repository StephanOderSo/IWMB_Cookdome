package View;

import static android.view.View.GONE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

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

import com.bienhuels.iwmb_cookdome.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Random;

import Model.Ingredient;
import Model.Recipe;
import Viewmodel.RecyclerAdapterCat;
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
    ConstraintLayout mainLayout;
    RecyclerView chosenCat;
    RecyclerView chosendietaryRec;
    RecyclerAdapterCat catRecyclerAdapter;
    RecyclerAdapterLo loRecyclerAdapter;
    RecyclerAdapterDietary dietaryRecyclerAdapter;
    public ArrayList<String> selectedCategoryList= new ArrayList<String>();
    public ArrayList<String> selectedDietaryRecList=new ArrayList<String>();
    public static ArrayList<String> resteList = new ArrayList<String>();
    ArrayList<Recipe> dBRecipeList,filteredRecipes;



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
        seekBar=(SeekBar) findViewById(R.id.timeselect);
        valueView=(TextView) findViewById(R.id.valueView);
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
        catSelect=(LinearLayout) findViewById(R.id.catSelect);
        extendBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                extendBtn1.setImageResource(R.drawable.arrow_up);
                if (clickCount2%2 !=0){
                    catSelect.setVisibility(View.VISIBLE);
                } else {extendBtn1.setImageResource(R.drawable.arrow_down);
                    catSelect.setVisibility(GONE);

                }
                clickCount2++;
            }
        });
//ErnaehrungsCheckliste entfalten
        diatarySelect=(LinearLayout) findViewById(R.id.diatarySelect);
        extendBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clickCount%2 !=0){
                    extendBtn2.setImageResource(R.drawable.arrow_up);
                    diatarySelect.setVisibility(View.VISIBLE);

                } else {extendBtn2.setImageResource(R.drawable.arrow_down);
                    diatarySelect.setVisibility(GONE);

                }
                clickCount++;
            }
        });

//Kategorie Checkboxen
        GridLayoutManager layoutManager=new GridLayoutManager(this,3);
        layoutManager.setOrientation(layoutManager.VERTICAL);
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
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCheck(checkBox,getResources().getString(R.string.breakki),catRecyclerAdapter);
            }
        });
        checkBox1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCheck(checkBox1,getResources().getString(R.string.soup),catRecyclerAdapter);
            }
        });
        checkBox2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCheck(checkBox2, getResources().getString(R.string.snack),catRecyclerAdapter);
            }
        });
        checkBox3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCheck(checkBox3,getResources().getString(R.string.mainMeal),catRecyclerAdapter);
            }
        });
        checkBox4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCheck(checkBox4,getResources().getString(R.string.salad),catRecyclerAdapter);
            }
        });
        checkBox5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCheck(checkBox5,getResources().getString(R.string.dessert),catRecyclerAdapter);
            }
        });


// Ernaehrungsselektion und Checkboxen
        GridLayoutManager layoutManagerDietary=new GridLayoutManager(this,3);
        layoutManagerDietary.setOrientation(layoutManagerDietary.VERTICAL);
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
        cbGluten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCheckDietary(cbGluten,getResources().getString(R.string.glutenfree),dietaryRecyclerAdapter);
            }
        });
        cbLactose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCheckDietary(cbLactose,getResources().getString(R.string.lactosefree),dietaryRecyclerAdapter);

            }
        });
        cbVegan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCheckDietary(cbVegan,getResources().getString(R.string.vegan),dietaryRecyclerAdapter);

            }
        });
        cbVege.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCheckDietary(cbVege,getResources().getString(R.string.vegetar),dietaryRecyclerAdapter);

            }
        });
        cbFettarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCheckDietary(cbFettarm,getResources().getString(R.string.lowfat),dietaryRecyclerAdapter);

            }
        });
        cbPaleo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCheckDietary(cbPaleo,getResources().getString(R.string.paleo),dietaryRecyclerAdapter);

            }
        });

//Leftover list

        restelistView= findViewById(R.id.leftoversList);
        StaggeredGridLayoutManager gridM=new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.HORIZONTAL);
        restelistView.setLayoutManager(gridM);
        loRecyclerAdapter=new RecyclerAdapterLo(getApplicationContext(),resteList);
        restelistView.setAdapter(loRecyclerAdapter);
        insertIngredient=findViewById(R.id.insertIngredient);
        addIngredientFilter=findViewById(R.id.addIngredientFilter);
        rowsize=3;
        addIngredientFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String lo=insertIngredient.getText().toString();
                resteList.add(lo.toLowerCase());
                Log.d("TAG",lo);
                Log.d("TAG",resteList.toString());
                Integer listsize=resteList.size();
                Integer rows=gridM.getSpanCount();
                Log.d("TAG",listsize.toString()+""+rows.toString());
                if(listsize>rowsize&&listsize%3==1){
                    rows=rows+1;
                    gridM.setSpanCount(rows);
                    rowsize=rowsize+3;
                }

                loRecyclerAdapter.notifyDataSetChanged();
                insertIngredient.setText("");
                //insertIngredient.setText("");
            }
        });
//Fertigbutton
        applyFilter=findViewById(R.id.filter);
        applyFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(FilterActivity.this,"pressed",Toast.LENGTH_SHORT).show();
                if(previousIntent.hasExtra("action")){
                    Intent newIntent=new Intent(FilterActivity.this,SearchActivity.class);
                    newIntent.putExtra("action",resteList);
                    startActivity(newIntent);
                    finish();
                }else{
                    getRandomRecipe();}
            }
        });

//Cancel Button
        Button cancel=findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toMainIntent=new Intent(FilterActivity.this,MainActivity.class);
                startActivity(toMainIntent);
            }
        });
    }
    private void onCheck(CheckBox checkbox, String selectedFilter, RecyclerAdapterCat recyclerAdapter){
        if(checkbox.isChecked()){
            selectedCategoryList.add(selectedFilter);
            recyclerAdapter.notifyItemInserted(selectedCategoryList.indexOf(selectedFilter));
            Log.d("newListadd", selectedCategoryList.toString());
        }
        else{
            selectedCategoryList.remove(selectedFilter);
            recyclerAdapter.notifyDataSetChanged();
            Log.d("newListrem", selectedCategoryList.toString());
        }
    }
    private void onCheckDietary(CheckBox checkbox,String selectedFilter,RecyclerAdapterDietary recyclerAdapter){
        if(checkbox.isChecked()){
            selectedDietaryRecList.add(selectedFilter);
            recyclerAdapter.notifyItemInserted(selectedDietaryRecList.indexOf(selectedFilter));
            Log.d("newListadd", selectedDietaryRecList.toString());
        }
        else{
            selectedDietaryRecList.remove(selectedFilter);
            recyclerAdapter.notifyDataSetChanged();
            Log.d("newListrem", selectedDietaryRecList.toString());
        }
    }
    private void getRandomRecipe() {
        dBRecipeList=new ArrayList<Recipe>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference("/Cookdome/Recipes");
        databaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        DataSnapshot snapshot = task.getResult();
                        // dBRecipeList= snapshot.getValue(listType);
                        for(DataSnapshot dsS:snapshot.getChildren()){
                            //for(DataSnapshot DsSS:snapshot.child("key").getChildren()) {
                            String dBKey = dsS.child("key").getValue(String.class);
                            Log.d("Key", dBKey);
                            String dBrecipeName = dsS.child("recipeName").getValue(String.class);
                            Log.d("RecipeName", dBrecipeName);
                            String dBcat = String.valueOf(dsS.child("category").getValue());
                            Log.d("category", dBcat);
                            Integer dBprepTime = Integer.parseInt(String.valueOf(dsS.child("prepTime").getValue()));
                            Log.d("time", dBprepTime.toString());
                            Integer dBportions = Integer.parseInt(String.valueOf(dsS.child("portions").getValue()));
                            Log.d("portions", dBportions.toString());
                            String dBImage = dsS.child("image").getValue(String.class);
                            Log.d("imageUrl", dBImage);

                            ArrayList<String> dBstepList = new ArrayList<>();
                            Log.d("Steplist", dBstepList.toString());
                            String index="0";
                            for(DataSnapshot stepSS:dsS.child("stepList").getChildren()){
                                String stepTry=String.valueOf(dsS.child("stepList").child(index).getValue());
                                Log.d("stepTry", stepTry);
                                dBstepList.add(stepTry);
                                Integer i=Integer.parseInt(index);
                                i++;
                                index=(String)i.toString();
                                Log.d("steps", dBstepList.toString());
                            }
                            String index2="0";
                            ArrayList<String> dBdietList = new ArrayList<>();
                            for(DataSnapshot stepSS:dsS.child("dietRec").getChildren()){
                                String dietTry=String.valueOf(dsS.child("dietRec").child(index2).getValue());
                                Integer i=Integer.parseInt(index2);
                                i++;
                                index2=i.toString();
                                Log.d("diaet", dietTry);
                                dBdietList.add(dietTry);
                            }


                            ArrayList<Ingredient>dBIngredientList=new ArrayList<>();
                            for(DataSnapshot IngSS:dsS.child("ingredientList").getChildren()){
                                Double amount=IngSS.child("amount").getValue(Double.class);
                                String unit=IngSS.child("unit").getValue(String.class);
                                String ingredientName=IngSS.child("ingredientName").getValue(String.class);
                                Ingredient ingredient=new Ingredient(amount,unit,ingredientName);
                                dBIngredientList.add(ingredient);
                                Log.d("ingredient", ingredient.toString());}

                            Recipe selectedRecipe = new Recipe(dBKey, dBImage, dBrecipeName, dBcat, dBprepTime, dBportions, dBIngredientList, dBstepList,dBdietList);
                            dBRecipeList.add(selectedRecipe);
                            // }


                        }
                        Toast.makeText(FilterActivity.this,"list Retreived",Toast.LENGTH_SHORT).show();

                    }else{
                        Toast.makeText(FilterActivity.this,"database empty",Toast.LENGTH_SHORT).show();
                    }
                    filterSearchList(time,selectedCategoryList,selectedDietaryRecList,resteList,filteredRecipes);
                }else{
                    Toast.makeText(FilterActivity.this,"data retrieval failed",Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void filterSearchList(Integer time,ArrayList<String> categories,ArrayList<String> dietary,ArrayList<String> ingredients,ArrayList<Recipe> filteredRecipes) {
        filteredRecipes=new ArrayList<>();
        Log.d("TAG", "hello");
        if(dietary.contains(getResources().getString(R.string.vegan))&&dietary.contains(getResources().getString(R.string.vegetar))){
            dietary.remove(getResources().getString(R.string.vegetar));
        }
        Log.d("ingredientList", ingredients.toString());
        for(Recipe recipe:dBRecipeList){
            Log.d("Recipe", recipe.getRecipeName().toString());
            if (time != null){
                Log.d("filter", "time selected");
                if(recipe.getPrepTime()<=time){
                    Log.d("timeFilter", "applied");
                }else{
                    Log.d("filter", "time didnt match");
                    continue;}}
            if(!categories.isEmpty()){
                Log.d("filter", "cat selected");
                if (categories.contains(recipe.getCategory())) {
                    Log.d("CatFilter", "applied");
                } else {
                    Log.d("filter", "cat didnt match");
                    continue;}}
            if(!dietary.isEmpty()){
                Log.d("filter", "diet selected");
                ArrayList<String> temp=recipe.getDietaryRec();
                Log.d("RecipesDietary", temp.toString());
                Log.d("filter", dietary.toString());
                if (recipe.getDietaryRec().containsAll(dietary)) {
                    Log.d("TAG", recipe.getRecipeName()+" meets all selected dietary requirements");
                } else {
                    Log.d("filter", "diet didnt match");
                    continue;}}
            if(!ingredients.isEmpty()){
                Log.d("filter", "ingredients selected");
                ArrayList<String>ingredientStringList=new ArrayList<>();
                for(Ingredient ingredient:recipe.getIngredientList()){
                    String name=ingredient.getIngredientName();
                    ingredientStringList.add(name);
                }Log.d("IngredientList", ingredientStringList.toString());
                if(ingredientStringList.containsAll(ingredients)){
                    Log.d("IngredientFilter", "applied");
                } else{
                    Log.d("filter", "ingredients didnt match");
                    continue;}}

            filteredRecipes.add(recipe);
            Log.d("LIST", filteredRecipes.toString());

        }
        if(filteredRecipes.isEmpty()){
            Toast.makeText(this, "No items matched your search", Toast.LENGTH_SHORT).show();
        }else{
            Random random_method = new Random();
            int index = random_method.nextInt(filteredRecipes.size());
            Recipe selectedRecipe=filteredRecipes.get(index);
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