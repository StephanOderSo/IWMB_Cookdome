package View;
import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bienhuels.iwmb_cookdome.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import Model.Ingredient;
import Model.Recipe;
import Model.User;
import Viewmodel.RecipeViewAdapters.IngrListAdapterwSLBtn;
import Viewmodel.RecipeViewAdapters.StepListAdapterNoBtn;

public class RecipeViewActivity extends AppCompatActivity {
    DatabaseReference databaseReference,databaseReferenceFav;
    FirebaseDatabase database;
    FirebaseAuth auth=FirebaseAuth.getInstance();
    ArrayList<String> favlist;
    ArrayList<String> ownlist;
    ImageView favView;
    Boolean portionsChanged;

    Recipe selectedRecipe;
    int portionsOrigin;
    CardView portionsCard;
    TextView portionsText;
    IngrListAdapterwSLBtn ingredientAdapter;
    ArrayList<Ingredient> dBingredientList;
    String key;
    ImageView edit;
    User user=new User();
    FirebaseUser fbUser;
    Context context;
    Handler userHandler=new Handler();
    String source="RecipeView";
    Thread checkFavThread;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_view);
        database=FirebaseDatabase.getInstance();
        databaseReferenceFav = database.getReference("/Cookdome/Users");
        fbUser=auth.getCurrentUser();

        context=getApplicationContext();
        Runnable checkFavRunnable=new Runnable() {
            @Override
            public void run() {
                synchronized (Thread.currentThread()){
                    while(favlist==null){
                        try {
                            Log.d(TAG, "waiting");
                            Thread.currentThread().wait();

                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }}
                checkFav();
            }
        };
        checkFavThread=new Thread(checkFavRunnable);
        checkFavThread.start();


        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                getSelectedRecipe();
            }
        };
        Thread getRecipeThread=new Thread(runnable);
        getRecipeThread.start();
        edit=findViewById(R.id.edit);
        edit.setOnClickListener(view -> {
            Intent toEditIntent=new Intent(this, CreateRecipeActivity.class);
            toEditIntent.putExtra("Edit",key);
            startActivity(toEditIntent);
            finish();
        });
        favView=findViewById(R.id.favourite);
        favView.setOnClickListener(view -> favlist=user.updateFavourites(selectedRecipe,context,favView,fbUser,userHandler));
        portionsChanged=false;
        portionsCard=findViewById(R.id.portionsBtn);
        portionsText=findViewById(R.id.portions);
        portionsCard.setOnClickListener(view -> {
                    buildPortionsDialog();
                });
    }

    public void buildPortionsDialog(){
        AlertDialog.Builder portionsDialog=new AlertDialog.Builder(RecipeViewActivity.this);
        portionsDialog.setTitle(R.string.setPortions);
        final EditText editPortions=new EditText(RecipeViewActivity.this);
        editPortions.setInputType(InputType.TYPE_CLASS_NUMBER);
        editPortions.setBackground(getDrawable(R.drawable.lavender_border));
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
    private void getSelectedRecipe() {
        Intent previousIntent = getIntent();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("/Cookdome/Recipes");
        key = previousIntent.getStringExtra("key");
        databaseReference.child(key).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                favlist=user.getFavourites(context,fbUser,userHandler,checkFavThread);
                ownlist=user.getOwn(context,fbUser,userHandler,Thread.currentThread());
                while(favlist==null){}
                if(ownlist.contains(key)){
                    userHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            edit.setVisibility(View.VISIBLE);
                        }
                    });
                }
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    selectedRecipe=new Recipe().rebuildFromFirebase(snapshot);
                    portionsOrigin=selectedRecipe.getPortions();
                    setValues(selectedRecipe);
                } else {
                    String id=user.getUID(fbUser,context);
                    databaseReferenceFav.child(id).child("Privates").child(key).get().addOnCompleteListener(task1 -> {
                        if (task1.getResult().exists()) {
                            DataSnapshot snapshot = task1.getResult();
                            selectedRecipe=new Recipe().rebuildFromFirebase(snapshot);
                            portionsOrigin=selectedRecipe.getPortions();
                            setValues(selectedRecipe);
                        }
                    }).addOnFailureListener(e -> Toast.makeText(RecipeViewActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
    public void checkFav(){
        if(favlist.contains(key)){
            userHandler.post(new Runnable() {
                @Override
                public void run() {
                    favView.setImageResource(R.drawable.liked);
                }
            });
        }else{
            userHandler.post(new Runnable() {
                @Override
                public void run() {
                    favView.setImageResource(R.drawable.unliked);
                }
            });
        }
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
                .placeholder(R.drawable.camera)
                .resize(400,400)
                .centerCrop()
                .into(imageView);
        category.setText(selectedRecipe.getCategory());
        int tempPrepTime=selectedRecipe.getPrepTime();
        time.setText(String.format(Integer.toString(tempPrepTime)));
        Integer tempPortions=selectedRecipe.getPortions();
        portionsText.setText(String.format(tempPortions.toString()));
        StringBuilder dietaryTxt=new StringBuilder();
        for(String diet: selectedRecipe.getDietaryRec()){
            String dietShort = "";
            if (diet.equals(getResources().getString(R.string.vegetar))) {
                dietShort = "VT";
            }
            if (diet.equals(getResources().getString(R.string.vegan))) {
                dietShort = "V";
            }
            if (diet.equals(getResources().getString(R.string.glutenfree))) {
                dietShort = "GF";
            }
            if (diet.equals(getResources().getString(R.string.lactosefree))) {
                dietShort = "LF";
            }
            if (diet.equals(getResources().getString(R.string.paleo))) {
                dietShort = "P";
            }
            if (diet.equals(getResources().getString(R.string.lowfat))) {
                dietShort = "LF";
            }
            dietaryTxt.append(dietShort);
            int i;
            i=selectedRecipe.getDietaryRec().indexOf(diet);
            if(i!=selectedRecipe.getDietaryRec().size()-1){
                dietaryTxt.append(" | ");
            }
        }
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
        int totalHeight=0;
        for (int i = 0; i < ingredientAdapter.getCount(); i++) {
            View mView = ingredientAdapter.getView(i, null, ingredientList);

            mView.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),

                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            totalHeight += mView.getMeasuredHeight();}
        int ingrcount=ingredientAdapter.getCount();
        ViewGroup.LayoutParams ingrparams=ingredientList.getLayoutParams();
        ingrparams.height=totalHeight+ingredientList.getDividerHeight()*ingrcount;
        ingredientList.setLayoutParams(ingrparams);
        ingredientList.setLayoutParams(ingrparams);
        ArrayList<String>dBStepList;
        dBStepList=selectedRecipe.getStepList();
        StepListAdapterNoBtn stepAdapter = new StepListAdapterNoBtn(getApplicationContext(), 0, dBStepList);
        stepList.setAdapter(stepAdapter);
        int totalHeight2=0;
        for (int i = 0; i < stepAdapter.getCount(); i++) {
            View mView = stepAdapter.getView(i, null, stepList);

            mView.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),

                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            totalHeight2 += mView.getMeasuredHeight();}
        int stepcount=stepAdapter.getCount();
        ViewGroup.LayoutParams stepparams=stepList.getLayoutParams();
        stepparams.height=totalHeight2+stepList.getDividerHeight()*stepcount;
        stepList.setLayoutParams(stepparams);
    }

    @Override
    public void onBackPressed() {
        Intent previousIntent = getIntent();
        if(previousIntent.hasExtra("fromCreate")){
            Intent toMainIntent=new Intent(RecipeViewActivity.this,MainActivity.class);
            startActivity(toMainIntent);
        }else{
            super.onBackPressed();
        }

    }
}