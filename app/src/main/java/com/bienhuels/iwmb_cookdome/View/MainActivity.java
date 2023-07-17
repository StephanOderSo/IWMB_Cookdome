package com.bienhuels.iwmb_cookdome.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bienhuels.iwmb_cookdome.R;
import com.bienhuels.iwmb_cookdome.ViewModel.MainActivityVM;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    //region Components
    DrawerLayout dlMainActivity;
    FirebaseAuth auth;
    //endregion

    private MainActivityVM viewmodel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewmodel = new MainActivityVM();

        //Search Button (click leads to SearchActivity)
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


        ImageButton burgermenu=findViewById(R.id.burgermenu);
        burgermenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dlMainActivity.open();
            }
        });
        //Burgermenu Navigation (Change Front (clickable) layout from main activity to Drawer to make Drawer-Items clickable)
        dlMainActivity=findViewById(R.id.dlMainActivity);
        NavigationView navView=findViewById(R.id.navView);
        navView.bringToFront();
        navView.setNavigationItemSelectedListener(this);

        //Burgermenu header (Load users Profile image and name from firebase to display in Drawer header)
        ImageView profileImage=navView.getHeaderView(0).findViewById(R.id.profileImage);
        TextView nameHeader=navView.getHeaderView(0).findViewById(R.id.nameHeader);
        auth=FirebaseAuth.getInstance();
//        String id=auth.getCurrentUser().getUid();
        FirebaseDatabase database=FirebaseDatabase.getInstance();
        DatabaseReference ref=database.getReference("/Cookdome/Users");

//        try {
//            ref.child("-NUHOTcWp06mne3AqB2b").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<DataSnapshot> task) {
//                    if (task.getResult().exists()) {
//                        DataSnapshot snapshot = task.getResult();
//                        // dBRecipeList= snapshot.getValue(listType);
//                        String name = snapshot.child("name").getValue(String.class);
//                        String url = snapshot.child("photo").getValue(String.class);
//
//                        Picasso.get()
//                                .load(url)
//                                .placeholder(R.drawable.camera)
//                                .resize(150, 150)
//                                .centerCrop()
//                                .into(profileImage);
//                        nameHeader.setText(name);
//
//
//                    } else {
//                        Toast.makeText(MainActivity.this, "No userinformation found", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//
//        } catch(Exception ex){
//            Toast.makeText(MainActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
//        }

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }
}