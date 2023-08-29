package com.bienhuels.iwmb_cookdome;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import View.MainActivity;
import View.RegisterActivity;

public class ConfirmActivity extends AppCompatActivity {

    Button login,register;
    EditText enteremail,enterpassword;
    String email,password;
    DatabaseReference databaseReference;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference=database.getReference("/Cookdome/Users");
        enteremail=findViewById(R.id.editTextTextEmailAddress);
        Intent previousIntent=getIntent();
        if(previousIntent.hasExtra("email")){
            email=previousIntent.getStringExtra("email");
            enteremail.setText(email);
        }
        enterpassword=findViewById(R.id.editTextTextPassword);
        login=findViewById(R.id.loginBtn);
        login.setOnClickListener(view -> {
            if(enteremail.getText()==null){
                Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
                enteremail.requestFocus();
                enteremail.setError("email required");
            }  else if (enterpassword.getText()==null) {
                Toast.makeText(this, "Please enter Password", Toast.LENGTH_SHORT).show();
                enterpassword.requestFocus();
                enterpassword.setError("password required");
            }else{
                email=enteremail.getText().toString();
                password=enterpassword.getText().toString();
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(this, "enter valid email", Toast.LENGTH_SHORT).show();
                    enteremail.requestFocus();
                    enteremail.setError("enter valid email");
                }else{
                    confirmUser(email,password);
                }
            }
        });
        register=findViewById(R.id.registerBtn);
        register.setOnClickListener(view -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }
    @Override
    protected void onStart(){
        super.onStart();
        auth= FirebaseAuth.getInstance();
        if(auth.getCurrentUser()!=null){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void confirmUser(String email, String password){
        auth= FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Toast.makeText(this, R.string.confirmed, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }else{
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                try{
                    throw Objects.requireNonNull(task.getException());
                }catch (FirebaseAuthInvalidCredentialsException e){
                    enteremail.setError("Invalid credentials");
                    enterpassword.setError("Invalid credentials");
                    Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }catch (FirebaseAuthInvalidUserException e){
                    enteremail.setError("User doesnt exist");
                    enterpassword.setError("User doesnt exist");
                    Toast.makeText(this, "User doesnt exist", Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}