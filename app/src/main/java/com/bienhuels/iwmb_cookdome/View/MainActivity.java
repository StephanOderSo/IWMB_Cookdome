package com.bienhuels.iwmb_cookdome.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bienhuels.iwmb_cookdome.R;
import com.bienhuels.iwmb_cookdome.ViewModel.MainActivityVM;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;


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

        //Burgermenu Navigation
        dlMainActivity=findViewById(R.id.dlMainActivity);
        NavigationView navView=findViewById(R.id.navView);
        navView.bringToFront();
        navView.setNavigationItemSelectedListener(this);
        ImageButton burgermenu=findViewById(R.id.burgermenu);
        burgermenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dlMainActivity.open();
            }
        });


        //Burgermenu header
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