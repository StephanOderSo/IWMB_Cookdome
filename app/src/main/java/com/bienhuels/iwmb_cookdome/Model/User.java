package com.bienhuels.iwmb_cookdome.Model;

public class User {

    private String password;
    private String email;
    private String username;
    private boolean loginSucceeded;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public void setLoginSucceeded(boolean loginSucceeded)
    {
        this.loginSucceeded = loginSucceeded;
    }
    public boolean getLoginSucceeded()
    {
        return loginSucceeded;
    }
}
