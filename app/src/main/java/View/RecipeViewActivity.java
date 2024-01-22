package View;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;

import com.bienhuels.iwmb_cookdome.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;

import Model.Firebase;
import Model.Ingredient;
import Model.Recipe;
import Model.Step;
import Viewmodel.RecipeViewAdapters.IngrListAdapterwSLBtn;
import Viewmodel.RecipeViewAdapters.StepListAdapterNoBtn;
import Viewmodel.Tools;

public class RecipeViewActivity extends AppCompatActivity {

    FirebaseAuth auth=FirebaseAuth.getInstance();
    ImageView favView,share,edit;
    Recipe selectedRecipe=new Recipe();
    int portionsOrigin;
    CardView portionsCard;
    TextView portionsText;
    IngrListAdapterwSLBtn ingredientAdapter;
    ArrayList<Ingredient> dBingredientList;
    String key;
    FirebaseUser fbUser;
    Context context;
    Handler handler =new Handler();
    Thread checkFavThread,setDataThread;
    Intent previousIntent;
    Tools tools=new Tools();
    Firebase firebase=new Firebase();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_view);
        fbUser=auth.getCurrentUser();
        previousIntent = getIntent();
        context=getApplicationContext();
        edit=findViewById(R.id.edit);
        favView=findViewById(R.id.favourite);
        portionsCard=findViewById(R.id.portionsBtn);
        portionsText=findViewById(R.id.portions);
        share=findViewById(R.id.share);

        edit.setOnClickListener(view -> {
            Intent toEditIntent=new Intent(this, CreateRecipeActivity.class);
            toEditIntent.putExtra("Edit",key);
            startActivity(toEditIntent);
            finish();
        });
        favView.setOnClickListener(view -> firebase.updateFavouriteRecipes(selectedRecipe,context,favView,fbUser, handler));
        portionsCard.setOnClickListener(view -> buildPortionsDialog());
        share.setOnClickListener(view -> {
            Intent toUsersIntent=new Intent(this,UsersActivity.class);
            toUsersIntent.putExtra("key",key);
            startActivity(toUsersIntent);
            finish();
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        getData();
    }
    @Override
    protected void onResume() {
        super.onResume();

    }

    public void buildPortionsDialog(){
        AlertDialog.Builder portionsDialog=new AlertDialog.Builder(RecipeViewActivity.this);
        portionsDialog.setTitle(R.string.setPortions);
        final EditText editPortions=new EditText(RecipeViewActivity.this);
        editPortions.setInputType(InputType.TYPE_CLASS_NUMBER);
        editPortions.setBackground(AppCompatResources.getDrawable(getApplicationContext(),R.drawable.lavender_border));
        editPortions.setHint(R.string.portions);
        editPortions.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        portionsDialog.setView(editPortions);
        portionsDialog.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
            int newPortions;
            if(!editPortions.getText().toString().equals("")){
                newPortions=Integer.parseInt(editPortions.getText().toString());
                if(newPortions>0){
                    portionsText.setText(String.valueOf(newPortions));
                    for(Ingredient ingredient:dBingredientList){
                        double amount=ingredient.getAmount();
                        ingredient.setAmount(amount/portionsOrigin*newPortions);
                    }
                    ingredientAdapter.notifyDataSetChanged();
                    portionsOrigin=newPortions;
                    Toast.makeText(this, R.string.changesApplied, Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this, R.string.number2small, Toast.LENGTH_LONG).show();
                }
            }
            else{
                Toast.makeText(this, R.string.noChange, Toast.LENGTH_SHORT).show();
            }
        });
        portionsDialog.setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
            dialogInterface.cancel();
            Toast.makeText(this, R.string.noChange, Toast.LENGTH_SHORT).show();

        });
        portionsDialog.show();
    }
   private void getData(){
       Runnable checkFavRunnable= () -> {
           if(firebase.getUser().getFavouriteRecipes().contains(key)){
               handler.post(() -> favView.setImageResource(R.drawable.liked));
           }else{
               handler.post(() -> favView.setImageResource(R.drawable.unliked));
           }
       };
       checkFavThread=new Thread(checkFavRunnable);

       Runnable checkOwnRun= () -> {
           if(firebase.getUser().getOwnRecipes().contains(key)){handler.post(() -> edit.setVisibility(View.VISIBLE));}};
       Thread checkOwnThread=new Thread(checkOwnRun);

       Runnable getOwnFavRun= () -> {
           firebase.setFavouriteRecipeKeys(context,fbUser, handler,checkFavThread);
           firebase.setOwnRecipeKeys(context,fbUser, handler,checkOwnThread);
       };
       Thread getOwnFavThread=new Thread(getOwnFavRun);
       getOwnFavThread.start();
       Runnable setDataRun= () -> {
           selectedRecipe=firebase.returnRecipe();
           portionsOrigin=selectedRecipe.getPortions();
           handler.post(() -> setValues(selectedRecipe));
       };
       setDataThread=new Thread(setDataRun);

       Runnable runnable= () -> {
           key = previousIntent.getStringExtra("key");
           firebase.downloadRecipe(key,context,handler,setDataThread,fbUser);
       };
       Thread getRecipeThread=new Thread(runnable);
       getRecipeThread.start();
   }


    private void setValues (Recipe selectedRecipe) {
        TextView textView = findViewById(R.id.recipeName);
        ImageView imageView = findViewById(R.id.imageView);
        TextView category = findViewById(R.id.category);
        TextView time = findViewById(R.id.time);
        ListView ingredientList = findViewById(R.id.ingredientList);
        ListView stepList = findViewById(R.id.stepList);
        TextView dietary= findViewById(R.id.dietary);
        textView.setText(selectedRecipe.getRecipeName());
        Picasso.get()
                .load(selectedRecipe.getImage())
                .placeholder(R.drawable.image)
                .resize(400,400)
                .centerCrop()
                .into(imageView);
        category.setText(selectedRecipe.getCategory());
        int tempPrepTime=selectedRecipe.getPrepTime();
        time.setText(String.format(Integer.toString(tempPrepTime)));
        Integer tempPortions=selectedRecipe.getPortions();
        portionsText.setText(String.format(tempPortions.toString()));
        ArrayList<String> arrayList=new ArrayList<>(Arrays.asList(getResources().getString(R.string.vegetar),getResources().getString(R.string.vegan),getResources().getString(R.string.glutenfree),getResources().getString(R.string.lactosefree),getResources().getString(R.string.paleo),getResources().getString(R.string.lowfat)));
        StringBuilder dietaryTxt=tools.setDietString(selectedRecipe,arrayList);
        if(dietaryTxt.toString().equals("")){
            dietary.setVisibility(View.GONE);
            ImageView dietaryIcon=findViewById(R.id.dietaryIcon);
            dietaryIcon.setVisibility(View.GONE);
        }else{
            dietary.setText(dietaryTxt.toString());
        }
        dBingredientList= selectedRecipe.getIngredientList();
        ingredientAdapter = new IngrListAdapterwSLBtn(getApplicationContext(), 0,dBingredientList);
        ingredientList.setAdapter(ingredientAdapter);
        tools.getListViewSize(ingredientAdapter,ingredientList,handler);
        ArrayList<Step> dBStepList;
        dBStepList=selectedRecipe.getStepList();
        StepListAdapterNoBtn stepAdapter = new StepListAdapterNoBtn(getApplicationContext(), 0, dBStepList);
        stepList.setAdapter(stepAdapter);
        tools.getListViewSize(stepAdapter,stepList,handler);
    }


    @Override
    public void onBackPressed() {
        Intent previousIntent = getIntent();
        selectedRecipe=null;
        if(previousIntent.hasExtra("fromCreate")){
            Intent toMainIntent=new Intent(RecipeViewActivity.this,MainActivity.class);
            startActivity(toMainIntent);
            finish();
        }else{
            super.onBackPressed();
        }

    }
}