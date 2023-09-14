package View;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
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
import com.google.firebase.database.FirebaseDatabase;

import Model.User;

public class EditProfileActivity extends AppCompatActivity {
    ImageView photo;
    FirebaseUser fbuser;
    FirebaseAuth auth;
    FirebaseDatabase database;
    String newname,newemail,newpass,newpassrepeat;
    TextView nameView,emailView,passwordView;
    ImageButton editname,editemail,editpassword,namedone,emaildone,passworddone;
    Button saveUserchange,cancelUserchange;
    EditText nameEditor,emailEditor,passEditor,repeatPassEditor;
    Uri imageUri;
    User user=new User();
    Context context;

    Handler userHandler=new Handler();


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
        fbuser=auth.getCurrentUser();
        context=getApplicationContext();
        Intent toMainIntent=new Intent(EditProfileActivity.this,MainActivity.class);
        Intent toLoginIntent=new Intent(EditProfileActivity.this,LoginActivity.class);

        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                setUserData();
            }
        };
        Thread getInfoThread=new Thread(runnable);
        getInfoThread.start();


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
            checkPassword();
        });


        saveUserchange.setOnClickListener(view -> {
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle(R.string.confirmIdentity);
            builder.setCancelable(false);
            EditText passView=new EditText(this);
            passView.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            builder.setView(passView);

            builder.setPositiveButton("OK", (dialogInterface, i) -> {
                if(fbuser==null){
                    Toast.makeText(this, R.string.signedOut, Toast.LENGTH_SHORT).show();
                    startActivity(toLoginIntent);
                    finish();
                }  else if (passView.getText()==null) {
                    Toast.makeText(this, R.string.enterPassword, Toast.LENGTH_SHORT).show();
                    passView.requestFocus();
                    passView.setError(getResources().getString(R.string.enterPassword));
                }else{
                    String pass=passView.getText().toString();
                    String mail=fbuser.getEmail();
                    User user=new User();
                    user.updateOnFirebase(fbuser,newname,newemail,newpass,imageUri,context,mail,pass);
                }
            });
            builder.show();


        });
        cancelUserchange.setOnClickListener(view -> {
            newemail=null;
            newpass=null;
            newpassrepeat=null;
            imageUri=null;
            startActivity(toMainIntent);
        });

    }
    public void checkPassword(){
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
    }

    public void setUserData(){
        user=new User().downloadFromFirebase(context,fbuser,nameView,emailView,photo,userHandler);
    }
    @Override
    public void onBackPressed() {
        Intent toMainIntent=new Intent(EditProfileActivity.this,MainActivity.class);
        startActivity(toMainIntent);}

}