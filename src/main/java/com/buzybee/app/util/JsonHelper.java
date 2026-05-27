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

    private String taskFilePath;
    private String scheduleFilePath;

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
    // DYNAMIC CONSTRUCTOR
    // ==========================================
    public JsonHelper(String username) {
        // Automatically point to the new data folder and the user's specific files
        this.taskFilePath = "data/tasks_" + username + ".json";
        this.scheduleFilePath = "data/schedule_" + username + ".json";

        // Failsafe: Create the 'data' folder on the hard drive if it doesn't exist yet!
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
    }

    // ==========================================
    // 1. SAVE TASKS (Java List -> JSON File)
    // ==========================================
    public void saveTasks(List<Task> tasks) {
        try (FileWriter writer = new FileWriter(taskFilePath)) {
            gson.toJson(tasks, writer);
            System.out.println("Tasks successfully saved to: " + taskFilePath);
        } catch (IOException e) {
            System.out.println("Error saving tasks: " + e.getMessage());
        }
    }

    // ==========================================
    // 2. LOAD TASKS (JSON File -> Java List)
    // ==========================================
    public List<Task> loadTasks() {
        File file = new File(taskFilePath);

        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (FileReader reader = new FileReader(file)) {
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
        try (FileWriter writer = new FileWriter(scheduleFilePath)) {
            gson.toJson(blocks, writer);
        } catch (IOException e) {
            System.out.println("Error saving schedule: " + e.getMessage());
        }
    }

    // ==========================================
    // 4. LOAD TIMEBLOCK
    // ==========================================
    public List<TimeBlock> loadTimeBlocks() {
        File file = new File(scheduleFilePath);
        if (!file.exists()) return new ArrayList<>();

        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<TimeBlock>>(){}.getType();
            return gson.fromJson(reader, listType);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
}