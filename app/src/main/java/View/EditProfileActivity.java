package View;

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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bienhuels.iwmb_cookdome.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {
    ImageView photo;
    FirebaseUser fbuser;
    FirebaseAuth auth;
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
                        try{
                            imageUri=data.getData();
                            photo.setImageURI(imageUri);
                        } catch(NullPointerException e){Toast.makeText(EditProfileActivity.this,R.string.no_image_selected,Toast.LENGTH_SHORT).show();}
                    }else {
                        Toast.makeText(EditProfileActivity.this,R.string.no_image_selected,Toast.LENGTH_SHORT).show();
                    }
                }
        );
        photo.setOnClickListener(view -> {
            Intent photoPicker = new Intent();
            photoPicker.setAction(Intent.ACTION_GET_CONTENT);
            photoPicker.setType("*/*");
            activityResultLauncher.launch(photoPicker);

        });
        editname.setOnClickListener(view -> {
            nameView.setVisibility(View.GONE);
            editname.setVisibility(View.GONE);
            nameEditor.setVisibility(View.VISIBLE);
            namedone.setVisibility(View.VISIBLE);
        });
        editemail.setOnClickListener(view -> {
            emailView.setVisibility(View.GONE);
            editemail.setVisibility(View.GONE);
            emailEditor.setVisibility(View.VISIBLE);
            emaildone.setVisibility(View.VISIBLE);
        });
        editpassword.setOnClickListener(view -> {
            passwordView.setVisibility(View.GONE);
            editpassword.setVisibility(View.GONE);
            passEditor.setVisibility(View.VISIBLE);
            repeatPassEditor.setVisibility(View.VISIBLE);
            passworddone.setVisibility(View.VISIBLE);
        });
        namedone.setOnClickListener(view -> {
            if(!nameEditor.getText().toString().equals("")){
                newname=nameEditor.getText().toString();
                nameEditor.setVisibility(View.GONE);
                namedone.setVisibility(View.GONE);
                nameView.setVisibility(View.VISIBLE);
                nameView.setText(newname);
                editname.setVisibility(View.VISIBLE);
            }else{
                Toast.makeText(EditProfileActivity.this, R.string.enterName, Toast.LENGTH_SHORT).show();
            }
        });
        emaildone.setOnClickListener(view -> {
            if(!emailEditor.getText().toString().equals("")){
                newemail=emailEditor.getText().toString();
                emailEditor.setVisibility(View.GONE);
                emaildone.setVisibility(View.GONE);
                emailView.setVisibility(View.VISIBLE);
                emailView.setText(newemail);
                editemail.setVisibility(View.VISIBLE);
            }else{
                Toast.makeText(EditProfileActivity.this, R.string.enterMail, Toast.LENGTH_SHORT).show();
            }
        });
        passworddone.setOnClickListener(view -> {
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
                            Toast.makeText(EditProfileActivity.this, R.string.noPassMatch, Toast.LENGTH_LONG).show();}
                    }else{
                        Toast.makeText(EditProfileActivity.this, R.string.minLength, Toast.LENGTH_LONG).show();}
                }else{
                    Toast.makeText(EditProfileActivity.this, R.string.repeatPasword, Toast.LENGTH_SHORT).show();}
            }else{
                Toast.makeText(EditProfileActivity.this, R.string.enterPassword, Toast.LENGTH_SHORT).show();}
        });
        Intent toMainIntent=new Intent(EditProfileActivity.this,MainActivity.class);
        saveUserchange.setOnClickListener(view -> {
            HashMap<String,Object> update=new HashMap<>();
            if(newname!=null){
                update.put("name",newname);}
            if(imageUri!=null){
                update.put("photo",imageUri.toString());}
            if(update.size()==0){
                Log.d("TAG", "no additional userinfo");
                if (newemail != null) {
                    fbuser.updateEmail(newemail).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if(newpass!=null){
                                Log.d("TAG", "pass is next");
                            }else{
                                startActivity(toMainIntent);
                            }
                        }
                    });
                }
                if(newpass!=null){
                    fbuser.updatePassword(newpass).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            startActivity(toMainIntent);
                        }
                    });
                } else{Toast.makeText(EditProfileActivity.this, R.string.noChange, Toast.LENGTH_SHORT).show();}

            }else{
                databaseReferenceUsers.child(id).updateChildren(update).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        if (newemail != null) {
                            fbuser.updateEmail(newemail);
                        }
                        if(newpass!=null){
                            fbuser.updatePassword(newpass);}

                        startActivity(toMainIntent);
                    }else{
                        Toast.makeText(EditProfileActivity.this, R.string.sthWrong, Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());}

        });
        cancelUserchange.setOnClickListener(view -> {
            newemail=null;
            newpass=null;
            newpassrepeat=null;
            imageUri=null;
            startActivity(toMainIntent);
        });

    }
    public void getUserinfo(){
        fbuser=auth.getCurrentUser();
        if(fbuser!=null) {
            id = fbuser.getUid();
            email = fbuser.getEmail();
            databaseReferenceUsers = database.getReference("/Cookdome/Users");
            databaseReferenceUsers.child(id).get().addOnCompleteListener(task -> {
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
            }).addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());

        }else{
            Toast.makeText(this, "no user logged in", Toast.LENGTH_SHORT).show();}
    }
    @Override
    public void onBackPressed() {
        Intent toMainIntent=new Intent(EditProfileActivity.this,MainActivity.class);
        startActivity(toMainIntent);}

}