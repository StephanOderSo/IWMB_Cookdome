package Viewmodel.RecipeViewAdapters;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bienhuels.iwmb_cookdome.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import Model.Ingredient;
import Model.User;


public class IngrListAdapterwSLBtn extends ArrayAdapter<Ingredient> {
    FirebaseAuth auth=FirebaseAuth.getInstance();
    public IngrListAdapterwSLBtn(@NonNull Context context, int resource, @NonNull ArrayList<Ingredient> ingredientList) {
        super(context, resource,ingredientList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Ingredient ingredient = getItem(position);

        if(convertView==null) {
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.ingredient_list_item,parent,false);
        }
        TextView amountView= convertView.findViewById(R.id.amountColumn);
        TextView unitView= convertView.findViewById(R.id.unitColumn);
        TextView ingredientView= convertView.findViewById(R.id.ingredientColumn);
        String amount=Double.toString(ingredient.getAmount());
        amountView.setText(amount);
        unitView.setText(ingredient.getUnit());
        ingredientView.setText(ingredient.getIngredientName());
        ImageButton imageButton= convertView.findViewById(R.id.removeStepBtn);
        imageButton.setImageResource(R.drawable.add_to_cart);
        imageButton.setOnClickListener(view -> addToSList(ingredient,imageButton));


        return convertView;
    }

    public void addToSList(Ingredient ingredient,ImageButton imageButton){
        Context context=getContext();
        FirebaseUser fbUser=auth.getCurrentUser();
        User user=new User();
        String uID= user.getUID(fbUser,context);
        Handler handler=new Handler();
        Runnable viewRunnable=new Runnable() {
            @Override
            public void run() {
                synchronized (Thread.currentThread()){

                        try {
                            Log.d(TAG, "waiting");
                            Thread.currentThread().wait();

                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                            imageButton.setImageResource(R.drawable.tick);
                            imageButton.setBackground(null);
                            imageButton.setBackgroundColor(0);
                            Toast.makeText(getContext(), R.string.added, Toast.LENGTH_SHORT).show();
                    }
                });

            }
        };
        Thread viewThread=new Thread(viewRunnable);
        viewThread.start();
        Runnable slRunnable=new Runnable() {
            @Override
            public void run() {
                user.setShoppingList(context,uID,handler,viewThread,ingredient);
            }
        };
        Thread slThread=new Thread(slRunnable);
        slThread.start();
    }
}
