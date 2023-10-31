package View;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bienhuels.iwmb_cookdome.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import Model.Firebase;
import Model.User;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    FloatingActionButton fab;
    DrawerLayout drawerLayout;
    FirebaseAuth auth=FirebaseAuth.getInstance();
    FirebaseUser fbuser;
    Context context;
    Handler userHandler=new Handler();
    User user=new User();
    ImageView profileImage;
    TextView mailHeader,nameHeader;
    View search;
    ImageButton burgermenu2;
    NavigationView navView;
    CardView randomcard,restcard,catcard;
    Firebase firebase=new Firebase();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fbuser=auth.getCurrentUser();
        context=getApplicationContext();
        fab=findViewById(R.id.fab);
        search = findViewById(R.id.search);
        drawerLayout=findViewById(R.id.drawerLayout);
        navView=findViewById(R.id.navView);
        burgermenu2=findViewById(R.id.burgermenu);
        randomcard=findViewById(R.id.randomcard);
        catcard=findViewById(R.id.catcard);
        restcard=findViewById(R.id.restcard);


//Burgermenu header
        profileImage=navView.getHeaderView(0).findViewById(R.id.profileImage);
        nameHeader=navView.getHeaderView(0).findViewById(R.id.nameHeader);
        mailHeader=navView.getHeaderView(0).findViewById(R.id.mailHeader);

        //Create-Recipe Button (Click leads to create recipe activity)
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, CreateRecipeActivity.class);
            startActivity(intent);
            finish();
        });
//Search Button (Click leads to Search activity with comment "search" to indicate source-activity)
        search.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            intent.putExtra("search","search");
            startActivity(intent);
            finish();
        });
//button to open Navigation-Drawer
        burgermenu2.setOnClickListener(view -> drawerLayout.open());

//Random Recipe Button
        randomcard.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, FilterActivity.class);
            startActivity(intent);
            finish();
        });
//Categories Button
        catcard.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SelectCategoryActivity.class);
            startActivity(intent);
            finish();
        });
//Leftovers  Button
        restcard.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, FilterActivity.class);
            intent.putExtra("action","action");
            startActivity(intent);
            finish();
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        navView.bringToFront();
        navView.setNavigationItemSelectedListener(this);
        setData();
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    public void setData(){
        Runnable setDataRun= () -> userHandler.post(() -> {
            user=firebase.getUser();
            Picasso.get()
                    .load(user.getPhoto())
                    .placeholder(R.drawable.image)
                    .resize(400, 400)
                    .centerCrop()
                    .into(profileImage);
            nameHeader.setText(user.getName());
            mailHeader.setText(fbuser.getEmail());
        });
        Thread setDataThread=new Thread(setDataRun);

        Runnable runnable= () -> firebase.downloadUser(context,fbuser,userHandler,setDataThread);
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
            finish();
        }
        if(item.getItemId()==R.id.ownRecipes){
            Intent ownIntent = new Intent(MainActivity.this, SearchActivity.class);
            ownIntent.putExtra("select", "ownRecipes");
            startActivity(ownIntent);
            finish();
        }
        if(item.getItemId()==R.id.likedRecipes){
            Intent likedIntent = new Intent(MainActivity.this, SearchActivity.class);
            likedIntent.putExtra("select", "likedRecipes");
            startActivity(likedIntent);
            finish();
        }
        if(item.getItemId()==R.id.logout){
            auth = FirebaseAuth.getInstance();
            auth.signOut();
            Intent signoutIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(signoutIntent);
            finish();
        }
        if(item.getItemId()==R.id.shoppingList){
            Intent toSlIntent = new Intent(MainActivity.this, ShoppinglistActivity.class);
            startActivity(toSlIntent);
            finish();}
        if(item.getItemId()==R.id.sharedRecipes){
            Intent toSlIntent = new Intent(MainActivity.this, SearchActivity.class);
            toSlIntent.putExtra("shared","");
            startActivity(toSlIntent);
            finish();
        }
        return false;
    }

}
