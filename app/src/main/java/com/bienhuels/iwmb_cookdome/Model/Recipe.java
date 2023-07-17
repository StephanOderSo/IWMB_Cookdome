package com.bienhuels.iwmb_cookdome.Model;

import java.util.ArrayList;

public class Recipe {
    String key;
   /** private UUID id;**/
    private String image;
    private String recipeName;
    private int prepTime;
    private ArrayList<Ingredient> ingredientList;
    private String category;
    private Integer portions;
    private ArrayList<String> StepList;
    private ArrayList<String> dietaryRec;

    public Recipe(){}


    public Recipe(String key, String image, String recipeName, String category, int prepTime, int portions, ArrayList<Ingredient> ingredientList, ArrayList<String> stepList, ArrayList<String> dietRec) {
       /** this.id=nextUID();**/
       this.key=key;
        this.image=image;
        this.recipeName = recipeName;
        this.prepTime = prepTime;
        this.portions=portions;
        this.ingredientList = ingredientList;
        this.category = category;
        this.StepList = stepList;
        this.dietaryRec=dietRec;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String id) {
        this.key=key;
    }

    public Integer getPortions() {
        return portions;
    }

    public void setPortions(Integer portions) {
        this.portions = portions;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public int getPrepTime() {
        return prepTime;
    }

    public void setPrepTime(int prepTime) {
        this.prepTime = prepTime;
    }

    public ArrayList<Ingredient> getIngredientList() {
        return ingredientList;
    }

    public void setIngredientList(ArrayList<Ingredient> ingredientList) {
        this.ingredientList = ingredientList;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public ArrayList<String> getStepList() {
        return StepList;
    }


    public void setStepList(ArrayList<String> stepList) {
        StepList = stepList;
    }

    public ArrayList<String> getDietaryRec() {
        return dietaryRec;
    }

    public void setDietaryRec(ArrayList<String> dietaryRec) {
        this.dietaryRec = dietaryRec;
    }
}

