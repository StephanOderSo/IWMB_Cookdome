package com.bienhuels.iwmb_cookdome.Model;

public class Ingredient {
    private double amount;
    private String unit;
    private String ingredientName;
    public Ingredient(){}

    public Ingredient(double amount, String unit, String ingredientName) {
        this.amount = amount;
        this.unit = unit;
        this.ingredientName = ingredientName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }
}
