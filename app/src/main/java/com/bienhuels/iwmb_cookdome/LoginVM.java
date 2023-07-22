package com.bienhuels.iwmb_cookdome;

import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.logging.Logger;

public class LoginVM extends BaseObservable {
    public LoginVM(){}

    FirebaseAuth auth;

    public void onClickLogin(View view, String email, String password){
        if(email.isEmpty() || email == null || password.isEmpty() || password == null)
        {
            Toast.makeText(view.getContext(), "###Password or Email empty###", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            Toast.makeText(view.getContext(), "###Wrong Format for email###", Toast.LENGTH_SHORT).show();
            return;
        }

        auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this::onCompleteLogin);

    }

    private void onCompleteLogin(@NonNull Task<AuthResult> task)
    {
        if(task.isSuccessful())
        {

        }
    }

}
