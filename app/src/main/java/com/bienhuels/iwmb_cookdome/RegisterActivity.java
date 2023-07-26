package com.bienhuels.iwmb_cookdome;

import static android.content.ContentValues.TAG;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import View.LoginActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.ArrayList;
import Model.User;

public class RegisterActivity extends AppCompatActivity {
    ImageView photo;
    Uri imageUri;
    String name,email,password;
    EditText nameView,emailView,passwordView,passwordRepeatView;
    Button registerBtn;
    DatabaseReference databaseReference;
    ArrayList<User> dBUsers;
    ProgressBar progressBar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference=database.getReference("Cookdome");
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
                        imageUri=data.getData();
                        photo.setImageURI(imageUri);
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
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    Log.d(TAG, "step1");
                    name = nameView.getText().toString();
                    email = emailView.getText().toString();
                    password = passwordView.getText().toString();
                    String repeatPassword = passwordRepeatView.getText().toString();
                    if (!password.equals(repeatPassword)) {
                        Toast.makeText(RegisterActivity.this, R.string.wrongPassword, Toast.LENGTH_SHORT).show();
                        passwordView.clearComposingText();
                        passwordRepeatView.clearComposingText();
                    } else {
                        checkUsers();
                        if (dBUsers.contains(email)) {
                            Toast.makeText(RegisterActivity.this, R.string.userExists, Toast.LENGTH_SHORT).show();
                        } else {
                            uploadToFirebase();
                        }
                    }
                }
            }
        });

    }

    private void checkUsers() {
        dBUsers=new ArrayList<User>();
    }
    public void saveUser(Uri uri){
        auth= FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser dBUser=auth.getCurrentUser();
                            String userID=dBUser.getUid();
                            User user=new User(name,uri.toString());
                            databaseReference.child("Users").child(userID).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(RegisterActivity.this,R.string.uploadSuccess,Toast.LENGTH_SHORT).show();
                                        //                                    dBUser.sendEmailVerification();
                                        Intent toLoginIntent=new Intent(getApplicationContext(), LoginActivity.class);
                                        toLoginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                        toLoginIntent.putExtra("email",email);
                                        startActivity(toLoginIntent);
                                        finish();

                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(RegisterActivity.this,e.getMessage().toString(),Toast.LENGTH_SHORT);

                                }
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, R.string.authFailed,
                                    Toast.LENGTH_SHORT).show();
                            try{
                                throw task.getException();
                            }catch(Exception e){
                                Log.d(TAG, e.getMessage());
                            }
                        }

                    }
                });



    }
    public void uploadToFirebase(){
        StorageReference storageRef= FirebaseStorage.getInstance().getReference().child("UserImages").child(imageUri.getLastPathSegment());
        storageRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                while(!uriTask.isComplete());
                Uri uriImage=uriTask.getResult();
                Uri imageUriNew=uriImage;
                saveUser(imageUriNew);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this,e.getMessage().toString(),Toast.LENGTH_SHORT);

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                Long progress=(100*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                progressBar.setProgress(Integer.parseInt(progress.toString()));
            }
        });

    }
}