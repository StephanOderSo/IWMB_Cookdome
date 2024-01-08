package Viewmodel.RecipeViewAdapters;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bienhuels.iwmb_cookdome.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import Model.Firebase;
import Model.Ingredient;
import Model.User;


public class IngrListAdapterwSLBtn extends ArrayAdapter<Ingredient> {
    FirebaseAuth auth=FirebaseAuth.getInstance();
    Context context;
    Firebase firebase=new Firebase();
    public IngrListAdapterwSLBtn(@NonNull Context context, int resource, @NonNull ArrayList<Ingredient> ingredientList) {
        super(context, resource,ingredientList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Ingredient ingredient = getItem(position);
        context=getContext();

        if(convertView==null) {
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.cell_ingredient_list_item,parent,false);
        }
        if(ingredient!=null){
        TextView amountView= convertView.findViewById(R.id.amountColumn);
        TextView unitView= convertView.findViewById(R.id.unitColumn);
        TextView ingredientView= convertView.findViewById(R.id.ingredientColumn);
        String amount=Double.toString(ingredient.getAmount());
        amountView.setText(amount);
        unitView.setText(ingredient.getUnit());
        ingredientView.setText(ingredient.getName());
        ImageButton imageButton= convertView.findViewById(R.id.removeStepBtn);
        imageButton.setImageResource(R.drawable.add_to_cart);
        imageButton.setOnClickListener(view -> addToSList(ingredient,imageButton));
        }else{
            Toast.makeText(getContext(), R.string.sthWrong, Toast.LENGTH_SHORT).show();
        }

        return convertView;
    }

    public void addToSList(Ingredient ingredient,ImageButton imageButton){
        FirebaseUser fbUser=auth.getCurrentUser();
        User user=new User();
        Handler handler=new Handler();
        Runnable viewRunnable= () -> {
            handler.post(() -> {
                    imageButton.setImageResource(R.drawable.tick);
                    imageButton.setBackground(null);
                    imageButton.setBackgroundColor(0);
                    Toast.makeText(context, R.string.added, Toast.LENGTH_SHORT).show();
            });
        };
        Thread viewThread=new Thread(viewRunnable);

        Runnable slRunnable= () -> firebase.addToShoppingList(context,fbUser,handler,viewThread,ingredient);
        Thread slThread=new Thread(slRunnable);
        slThread.start();
    }
}
