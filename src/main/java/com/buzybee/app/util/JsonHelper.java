package com.buzybee.app.util;

import com.buzybee.app.model.Task;
import com.buzybee.app.model.TimeBlock;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.*;
import java.time.LocalDate;
import java.time.LocalTime;


/**
 * Utility class responsible for serializing and deserializing Tasks to JSON.
 * Bridges the gap between the Java object model and the hard drive storage.
 */
public class JsonHelper {


    private static final String FILE_PATH = "buzybee_data.json";
    private static final String SCHEDULE_FILE = "schedule.json";

    // We use GsonBuilder with setPrettyPrinting so the JSON file is easily readable
    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            // Translator for LocalDate (Used in Task)
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, typeOfT, context) -> LocalDate.parse(json.getAsString()))
            // Translator for LocalTime (Used in TimeBlock)
            .registerTypeAdapter(LocalTime.class, (JsonSerializer<LocalTime>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalTime.class, (JsonDeserializer<LocalTime>) (json, typeOfT, context) -> LocalTime.parse(json.getAsString()))
            .create();
    // ==========================================
    // 1. SAVE TASKS (Java List -> JSON File)
    // ==========================================
    public void saveTasks(List<Task> tasks) {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(tasks, writer);
            System.out.println("Tasks successfully saved to JSON!");
        } catch (IOException e) {
            System.out.println("Error saving tasks: " + e.getMessage());
        }
    }

    // ==========================================
    // 2. LOAD TASKS (JSON File -> Java List)
    // ==========================================
    public List<Task> loadTasks() {
        File file = new File(FILE_PATH);

        // If the app is opening for the very first time, return an empty list
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (FileReader reader = new FileReader(file)) {
            // This weird Type line is just how Gson handles Lists
            Type listType = new TypeToken<ArrayList<Task>>(){}.getType();
            return gson.fromJson(reader, listType);
        } catch (IOException e) {
            System.out.println("Error loading tasks: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    // ==========================================
    // 3. SAVE TIMEBLOCK
    // ==========================================
    public void saveTimeBlocks(List<TimeBlock> blocks) {
        try (java.io.FileWriter writer = new java.io.FileWriter(SCHEDULE_FILE)) {
            gson.toJson(blocks, writer);
        } catch (java.io.IOException e) {
            System.out.println("Error saving schedule: " + e.getMessage());
        }
    }

    // ==========================================
    // 4. LOAD TIMEBLOCK
    // ==========================================
    public List<TimeBlock> loadTimeBlocks() {
        java.io.File file = new java.io.File(SCHEDULE_FILE);
        if (!file.exists()) return new java.util.ArrayList<>();

        try (java.io.FileReader reader = new java.io.FileReader(file)) {
            java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<java.util.ArrayList<TimeBlock>>(){}.getType();
            return gson.fromJson(reader, listType);
        } catch (java.io.IOException e) {
            return new java.util.ArrayList<>();
        }
    }

    /* // Test
    public static void main(String[] args) {
        JsonHelper testHelper = new JsonHelper();

        // 1. Create a fake list of tasks
        List<Task> myTasks = new ArrayList<>();
        Task task1 = new Task("Write Chapter 1", 4.0, 3.5, java.time.LocalDate.now().plusDays(2));
        Task task2 = new Task("Design App Logo", 2.0, 1.0, java.time.LocalDate.now().plusDays(5));

        myTasks.add(task1);
        myTasks.add(task2);

        System.out.println("--- ATTEMPTING TO SAVE ---");
        testHelper.saveTasks(myTasks);

        System.out.println("\n--- ATTEMPTING TO LOAD ---");
        List<Task> loadedTasks = testHelper.loadTasks();

        for (Task t : loadedTasks) {
            System.out.println("Successfully loaded: " + t.getName() + " (Status: " + t.getStatus() + ")");
        }
    }

     */
}