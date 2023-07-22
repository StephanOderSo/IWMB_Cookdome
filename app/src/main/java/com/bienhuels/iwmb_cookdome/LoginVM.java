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
    public LoginVM(Context viewContext){

        this.viewContext = viewContext;
    }
    private Context viewContext;

    @Bindable
    private String toastMessage = null;
    public String getToastMessage() {
        return toastMessage;
    }

    public void setToastMessage(String toastMessage) {
        this.toastMessage = toastMessage;
        notifyPropertyChanged(BR.toastMessage);
    }

    @Bindable
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        notifyPropertyChanged(BR.user);
    }

    @Bindable
    private boolean loginSuccess;
    public boolean isLoginSuccess() {
        return loginSuccess;
    }

    public void setLoginSuccess(boolean loginSuccess) {
        this.loginSuccess = loginSuccess;
        notifyPropertyChanged(BR.loginSuccess);
    }

    FirebaseAuth auth;

    public void onClickLogin(String email, String password){
        User loginUser = new User();

        if(email.isEmpty() || password.isEmpty())
        {
            String message = "###Password or Email empty###";
            setToastMessage(message);
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            Toast.makeText(viewContext, "###Wrong Format for email###", Toast.LENGTH_SHORT).show();
            return;
        }

        loginUser.setEmail(email);
        loginUser.setPassword(password);
        this.setUser(loginUser);
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
            this.setUser(user);
            this.setLoginSuccess(true);
        }
        else {
            this.setUser(null);
            Toast.makeText(viewContext, "###Failure while Login###", Toast.LENGTH_SHORT).show();
            this.setLoginSuccess(false);
        }
    }
}
