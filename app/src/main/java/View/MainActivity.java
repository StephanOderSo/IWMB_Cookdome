package View;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bienhuels.iwmb_cookdome.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import Model.User;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    FloatingActionButton fab;
    DrawerLayout drawerLayout;
    FirebaseAuth auth=FirebaseAuth.getInstance();
    FirebaseUser fbuser;
    Context context;
    Handler userHandler=new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fbuser=auth.getCurrentUser();
        context=getApplicationContext();
//Create-Recipe Button (Click leads to create recipe activity)
        fab=findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, CreateRecipeActivity.class);
            startActivity(intent);
            finish();
        });
//Search Button (Click leads to Search activity with comment "search" to indicate source-activity)
        View search = findViewById(R.id.search);
        search.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            intent.putExtra("search","search");
            startActivity(intent);
            finish();
        });
//Burgermenu Navigation-Drawer
        drawerLayout=findViewById(R.id.drawerLayout);
        NavigationView navView=findViewById(R.id.navView);
        navView.bringToFront();
        navView.setNavigationItemSelectedListener(this);
        ImageButton burgermenu2=findViewById(R.id.burgermenu);
        burgermenu2.setOnClickListener(view -> drawerLayout.open());


//Random Recipe Button
        CardView randomcard=findViewById(R.id.randomcard);
        randomcard.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, FilterActivity.class);
            startActivity(intent);
            finish();
        });
//Categories Button
        CardView catcard=findViewById(R.id.catcard);
        catcard.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SelectCategoryActivity.class);
            startActivity(intent);
            finish();
        });
//Leftovers  Button
        CardView restcard=findViewById(R.id.restcard);
        restcard.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, FilterActivity.class);
            intent.putExtra("action","action");
            startActivity(intent);
            finish();
        });
//Burgermenu header
        ImageView profileImage=navView.getHeaderView(0).findViewById(R.id.profileImage);
        TextView nameHeader=navView.getHeaderView(0).findViewById(R.id.nameHeader);
        TextView mailHeader=navView.getHeaderView(0).findViewById(R.id.mailHeader);

        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                User user= new User().getUserFromFirebase(context,fbuser,nameHeader,mailHeader,profileImage,userHandler);
            }
        };
        Thread getUserThread=new Thread(runnable);
        getUserThread.start();
    }
    @Override
    public void onBackPressed(){
        if(drawerLayout.isOpen()){
            drawerLayout.close();
        }else{
            super.onBackPressed();
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.profile){
            Intent editprofileIntent = new Intent(MainActivity.this, EditProfileActivity.class);
            startActivity(editprofileIntent);
        }
        if(item.getItemId()==R.id.ownRecipes){
            Intent ownIntent = new Intent(MainActivity.this, SearchActivity.class);
            ownIntent.putExtra("select", "ownRecipes");
            startActivity(ownIntent);
        }
        if(item.getItemId()==R.id.likedRecipes){
            Intent likedIntent = new Intent(MainActivity.this, SearchActivity.class);
            likedIntent.putExtra("select", "likedRecipes");
            startActivity(likedIntent);

        }
        if(item.getItemId()==R.id.logout){
            auth = FirebaseAuth.getInstance();
            auth.signOut();
            Intent signoutIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(signoutIntent);
        }
        if(item.getItemId()==R.id.shoppingList){
            Intent toSlIntent = new Intent(MainActivity.this, ShoppinglistActivity.class);
            startActivity(toSlIntent);}
        return false;
    }

}
