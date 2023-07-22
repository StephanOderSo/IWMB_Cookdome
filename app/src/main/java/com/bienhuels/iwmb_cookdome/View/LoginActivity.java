package com.bienhuels.iwmb_cookdome.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bienhuels.iwmb_cookdome.LoginVM;
import com.bienhuels.iwmb_cookdome.Model.User;
import com.bienhuels.iwmb_cookdome.R;
import com.bienhuels.iwmb_cookdome.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLoginBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        LoginVM viewmodel = new LoginVM(this);
        binding.setViewmodel(viewmodel);
        binding.executePendingBindings();
    }

    //Todo: 2 Params uebergeben
    @BindingAdapter({"bind:login", "bind:loginSuccess"})
    public static void switchActivity(View view, User user, boolean loginSuccess){
        if(user != null && loginSuccess)
        {
            Intent intent = new Intent(view.getContext(), MainActivity.class);
            view.getContext().startActivity(intent);
        }

//        finish();

    }

    @BindingAdapter({"toastMessage"})
    public static void showToastMessage(View view, String message) {
            if(message != null)
                Toast.makeText(view.getContext(), message, Toast.LENGTH_SHORT).show();
    }
}