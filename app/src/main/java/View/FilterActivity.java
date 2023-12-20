package View;

import static android.view.View.GONE;

import android.content.Context;
import android.content.Intent;
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
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bienhuels.iwmb_cookdome.R;

import java.util.ArrayList;
import java.util.Random;

import Model.Firebase;
import Model.Ingredient;
import Model.Recipe;
import Viewmodel.SearchAdapters.RecyclerAdapterCat;
import Viewmodel.SearchAdapters.RecyclerAdapterDietary;
import Viewmodel.SearchAdapters.RecyclerAdapterLo;
import Viewmodel.Tools;

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
    Button applyFilter,cancel;
    LinearLayout diatarySelect;
    LinearLayout catSelect;
    RecyclerView chosenCat;
    RecyclerView chosendietaryRec;
    RecyclerAdapterCat catRecyclerAdapter;
    RecyclerAdapterLo loRecyclerAdapter;
    RecyclerAdapterDietary dietaryRecyclerAdapter;
    public ArrayList<String> selectedCategoryList= new ArrayList<>();
    public ArrayList<String> selectedDietaryRecList= new ArrayList<>();
    public static ArrayList<String> leftoversList = new ArrayList<>();
    ArrayList<Recipe> dBRecipeList;
    CheckBox checkBox1,checkBox2,checkBox3,checkBox4,checkBox5,cbGluten,cbLactose,cbVegan,cbVege,cbPaleo,cbFettarm;
    StaggeredGridLayoutManager gridM;
    Intent previousIntent;
    Firebase database=new Firebase();
    Handler handler=new Handler();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        previousIntent=getIntent();
        if(previousIntent.hasExtra("leftovers")){
            ConstraintLayout subContainer=findViewById(R.id.subContainer);
            subContainer.setVisibility(GONE);
            CardView headerCard=findViewById(R.id.headercard);
            headerCard.setVisibility(View.VISIBLE);
        }
//Timepicker
        seekBar= findViewById(R.id.timeselect);
        valueView= findViewById(R.id.valueView);

//Category Checkboxes and List
        extendBtn1=findViewById(R.id.extendBtn1);
        extendBtn2=findViewById(R.id.extendBtn2);
        catSelect= findViewById(R.id.catSelect);
        GridLayoutManager layoutManager=new GridLayoutManager(this,3);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        chosenCat=findViewById(R.id.chosenCat);
        chosenCat.setLayoutManager(layoutManager);
        catRecyclerAdapter=new RecyclerAdapterCat(getApplicationContext(),selectedCategoryList);
        chosenCat.setAdapter(catRecyclerAdapter);
        checkBox=findViewById(R.id.breakki);
        checkBox1=findViewById(R.id.soup);
        checkBox2=findViewById(R.id.snacks);
        checkBox3=findViewById(R.id.mainmeal);
        checkBox4=findViewById(R.id.salad);
        checkBox5=findViewById(R.id.dessert);

// diet checkboxes and list
        GridLayoutManager layoutManagerDietary=new GridLayoutManager(this,3);
        layoutManagerDietary.setOrientation(LinearLayoutManager.VERTICAL);
        chosendietaryRec=findViewById(R.id.chosendietaryRec);
        chosendietaryRec.setLayoutManager(layoutManagerDietary);
        dietaryRecyclerAdapter=new RecyclerAdapterDietary(getApplicationContext(),selectedDietaryRecList);
        chosendietaryRec.setAdapter(dietaryRecyclerAdapter);
        checkBox=findViewById(R.id.breakki);
        cbGluten=findViewById(R.id.gluten);
        cbLactose=findViewById(R.id.lactose);
        cbVegan=findViewById(R.id.vegan);
        cbVege=findViewById(R.id.vege);
        cbPaleo=findViewById(R.id.paleo);
        cbFettarm=findViewById(R.id.lowfat);

//Leftover list
        restelistView= findViewById(R.id.leftoversList);
        gridM=new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.HORIZONTAL);
        restelistView.setLayoutManager(gridM);
        loRecyclerAdapter=new RecyclerAdapterLo(getApplicationContext(), leftoversList);
        restelistView.setAdapter(loRecyclerAdapter);
        insertIngredient=findViewById(R.id.insertIngredient);
        addIngredientFilter=findViewById(R.id.addIngredientFilter);

        applyFilter=findViewById(R.id.filter);
        cancel=findViewById(R.id.cancel);
        //Unfolod Category checkboxes
        extendBtn1.setOnClickListener(view -> {
            extendBtn1.setImageResource(R.drawable.arrow_up);
            if (clickCount2%2 !=0){
                catSelect.setVisibility(View.VISIBLE);
            } else {extendBtn1.setImageResource(R.drawable.arrow_down);
                catSelect.setVisibility(GONE);

            }
            clickCount2++;
        });

        diatarySelect= findViewById(R.id.diatarySelect);

        //Unfold Diet Checkboxes
        extendBtn2.setOnClickListener(view -> {
            if (clickCount%2 !=0){
                extendBtn2.setImageResource(R.drawable.arrow_up);
                diatarySelect.setVisibility(View.VISIBLE);

            } else {extendBtn2.setImageResource(R.drawable.arrow_down);
                diatarySelect.setVisibility(GONE);

            }
            clickCount++;
        });
        //Category checkboxes
        Tools tools=new Tools();
        checkBox.setOnClickListener(view -> tools.onCheck(checkBox,getResources().getString(R.string.breakki),catRecyclerAdapter,selectedCategoryList));
        checkBox1.setOnClickListener(view -> tools.onCheck(checkBox1,getResources().getString(R.string.soup),catRecyclerAdapter,selectedCategoryList));
        checkBox2.setOnClickListener(view -> tools.onCheck(checkBox2, getResources().getString(R.string.snack),catRecyclerAdapter,selectedCategoryList));
        checkBox3.setOnClickListener(view -> tools.onCheck(checkBox3,getResources().getString(R.string.mainMeal),catRecyclerAdapter,selectedCategoryList));
        checkBox4.setOnClickListener(view -> tools.onCheck(checkBox4,getResources().getString(R.string.salad),catRecyclerAdapter,selectedCategoryList));
        checkBox5.setOnClickListener(view -> tools.onCheck(checkBox5,getResources().getString(R.string.dessert),catRecyclerAdapter,selectedCategoryList));
        //diet checkboxes

        cbGluten.setOnClickListener(view -> tools.onCheck(cbGluten,getResources().getString(R.string.glutenfree),dietaryRecyclerAdapter,selectedDietaryRecList));
        cbLactose.setOnClickListener(view -> tools.onCheck(cbLactose,getResources().getString(R.string.lactosefree),dietaryRecyclerAdapter,selectedDietaryRecList));
        cbVegan.setOnClickListener(view -> tools.onCheck(cbVegan,getResources().getString(R.string.vegan),dietaryRecyclerAdapter,selectedDietaryRecList));
        cbVege.setOnClickListener(view -> tools.onCheck(cbVege,getResources().getString(R.string.vegetar),dietaryRecyclerAdapter,selectedDietaryRecList));
        cbFettarm.setOnClickListener(view -> tools.onCheck(cbFettarm,getResources().getString(R.string.lowfat),dietaryRecyclerAdapter,selectedDietaryRecList));
        cbPaleo.setOnClickListener(view -> tools.onCheck(cbPaleo,getResources().getString(R.string.paleo),dietaryRecyclerAdapter,selectedDietaryRecList));
        //Leftover list
        rowsize=3;
        addIngredientFilter.setOnClickListener(view -> {
            if(insertIngredient.getText()!=null){
                if(!insertIngredient.getText().toString().equals("")){
                    String lo=insertIngredient.getText().toString();
                    leftoversList.add(lo.toLowerCase());
                    int listsize= leftoversList.size();
                    int rows=gridM.getSpanCount();
                    if(listsize>rowsize&&listsize%3==1){
                        rows=rows+1;
                        gridM.setSpanCount(rows);
                        rowsize=rowsize+3;
                    }
                    loRecyclerAdapter.notifyDataSetChanged();
                    insertIngredient.setText("");
                }else{
                    Toast.makeText(this, R.string.enterLeftover, Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(this, R.string.enterLeftover, Toast.LENGTH_SHORT).show();
            }
        });
//Apply filter button
        applyFilter.setOnClickListener(view -> {
            if(previousIntent.hasExtra("leftovers")){
                Intent newIntent=new Intent(FilterActivity.this,SearchActivity.class);
                newIntent.putExtra("leftovers", leftoversList);
                startActivity(newIntent);
                finish();
            }else{
                Runnable getRandomRun= () -> {
                    dBRecipeList=database.returnRecipes();
                    filterAndGetRandom(time,selectedCategoryList,selectedDietaryRecList,leftoversList);
                };
                Thread getRandomThread=new Thread(getRandomRun);
                Context context=getApplicationContext();
                Handler handler=new Handler();
                Runnable download= () -> database.getAllRecipes(context,handler,getRandomThread);
                Thread downloadThread=new Thread(download);
                downloadThread.start();
            }
        });
        super.onResume();
        //Cancel Button
        cancel.setOnClickListener(view -> {
            Intent toMainIntent=new Intent(FilterActivity.this,MainActivity.class);
            startActivity(toMainIntent);
        });

    }

    @Override
    protected void onStart() {
        // pass seekbar values to textview
        super.onStart();
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
    }

    @Override
    protected void onResume() {

       super.onResume();
    }

    private void filterAndGetRandom(Integer time, ArrayList<String> categories, ArrayList<String> dietary, ArrayList<String> ingredients) {
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
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), R.string.noMatch, Toast.LENGTH_SHORT).show();
                }
            });
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