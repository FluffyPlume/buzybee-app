package com.buzybee.app.config;

import com.buzybee.app.model.User;
import com.buzybee.app.dao.TaskDAO;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private TaskDAO userTaskDAO;

    private SessionManager() {}

    public static SessionManager getInstance(){
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // Called upon succesful login
    public void startSession(User user){
        this.currentUser = user;
        this.userTaskDAO = new TaskDAO(user.getUsername());
        System.out.println("Session started for: " + user.getUsername());
    }
    // Gets the profile data of the currently logged-in user
    // example, for displaying "Welcome, [Username]"
    public User getCurrentUser(){
        return currentUser;
    }

    // Call this to read/write data
    public TaskDAO getTaskDao(){
        return userTaskDAO;
    }

    // Clears the current runtime state data
    // Used for when a user clicks logout button
    public void endSession(){
        System.out.println("Session destroyed for user: " + (currentUser != null ? currentUser.getUsername() : "None"));
        this.currentUser = null;
        this.userTaskDAO = null;
    }
}
