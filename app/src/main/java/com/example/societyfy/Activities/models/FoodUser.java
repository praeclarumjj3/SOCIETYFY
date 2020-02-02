package com.example.societyfy.Activities.models;

public class FoodUser {


    private String email;
    private String image;
    private String name;
    private String user_id;


    public FoodUser(String email, String image, String name, String user_id) {
        this.email = email;
        this.image = image;
        this.name = name;
        this.user_id = user_id;
    }


    public FoodUser() {

    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
