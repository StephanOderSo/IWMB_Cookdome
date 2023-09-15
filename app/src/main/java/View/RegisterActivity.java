package View;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bienhuels.iwmb_cookdome.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import Model.User;

public class RegisterActivity extends AppCompatActivity {
    ImageView photo;
    Uri imageUri;
    String name,email,password;
    EditText nameView,emailView,passwordView,passwordRepeatView;
    Button registerBtn;
    DatabaseReference databaseReference;
    ProgressBar progressBar;
     FirebaseAuth auth;
     User user=new User();
     Thread registerThread;
     Handler handler=new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference=database.getReference("Cookdome");
        auth=FirebaseAuth.getInstance();

        Context context=getApplicationContext();
        setContentView(R.layout.activity_register);
        nameView=findViewById(R.id.name);
        emailView=findViewById(R.id.email);
        passwordView=findViewById(R.id.password);
        passwordRepeatView=findViewById(R.id.passwordRepeat);
        registerBtn=findViewById(R.id.registerBtn);
        progressBar=findViewById(R.id.progressBarRegister);
//Profilephoto
        photo=findViewById(R.id.photo);
        ActivityResultLauncher<Intent> activityResultLauncher= registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode()== Activity.RESULT_OK){
                        Intent data=result.getData();
                        if(data!=null){
                            imageUri=data.getData();
                            photo.setImageURI(imageUri);
                        }else {
                            Toast.makeText(RegisterActivity.this,R.string.no_image_selected,Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(RegisterActivity.this,R.string.no_image_selected,Toast.LENGTH_SHORT).show();
                    }
                }
        );
        photo.setOnClickListener(view -> {
            Intent photoPicker = new Intent();
            photoPicker.setAction(Intent.ACTION_GET_CONTENT);
            photoPicker.setType("*/*");
            activityResultLauncher.launch(photoPicker);

        });

//Register
        registerBtn.setOnClickListener(view -> {
            if (imageUri == null) {
                Toast.makeText(RegisterActivity.this, R.string.choosePhoto, Toast.LENGTH_SHORT).show();
            }
            else if (nameView.getText().toString().equals("")) {
                Toast.makeText(RegisterActivity.this, R.string.enterName, Toast.LENGTH_SHORT).show();
                nameView.requestFocus();
            }
            else if (emailView.getText().toString().equals("")) {
                Toast.makeText(RegisterActivity.this, R.string.enterMail, Toast.LENGTH_SHORT).show();
            }
            else if (!Patterns.EMAIL_ADDRESS.matcher(emailView.getText().toString()).matches()) {
                Toast.makeText(RegisterActivity.this, R.string.invalidMail, Toast.LENGTH_SHORT).show();
            }
            else if (passwordView.getText().toString().equals("")) {
                Toast.makeText(RegisterActivity.this, R.string.enterPassword, Toast.LENGTH_SHORT).show();
            }
            else if (passwordView.getText().toString().length()<8) {
                Toast.makeText(RegisterActivity.this, R.string.passwordLength, Toast.LENGTH_SHORT).show();
                passwordView.clearComposingText();
            }
            else if (passwordRepeatView.getText().toString().equals("")) {
                Toast.makeText(RegisterActivity.this, R.string.repeatPasword, Toast.LENGTH_SHORT).show();
            } else {
                name = nameView.getText().toString();
                email = emailView.getText().toString();
                password = passwordView.getText().toString();
                String repeatPassword = passwordRepeatView.getText().toString();
                if (!password.equals(repeatPassword)) {
                    Toast.makeText(RegisterActivity.this, R.string.wrongPassword, Toast.LENGTH_LONG).show();
                    passwordView.clearComposingText();
                    passwordRepeatView.clearComposingText();
                } else{
                    Runnable runnable= () -> user.uploadToFirebase(imageUri,name,email,password,context,handler);
                    registerThread=new Thread(runnable);
                    registerThread.start();
                }

            }
        });

    }



}