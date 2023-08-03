package View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bienhuels.iwmb_cookdome.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import Model.User;

public class EditProfileActivity extends AppCompatActivity {
    ImageView photo;
    FirebaseUser fbuser;
    FirebaseAuth auth;
    User user;
    DatabaseReference databaseReferenceUsers;
    FirebaseDatabase database;
    String name,email,url,id,newname,newemail,newpass,newpassrepeat;
    TextView nameView,emailView,passwordView;
    ImageButton editname,editemail,editpassword,namedone,emaildone,passworddone;
    Button saveUserchange,cancelUserchange;
    EditText nameEditor,emailEditor,passEditor,repeatPassEditor;
    Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        photo=findViewById(R.id.photo);
        nameView=findViewById(R.id.nameView);
        emailView=findViewById(R.id.emailView);
        passwordView=findViewById(R.id.passwordView);
        editname=findViewById(R.id.editname);
        editemail=findViewById(R.id.editemail);
        editpassword=findViewById(R.id.editpassword);
        namedone=findViewById(R.id.namedone);
        emaildone=findViewById(R.id.emaildone);
        passworddone=findViewById(R.id.passworddone);
        nameEditor=findViewById(R.id.name);
        emailEditor=findViewById(R.id.email);
        passEditor=findViewById(R.id.password);
        repeatPassEditor=findViewById(R.id.passwordRepeat);
        saveUserchange=findViewById(R.id.saveUserchange);
        cancelUserchange=findViewById(R.id.cancelUserChange);
        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
        getUserinfo();
//Edit photo
        ActivityResultLauncher<Intent> activityResultLauncher= registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode()== Activity.RESULT_OK){
                        Intent data=result.getData();
                        imageUri=data.getData();
                        photo.setImageURI(imageUri);
                    }else {
                        Toast.makeText(EditProfileActivity.this,"No Image Selected",Toast.LENGTH_SHORT).show();
                    }
                }
        );
        photo.setOnClickListener(view -> {
            Intent photoPicker = new Intent();
            photoPicker.setAction(Intent.ACTION_GET_CONTENT);
            photoPicker.setType("*/*");
            activityResultLauncher.launch(photoPicker);

        });
        editname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameView.setVisibility(View.GONE);
                editname.setVisibility(View.GONE);
                nameEditor.setVisibility(View.VISIBLE);
                namedone.setVisibility(View.VISIBLE);
            }
        });
        editemail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emailView.setVisibility(View.GONE);
                editemail.setVisibility(View.GONE);
                emailEditor.setVisibility(View.VISIBLE);
                emaildone.setVisibility(View.VISIBLE);
            }
        });
        editpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passwordView.setVisibility(View.GONE);
                editpassword.setVisibility(View.GONE);
                passEditor.setVisibility(View.VISIBLE);
                repeatPassEditor.setVisibility(View.VISIBLE);
                passworddone.setVisibility(View.VISIBLE);
            }
        });
        namedone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!nameEditor.getText().toString().equals("")){
                    newname=nameEditor.getText().toString();
                    nameEditor.setVisibility(View.GONE);
                    namedone.setVisibility(View.GONE);
                    nameView.setVisibility(View.VISIBLE);
                    nameView.setText(newname);
                    editname.setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(EditProfileActivity.this, "Please enter Name", Toast.LENGTH_SHORT).show();
                }
            }
        });
        emaildone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!emailEditor.getText().toString().equals("")){
                    newemail=emailEditor.getText().toString();
                    Log.d("email", newemail);
                    emailEditor.setVisibility(View.GONE);
                    emaildone.setVisibility(View.GONE);
                    emailView.setVisibility(View.VISIBLE);
                    emailView.setText(newemail);
                    editemail.setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(EditProfileActivity.this, "Please enter email", Toast.LENGTH_SHORT).show();
                }
            }
        });
        passworddone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!passEditor.getText().toString().equals("")){
                    if(!repeatPassEditor.getText().toString().equals("")){
                        newpass=passEditor.getText().toString();
                        newpassrepeat=repeatPassEditor.getText().toString();
                        if(newpass.length()>=8){
                            if(newpass.equals(newpassrepeat)){
                                passEditor.setVisibility(View.GONE);
                                repeatPassEditor.setVisibility(View.GONE);
                                passwordView.setVisibility(View.VISIBLE);
                                passworddone.setVisibility(View.GONE);
                                editpassword.setVisibility(View.VISIBLE);
                            }else{
                                Toast.makeText(EditProfileActivity.this, "passwords don't match", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(EditProfileActivity.this, "Password must be min 8 characters", Toast.LENGTH_SHORT).show();
                        }

                    }else{
                        Toast.makeText(EditProfileActivity.this, "enter password Repeat", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(EditProfileActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Intent toMainIntent=new Intent(EditProfileActivity.this,MainActivity.class);
        saveUserchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String,Object> update=new HashMap();
                if(newname!=null){
                    update.put("name",newname);}
                if(imageUri!=null){
                    update.put("photo",imageUri.toString());}
                if(update.size()==0){
                    Log.d("TAG", "no additional userinfo");
                    if (newemail != null) {
                        Log.d("TAG", "email not null");
                        fbuser.updateEmail(newemail).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("TAG", "User email address updated.");
                                    if(newpass!=null){
                                        Log.d("TAG", "pass is next");
                                    }else{
                                        startActivity(toMainIntent);
                                    }
                                }
                            }
                        });
                    }
                    if(newpass!=null){
                        fbuser.updatePassword(newpass).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("TAG", "User password updated.");
                                    startActivity(toMainIntent);
                                }
                            }
                        });
                    } else{Toast.makeText(EditProfileActivity.this, "no changes entered", Toast.LENGTH_SHORT).show();}



                }else{
                    databaseReferenceUsers.child(id).updateChildren(update).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()){
                                Log.d("TAG", "saved");
                                if (newemail != null) {
                                    fbuser.updateEmail(newemail);
                                }
                                if(newpass!=null){
                                    fbuser.updatePassword(newpass);}

                                startActivity(toMainIntent);
                            }else{
                                Toast.makeText(EditProfileActivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });}

            }
        });
        cancelUserchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newemail=null;
                newpass=null;
                newpassrepeat=null;
                imageUri=null;
                startActivity(toMainIntent);
            }
        });

    }
    public void getUserinfo(){
        fbuser=auth.getCurrentUser();
        if(fbuser!=null) {
            id = fbuser.getUid();
            email = fbuser.getEmail();
            databaseReferenceUsers = database.getReference("/Cookdome/Users");
            databaseReferenceUsers.child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.getResult().exists()) {
                        DataSnapshot snapshot = task.getResult();
                        // dBRecipeList= snapshot.getValue(listType);
                        name = snapshot.child("name").getValue(String.class);
                        url = snapshot.child("photo").getValue(String.class);

                        Picasso.get()
                                .load(url)
                                .placeholder(R.drawable.camera)
                                .resize(400, 400)
                                .centerCrop()
                                .into(photo);
                        nameView.setText(name);
                        emailView.setText(email);

                    } else {
                        Toast.makeText(EditProfileActivity.this, "No userinformation found", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }else{
            Toast.makeText(this, "no user logged in", Toast.LENGTH_SHORT).show();}
    }
    @Override
    public void onBackPressed() {
        Intent toMainIntent=new Intent(EditProfileActivity.this,MainActivity.class);
        startActivity(toMainIntent);}

}