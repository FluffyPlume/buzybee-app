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
    public User authenticate(String identifier, String password){
        List<User> users = loadAllUsers();
        for (User user : users){
            // Check if textfield matches either Username OR Email
            boolean matchesUsername = user.getUsername().equalsIgnoreCase(identifier);
            boolean matchesEmail = user.getEmail() != null && user.getEmail().equalsIgnoreCase(identifier);

            if ((matchesUsername || matchesEmail) && user.getPassword().equals(password)){
                System.out.println("Login Succesful for: " + user.getUsername());
                return user; // Return the full user profile
            }
        }
        System.out.println("Login Failed: Invalid Credentials.");
        return null; // Return null if no match is found
    }

    // Register a new user and auto-create the file
    public boolean registerUser(String username, String password, String email) {
        List<User> users = loadAllUsers();

        for (User u : users){
            if (u.getUsername().equalsIgnoreCase(username)){
                System.out.println("Registration failed: Username '" + username + " is already taken.");
                return false;
            }
        }

        users.add(new User(username, password, email));

        File dataDir = new File("data");
        if (!dataDir.exists()){
            dataDir.mkdirs();
        }
        // Save the updated list back to users.json
        // If users.json doesn't exist, FileWriter creates it
        try (java.io.FileWriter writer = new java.io.FileWriter(FILE_PATH)) {
            gson.toJson(users, writer);
            System.out.println("Successfully registered new user: " + username);
            return true;
        } catch (IOException e) {
            System.out.println("Error saving new user: " + e.getMessage());
            return false;
        }
    }
}
