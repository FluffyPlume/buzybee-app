package com.buzybee.app.controller;

import com.buzybee.app.config.SessionManager;
import com.buzybee.app.model.Task;
import com.buzybee.app.service.TaskService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;

public class TasksController {

    @FXML private TableView<Task> pendingTasksTable;
    @FXML private TableColumn<Task, String> colName;
    @FXML private TableColumn<Task, LocalDate> colDeadline;
    @FXML private TableColumn<Task, Double> colTime;
    @FXML private TableColumn<Task, Double> colDifficulty;
    @FXML private TableColumn<Task, Double> colPriorityScore;

    @FXML private ListView<Task> backlogListView;
    @FXML private ListView<Task> completedListView;

    @FXML private Label lblDetailName;
    @FXML private Label lblDetailStatus;
    @FXML private Label lblDetailDeadline;
    @FXML private Label lblDetailTime;
    @FXML private Label lblDetailDifficulty;
    @FXML private Label lblDetailPriorityScore;
    @FXML private Label lblDetailDaysRemaining;

    @FXML private Button btnAddNewTask;
    @FXML private Button btnMarkComplete;
    @FXML private Button btnEditTask;
    @FXML private Button btnDeleteTask;
    @FXML private Button btnRefreshTask;
    @FXML private Button btnRunAlgorithm;

    private TaskService taskService;

    // Temporary list to hold our data until you build the JSON database saving
    private ObservableList<Task> pendingTasks = FXCollections.observableArrayList();
    private ObservableList<Task> backlogTasks = FXCollections.observableArrayList();
    private ObservableList<Task> completedTasks = FXCollections.observableArrayList();


    @FXML
    private void initialize() {

        taskService = new TaskService();
        //  Tell the Table Columns which variables to look for in Task.java
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDeadline.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("estimatedTime"));
        colDifficulty.setCellValueFactory(new PropertyValueFactory<>("difficulty"));
        colPriorityScore.setCellValueFactory(new PropertyValueFactory<>("priorityScore"));

        // Tell the ListViews to only display the Task's name
        setupListView(backlogListView);
        setupListView(completedListView);

        //  Connect the Lists to our Data
        pendingTasksTable.setItems(pendingTasks);
        backlogListView.setItems(backlogTasks);
        completedListView.setItems(completedTasks);

        colPriorityScore.setCellFactory(column -> new javafx.scene.control.TableCell<Task, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });

        // Listeners: When a user clicks a row, show the details
        pendingTasksTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showTaskDetails(newValue)
        );
        backlogListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showTaskDetails(newValue)
        );
        completedListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showTaskDetails(newValue)
        );

        pendingTasksTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        loadDataFromDatabase();

    }

    private void setupListView(ListView<Task> listView) {
        listView.setCellFactory(param -> new ListCell<Task>() {
            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);
                if (empty || task == null || task.getName() == null) {
                    setText(null);
                } else {
                    setText(task.getName()); // Only show the name in the small lists
                }
            }
        });
    }

    private void showTaskDetails(Task task) {
        if (task != null) {
            if (lblDetailName != null) lblDetailName.setText(task.getName());
            if (lblDetailStatus != null) lblDetailStatus.setText(task.getStatus());
            if (lblDetailDeadline != null) lblDetailDeadline.setText(task.getDeadline().toString());

            if (lblDetailDaysRemaining != null) lblDetailDaysRemaining.setText(task.getDaysRemaining() + " days");
            if (lblDetailTime != null) lblDetailTime.setText(task.getEstimatedTime() + " hrs");
            if (lblDetailDifficulty != null) lblDetailDifficulty.setText(String.format("%.1f", task.getDifficulty()));
            if (lblDetailPriorityScore != null) lblDetailPriorityScore.setText(String.format("%.2f", task.getPriorityScore()));
        } else {
            if (lblDetailName != null) lblDetailName.setText("---");
            if (lblDetailStatus != null) lblDetailStatus.setText("---");
            if (lblDetailDeadline != null) lblDetailDeadline.setText("---");

            // CLEAR WHEN DESELECTED
            if (lblDetailDaysRemaining != null) lblDetailDaysRemaining.setText("---");

            if (lblDetailTime != null) lblDetailTime.setText("---");
            if (lblDetailDifficulty != null) lblDetailDifficulty.setText("---");
            if (lblDetailPriorityScore != null) lblDetailPriorityScore.setText("---");
        }
    }

    private void loadDummyData() {
        pendingTasks.add(new Task("Write Essay", 6.0, 3.5, LocalDate.now().plusDays(2)));
        pendingTasks.add(new Task("Math Homework", 8.0, 2.0, LocalDate.now().plusDays(1)));

        Task backlogItem = new Task("Read Chapter 4", 3.0, 1.0, LocalDate.now().minusDays(1));
        backlogItem.setStatus("BACKLOG");
        backlogTasks.add(backlogItem);
    }

    private void loadDataFromDatabase() {
        // Clear the old data from the UI lists
        pendingTasks.clear();
        backlogTasks.clear();
        completedTasks.clear();

        // Fetch the fresh data from the JSON database via the Service
        if (taskService != null) {
            pendingTasks.addAll(taskService.getAllActiveTasks());
            backlogTasks.addAll(taskService.getBacklogTasks());


            completedTasks.addAll(SessionManager.getInstance().getTaskDao().getCompletedTasks());
        }
    }


    @FXML
    private void handleAddNewTask() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/buzybee/app/fxml/add-task-modal.fxml"));
            javafx.scene.Parent root = loader.load();


            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Add New Task");
            stage.setScene(new javafx.scene.Scene(root));
            stage.setResizable(false);


            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);


            stage.showAndWait();


            AddTaskController controller = loader.getController();
            Task newTask = controller.getCreatedTask();


            if (newTask != null) {
                taskService.createNewTask(newTask);

                loadDataFromDatabase();
            }

        } catch (Exception e) {
            System.out.println("Could not load the Add Task window!");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditTask() {
        Task selected = pendingTasksTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            System.out.println("Editing: " + selected.getName());
        } else {
            System.out.println("Please select a task first!");
        }
    }

    @FXML
    private void handleDeleteTask() {
        Task selected = pendingTasksTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            taskService.deleteTask(selected.getId());
            loadDataFromDatabase();
            System.out.println("Deleted: " + selected.getName());
        } else { System.out.println("Please select a task first."); }
    }

    @FXML
    private void handleMarkComplete() {
        Task selected = pendingTasksTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            taskService.markTaskComplete(selected.getId());
            loadDataFromDatabase();
            System.out.println("Completed: " + selected.getName());
        } else {
            System.out.println("Please select a task first!");
        }
    }

    @FXML
    private void handleRefresh() {
        loadDataFromDatabase();
        System.out.println("Data Refreshed from JSON.");
    }

    @FXML
    private void handleRunKnapsack() {
       // Run the algorithm and save the results (I'm using "DEFAULT" mode based on your code)
        taskService.prioritizeActiveTasks();
        // Refresh the UI to show the new Priority Scores!
        loadDataFromDatabase();
        System.out.println("Algorithm Finished! Lists refreshed.");
    }
}