package com.bienhuels.iwmb_cookdome.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.bienhuels.iwmb_cookdome.LoginVM;
import com.bienhuels.iwmb_cookdome.R;
import com.bienhuels.iwmb_cookdome.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLoginBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        LoginVM viewmodel = new LoginVM();
        binding.setViewmodel(viewmodel);
    }
}