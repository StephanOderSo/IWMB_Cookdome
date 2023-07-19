package com.bienhuels.iwmb_cookdome.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bienhuels.iwmb_cookdome.EditProfileActivity;
import com.bienhuels.iwmb_cookdome.R;
import com.bienhuels.iwmb_cookdome.SelectCategoryActivity;
import com.bienhuels.iwmb_cookdome.ViewModel.MainActivityVM;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    FloatingActionButton fab;
    DrawerLayout drawerLayout;
    FirebaseAuth auth;
    private MainActivityVM viewmodel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewmodel = new MainActivityVM();
//Create-Recipe Button
        fab=findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(MainActivity.this, CreateRecipeActivity.class);
                startActivity(intent);
                finish();
            }
        });
//Search Button
        View search = findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                intent.putExtra("search","search");
                startActivity(intent);
                finish();
            }
        });
//Burgermenu Navigation
        drawerLayout=findViewById(R.id.drawerLayout);
        NavigationView navView=findViewById(R.id.navView);
        navView.bringToFront();
        navView.setNavigationItemSelectedListener(this);
        ImageButton burgermenu2=findViewById(R.id.burgermenu);
        burgermenu2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.open();
            }
        });



        CardView randomcard=findViewById(R.id.randomcard);
        randomcard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FilterActivity.class);
                startActivity(intent);
                finish();
            }
        });
        CardView catcard=findViewById(R.id.catcard);
        catcard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SelectCategoryActivity.class);
                startActivity(intent);
                finish();
            }
        });
        CardView restcard=findViewById(R.id.restcard);
        restcard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FilterActivity.class);
                intent.putExtra("action","Resteverwertung");
                startActivity(intent);
                finish();
            }
        });
//Burgermenu header
        ImageView profileImage=navView.getHeaderView(0).findViewById(R.id.profileImage);
        TextView nameHeader=navView.getHeaderView(0).findViewById(R.id.nameHeader);
        auth=FirebaseAuth.getInstance();
        String id=auth.getCurrentUser().getUid();
        FirebaseDatabase database=FirebaseDatabase.getInstance();
        DatabaseReference ref=database.getReference("/Cookdome/Users");
        ref.child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    // dBRecipeList= snapshot.getValue(listType);
                    String name = snapshot.child("name").getValue(String.class);
                    String url = snapshot.child("photo").getValue(String.class);

                    Picasso.get()
                            .load(url)
                            .placeholder(R.drawable.camera)
                            .resize(150, 150)
                            .centerCrop()
                            .into(profileImage);
                    nameHeader.setText(name);


                } else {
                    Toast.makeText(MainActivity.this, "No userinformation found", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
        Log.d("menuitemid","worked");
        if(item.getItemId()==R.id.profile){
            Log.d("menuitemid","profile");
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
        return false;
    }

}
