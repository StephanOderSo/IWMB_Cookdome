package Viewmodel;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bienhuels.iwmb_cookdome.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import Model.Ingredient;
import Model.User;


public class ShoppinglistAdapter extends ArrayAdapter<Ingredient> {
    Handler handler=new Handler();
    public ShoppinglistAdapter(@NonNull Context context, int ressource, ArrayList<Ingredient> shoppingList) {
        super(context,ressource,shoppingList);
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Ingredient ingredient= getItem(position);

        if(convertView==null) {
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.cell_shoppinglist_item,parent,false);
        }
        if(ingredient!=null){
        TextView amountView= convertView.findViewById(R.id.amountColumnSl);
        TextView unitView= convertView.findViewById(R.id.unitColumnSl);
        TextView ingredientView= convertView.findViewById(R.id.ingredientColumnSl);
        CheckBox checkBox= convertView.findViewById(R.id.checkBoxSL);
        String amount=Double.toString(ingredient.getAmount());
        amountView.setText(amount);
        unitView.setText(ingredient.getUnit());
        ingredientView.setText(ingredient.getIngredientName());
        checkBox.setOnClickListener(view -> onCheck(checkBox,ingredient));
    }else{
            Toast.makeText(getContext(), R.string.sthWrong, Toast.LENGTH_SHORT).show();
        }

        return convertView;
    }
    private void onCheck(CheckBox checkbox,Ingredient ingredient){
        Runnable run= () -> {
            FirebaseAuth auth;
            auth= FirebaseAuth.getInstance();
            FirebaseUser fbuser=auth.getCurrentUser();
            User user=new User();
            Context context=getContext();

            if(checkbox.isChecked()){
                user.removeFromShoppingList(fbuser,context,ingredient,handler);
            }
            else{
                user.addToShoppingList(context,fbuser,handler,Thread.currentThread(),ingredient);
            }
        };
        Thread thread=new Thread(run);
        thread.start();

    }
}
