package com.bienhuels.iwmb_cookdome.Model;

import java.util.ArrayList;

public class User {
    String name,photo;
    ArrayList<Recipe>recipes,favourites;


    public User(){}
    public User(String name, String photo) {
        this.name = name;
        this.photo = photo;
    }

    public User(String name, String photo, ArrayList<Recipe> recipes, ArrayList<Recipe> favourites) {
        this.name = name;
        this.photo = photo;
        this.recipes = recipes;
        this.favourites=favourites;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public ArrayList<Recipe> getRecipes() {
        return recipes;
    }

    public void setRecipes(ArrayList<Recipe> recipes) {
        this.recipes = recipes;
    }

    public ArrayList<Recipe> getFavourites() {
        return favourites;
    }

    public void setFavourites(ArrayList<Recipe> favourites) {
        this.favourites = favourites;
    }
}
