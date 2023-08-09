package Viewmodel;

import java.util.Comparator;

import Model.Recipe;

public class CustomComparator implements Comparator<Recipe> {
    @Override
    public int compare(Recipe o1, Recipe o2) {
        return o1.getRecipeName().compareTo(o2.getRecipeName());
    }
}
