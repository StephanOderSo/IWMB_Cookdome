package View;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bienhuels.iwmb_cookdome.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

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
        register=findViewById(R.id.registerBtn);

        //login button
        login.setOnClickListener(view -> {
            if(enteremail.getText()==null){
                enteremail.requestFocus();
                enteremail.setError(getResources().getString(R.string.enterMail));
            }  else if (enterpassword.getText()==null) {
                enterpassword.requestFocus();
                enterpassword.setError(getResources().getString(R.string.enterPassword));
            } else if (enteremail.getText()!=null) {
                if(enteremail.getText().toString().equals("")){
                    enteremail.requestFocus();
                    enteremail.setError(getResources().getString(R.string.enterMail));}
                if (enterpassword.getText()!=null) {
                    if(enterpassword.getText().toString().equals("")){
                        enterpassword.requestFocus();
                        enterpassword.setError(getResources().getString(R.string.enterPassword));
                    }else{
                        email=enteremail.getText().toString();
                        password=enterpassword.getText().toString();
                        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            Toast.makeText(LoginActivity.this, R.string.invalidMail, Toast.LENGTH_SHORT).show();
                            enteremail.requestFocus();
                            enteremail.setError(getResources().getString(R.string.invalidMail));
                        }else{
                            loginUser(email,password);
                        }
                    }
                }
            }
        });
        //register button, end user to register
        register.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });

    }
    @Override
    protected void onStart(){
        super.onStart();
        auth= FirebaseAuth.getInstance();
        if(auth.getCurrentUser()!=null){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void loginUser(String email, String password){
        auth= FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Toast.makeText(LoginActivity.this, R.string.loginsuccess, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }else{
                Toast.makeText(LoginActivity.this, R.string.sthWrong, Toast.LENGTH_SHORT).show();
                try{
                    throw Objects.requireNonNull(task.getException());
                }catch (FirebaseAuthInvalidCredentialsException e){
                    enteremail.setError(getResources().getString(R.string.invalidCred));
                    enterpassword.setError(getResources().getString(R.string.invalidCred));
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.invalidCred), Toast.LENGTH_SHORT).show();
                }catch (FirebaseAuthInvalidUserException e){
                    enteremail.setError(getResources().getString(R.string.invalidCred));
                    enterpassword.setError(getResources().getString(R.string.invalidCred));
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.invalidCred), Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}