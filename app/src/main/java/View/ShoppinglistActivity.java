package View;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bienhuels.iwmb_cookdome.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import Model.Ingredient;
import Model.User;
import Viewmodel.ShoppinglistAdapter;

public class ShoppinglistActivity extends AppCompatActivity {
    ArrayList<Ingredient> shoppingList;
    User user=new User();
    String uID;
    Context context;
    Handler handler=new Handler();
    ListView shoppinglistView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shoppinglist);
        FirebaseAuth auth;
        context=getApplicationContext();
        auth= FirebaseAuth.getInstance();
        FirebaseUser fbUser=auth.getCurrentUser();
        uID=user.getUID(fbUser,context);

        setupList();
        shoppinglistView=findViewById(R.id.shoppinglistView);
        shoppinglistView.setBackground(null);
    }

    public void setupList(){
        Runnable setListRunnable= () -> {
            synchronized (Thread.currentThread()){
                while(shoppingList==null){
                    try {
                        Log.d(TAG, "waiting");
                        Thread.currentThread().wait();

                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }}
            handler.post(() -> {
                ShoppinglistAdapter adapter=new ShoppinglistAdapter(getApplicationContext(),0,shoppingList);
                shoppinglistView.setAdapter(adapter);
                if(shoppingList.isEmpty()){

                    TextView slEmpty=findViewById(R.id.shoppinngListEmpty);
                    shoppinglistView.setVisibility(View.GONE);
                    slEmpty.setVisibility(View.VISIBLE);
                }
            });
        };
        Thread setListThread=new Thread(setListRunnable);
        setListThread.start();
        Runnable getListRunnable= () -> shoppingList=user.getShoppingList(context,uID,setListThread);
        Thread getListThread=new Thread(getListRunnable);
        getListThread.start();
    }
}