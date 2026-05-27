package com.buzybee.app.dao;

import com.buzybee.app.model.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private final String FILE_PATH = "data/users.json";
    private Gson gson = new Gson();

    public List<User> loadAllUsers() {
        File file = new File(FILE_PATH);
        if (!file.exists()){
            System.out.println("Error: data/users.json file is missing!");
            return new ArrayList<>();
        }

        try (FileReader reader = new FileReader(file)){
            Type listType = new TypeToken<ArrayList<User>>(){}.getType();
            return gson.fromJson(reader, listType);
        } catch (IOException e){
            System.out.println("Error reading users file: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Check if the typed username and password match our database
    public boolean authenticate(String username, String password){
        List<User> users = loadAllUsers();
        for (User user : users){
            if (user.getUsername().equalsIgnoreCase(username) && user.getPassword().equals(password)){
                return true; // match found
            }
        }
        return false; // Invalid username or password
    }

    // Register a new user and auto-create the file

}
