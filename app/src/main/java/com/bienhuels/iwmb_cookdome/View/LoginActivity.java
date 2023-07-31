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

        LoginVM viewmodel = new LoginVM();
        binding.setViewmodel(viewmodel);
        binding.executePendingBindings();
    }

    //Todo: 2 Params uebergeben
    @BindingAdapter({"bind:user"})
    public static void switchActivity(View view, User user){
        if(user != null)
        {
            if(user.getLoginSucceeded())
            {
                Intent intent = new Intent(view.getContext(), MainActivity.class);
                view.getContext().startActivity(intent);
            }
            else
            {
                Toast.makeText(view.getContext(), "###Failed to login. Check password and mail###", Toast.LENGTH_SHORT).show();
            }
        }

//        finish();

    }

//    @BindingAdapter({"toastMessage"})
//    public static void showToastMessage(View view, String message) {
//            if(message != null)
//                Toast.makeText(view.getContext(), message, Toast.LENGTH_SHORT).show();
//    }
}