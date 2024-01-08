package Model;

import java.util.ArrayList;

public class User {
    String name,photo,email,id;
    ArrayList<String> favouriteRecipes;
    ArrayList<String> ownRecipes;
    ArrayList<Ingredient>shoppingList=new ArrayList<>();
    ArrayList<String>sharedRecipes=new ArrayList<>();




    public User(){}

    public User(String name, String photo,String id) {
        this.name = name.toLowerCase();
        this.photo = photo;
        this.id=id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId(){return id;}

    public void setID(String id){
        this.id=id;
    }
    public String getPhoto() {
        return photo;
    }
    public  void setPhoto(String url){
        this.photo=url;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<String> getFavouriteRecipes() {
        return favouriteRecipes;
    }

    public void setFavouriteRecipes(ArrayList<String> favouriteRecipes) {
        this.favouriteRecipes = favouriteRecipes;
    }

    public ArrayList<String> getOwnRecipes() {
        return ownRecipes;
    }

    public void setOwnRecipes(ArrayList<String> ownRecipes) {
        this.ownRecipes = ownRecipes;
    }

    public ArrayList<Ingredient> getShoppingList() {
        return shoppingList;
    }

    public void setShoppingList(ArrayList<Ingredient> shoppingList) {
        this.shoppingList = shoppingList;
    }

    public ArrayList<String> getSharedRecipes() {
        return sharedRecipes;
    }

    public void setSharedRecipes(ArrayList<String> sharedRecipes) {
        this.sharedRecipes = sharedRecipes;
    }
}
