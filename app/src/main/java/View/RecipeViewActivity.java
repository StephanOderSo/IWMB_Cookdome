package View;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

import Model.Ingredient;
import Model.Recipe;
import Model.User;
import Viewmodel.RecipeViewAdapters.IngrListAdapterwSLBtn;
import Viewmodel.RecipeViewAdapters.StepListAdapterNoBtn;

public class RecipeViewActivity extends AppCompatActivity {

    FirebaseAuth auth=FirebaseAuth.getInstance();
    ArrayList<String> favlist;
    ArrayList<String> ownlist;
    ImageView favView,share,edit;
    Recipe selectedRecipe=new Recipe();
    int portionsOrigin;
    CardView portionsCard;
    TextView portionsText;
    IngrListAdapterwSLBtn ingredientAdapter;
    ArrayList<Ingredient> dBingredientList;
    String key;
    User user=new User();
    FirebaseUser fbUser;
    Context context;
    Handler handler =new Handler();
    Thread checkFavThread,setDataThread;
    Intent previousIntent;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_view);
        fbUser=auth.getCurrentUser();
        previousIntent = getIntent();
        context=getApplicationContext();
        getData();

        edit=findViewById(R.id.edit);
        edit.setOnClickListener(view -> {
            Intent toEditIntent=new Intent(this, CreateRecipeActivity.class);
            toEditIntent.putExtra("Edit",key);
            startActivity(toEditIntent);
            finish();
        });
        favView=findViewById(R.id.favourite);
        favView.setOnClickListener(view -> favlist=user.updateFavourites(selectedRecipe,context,favView,fbUser, handler,favlist));
        portionsCard=findViewById(R.id.portionsBtn);
        portionsText=findViewById(R.id.portions);
        portionsCard.setOnClickListener(view -> buildPortionsDialog());
        share=findViewById(R.id.share);
        share.setOnClickListener(view -> {
            Intent toUsersIntent=new Intent(this,UsersActivity.class);
            toUsersIntent.putExtra("key",key);
            startActivity(toUsersIntent);
            finish();
        });
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
           if(favlist.contains(key)){
               handler.post(() -> favView.setImageResource(R.drawable.liked));
           }else{
               handler.post(() -> favView.setImageResource(R.drawable.unliked));
           }
       };
       checkFavThread=new Thread(checkFavRunnable);

       Runnable checkOwnRun= () -> {
           if(ownlist.contains(key)){handler.post(() -> edit.setVisibility(View.VISIBLE));}};
       Thread checkOwnThread=new Thread(checkOwnRun);

       Runnable getOwnFavRun= () -> {
           favlist=user.getFavourites(context,fbUser, handler,checkFavThread);
           ownlist=user.getOwn(context,fbUser, handler,checkOwnThread);
       };
       Thread getOwnFavThread=new Thread(getOwnFavRun);
       getOwnFavThread.start();
       Runnable setDataRun= () -> {
           selectedRecipe=selectedRecipe.getRecipe();
           portionsOrigin=selectedRecipe.getPortions();
           handler.post(() -> setValues(selectedRecipe));
       };
       setDataThread=new Thread(setDataRun);

       Runnable runnable= () -> {
           key = previousIntent.getStringExtra("key");
           selectedRecipe.downloadSelectedRecipe(key,context,handler,setDataThread,fbUser);
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
        getListViewSize(ingredientAdapter,ingredientList);
        ArrayList<String>dBStepList;
        dBStepList=selectedRecipe.getStepList();
        StepListAdapterNoBtn stepAdapter = new StepListAdapterNoBtn(getApplicationContext(), 0, dBStepList);
        stepList.setAdapter(stepAdapter);
        getListViewSize(stepAdapter,stepList);
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