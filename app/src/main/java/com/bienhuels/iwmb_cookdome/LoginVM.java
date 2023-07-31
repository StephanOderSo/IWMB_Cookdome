package com.bienhuels.iwmb_cookdome;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.bienhuels.iwmb_cookdome.Model.User;
import com.bienhuels.iwmb_cookdome.View.MainActivity;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.logging.Logger;

public class LoginVM extends BaseObservable {
    public LoginVM(){ }

    @Bindable
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        notifyPropertyChanged(BR.user);
    }

    FirebaseAuth auth;

    public void onClickLogin(String email, String password){
        user = new User();
        user.setEmail(email);
        user.setPassword(password);
        if(email.isEmpty() || password.isEmpty())
        {
            setUser(user);
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            return;
        }

        auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this::onCompleteLogin);
    }

    public void onClickRegister() throws Exception {
        throw new Exception("Not implemented");
    }

    private void onCompleteLogin(@NonNull Task<AuthResult> task) {
        if(task.isSuccessful())
        {
            //ToDo: Muss auch anders moeglich sein, mit dem Binding zu arbeiten...
            user.setLoginSucceeded(true);
            this.setUser(user);
        }
        else {
            user.setLoginSucceeded(false);
            this.setUser(user);
        }
    }
}
