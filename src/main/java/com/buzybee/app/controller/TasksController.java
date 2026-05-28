package com.buzybee.app.controller;

import com.buzybee.app.config.SessionManager;
import com.buzybee.app.model.Task;
import com.buzybee.app.service.TaskService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TasksController {

    @FXML private TableView<Task> pendingTasksTable;
    @FXML private TableColumn<Task, String> colPendingName;
    @FXML private TableColumn<Task, LocalDate> colPendingDeadline;
    @FXML private TableColumn<Task, Double> colPendingTime;
    @FXML private TableColumn<Task, Double> colPendingDifficulty;
    @FXML private TableColumn<Task, Double> colPendingPriorityScore;

    @FXML private TableView<Task> activeTasksTable;
    @FXML private TableColumn<Task, String> colActiveName;
    @FXML private TableColumn<Task, LocalDate> colActiveDeadline;
    @FXML private TableColumn<Task, Double> colActiveTime;
    @FXML private TableColumn<Task, Double> colActiveDifficulty;
    @FXML private TableColumn<Task, Double> colActivePriorityScore;

    @FXML private TableView<Task> backlogTasksTable;
    @FXML private TableColumn<Task, String> colBacklogName;
    @FXML private TableColumn<Task, LocalDate> colBacklogDeadline;
    @FXML private TableColumn<Task, Double> colBacklogTime;
    @FXML private TableColumn<Task, Double> colBacklogDifficulty;
    @FXML private TableColumn<Task, Double> colBacklogPriorityScore;

    @FXML private ListView<Task> completedListView;

    @FXML private Label lblDetailName;
    @FXML private Label lblDetailStatus;
    @FXML private Label lblDetailDeadline;
    @FXML private Label lblDetailTime;
    @FXML private Label lblDetailDifficulty;
    @FXML private Label lblDetailPriorityScore;
    @FXML private Label lblDetailDaysRemaining;
    @FXML private Label lblDetailDateAdded;

    @FXML private Button btnAddNewTask;
    @FXML private Button btnMarkComplete;
    @FXML private Button btnEditTask;
    @FXML private Button btnDeleteTask;
    @FXML private Button btnRefreshTask;
    @FXML private Button btnRunAlgorithm;

    private TaskService taskService;

    private ObservableList<Task> pendingTasks = FXCollections.observableArrayList();
    private ObservableList<Task> activeTasks = FXCollections.observableArrayList();
    private ObservableList<Task> backlogTasks = FXCollections.observableArrayList();
    private ObservableList<Task> completedTasks = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        taskService = new TaskService();

        setupTableColumns(pendingTasksTable, colPendingName, colPendingDeadline, colPendingTime, colPendingDifficulty, colPendingPriorityScore);
        setupTableColumns(activeTasksTable, colActiveName, colActiveDeadline, colActiveTime, colActiveDifficulty, colActivePriorityScore);
        setupTableColumns(backlogTasksTable, colBacklogName, colBacklogDeadline, colBacklogTime, colBacklogDifficulty, colBacklogPriorityScore);

        completedListView.setCellFactory(param -> new ListCell<Task>() {
            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);
                if (empty || task == null) {
                    setText(null);
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
                    String addedDate = (task.getDateAdded() != null) ? task.getDateAdded().format(formatter) : "Unknown";
                    String compDate = (task.getDateCompleted() != null) ? task.getDateCompleted().format(formatter) : "Unknown";

                    setText(task.getName() + " \n(Added: " + addedDate + " | Completed: " + compDate + ")");
                }
            }
        });

        pendingTasksTable.setItems(pendingTasks);
        activeTasksTable.setItems(activeTasks);
        backlogTasksTable.setItems(backlogTasks);
        completedListView.setItems(completedTasks);

        setupSelectionListener(pendingTasksTable, activeTasksTable, backlogTasksTable, completedListView);
        setupSelectionListener(activeTasksTable, pendingTasksTable, backlogTasksTable, completedListView);
        setupSelectionListener(backlogTasksTable, pendingTasksTable, activeTasksTable, completedListView);

        completedListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                pendingTasksTable.getSelectionModel().clearSelection();
                activeTasksTable.getSelectionModel().clearSelection();
                backlogTasksTable.getSelectionModel().clearSelection();
                showTaskDetails(newVal);
            }
        });

        loadDataFromDatabase();
    }


    private void setupTableColumns(TableView<Task> table, TableColumn<Task, String> name, TableColumn<Task, LocalDate> deadline, TableColumn<Task, Double> time, TableColumn<Task, Double> difficulty, TableColumn<Task, Double> priority) {
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        deadline.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        time.setCellValueFactory(new PropertyValueFactory<>("estimatedTime"));
        difficulty.setCellValueFactory(new PropertyValueFactory<>("difficulty"));
        priority.setCellValueFactory(new PropertyValueFactory<>("priorityScore"));

        priority.setCellFactory(column -> new javafx.scene.control.TableCell<Task, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); }
                else { setText(String.format("%.2f", item)); }
            }
        });
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupSelectionListener(TableView<Task> primaryTable, TableView<Task> clear1, TableView<Task> clear2, ListView<Task> clear3) {
        primaryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                clear1.getSelectionModel().clearSelection();
                clear2.getSelectionModel().clearSelection();
                clear3.getSelectionModel().clearSelection();
                showTaskDetails(newVal);
            }
        });
    }

    private Task getSelectedTask() {
        if (pendingTasksTable.getSelectionModel().getSelectedItem() != null) return pendingTasksTable.getSelectionModel().getSelectedItem();
        if (activeTasksTable.getSelectionModel().getSelectedItem() != null) return activeTasksTable.getSelectionModel().getSelectedItem();
        if (backlogTasksTable.getSelectionModel().getSelectedItem() != null) return backlogTasksTable.getSelectionModel().getSelectedItem();
        if (completedListView.getSelectionModel().getSelectedItem() != null) return completedListView.getSelectionModel().getSelectedItem();
        return null;
    }



    private void showTaskDetails(Task task) {
        if (task != null) {
            if (lblDetailName != null) lblDetailName.setText(task.getName());
            if (lblDetailStatus != null) lblDetailStatus.setText(task.getStatus());
            if (lblDetailDateAdded != null) {
                lblDetailDateAdded.setText(task.getDateAdded() != null ? task.getDateAdded().toString() : "Unknown");
            }
            if (lblDetailDeadline != null) lblDetailDeadline.setText(task.getDeadline().toString());

            if (lblDetailDaysRemaining != null) lblDetailDaysRemaining.setText(task.getDaysRemaining() + " days");
            if (lblDetailTime != null) lblDetailTime.setText(task.getEstimatedTime() + " hrs");
            if (lblDetailDifficulty != null) lblDetailDifficulty.setText(String.format("%.1f", task.getDifficulty()));
            if (lblDetailPriorityScore != null) lblDetailPriorityScore.setText(String.format("%.2f", task.getPriorityScore()));
        } else {
            if (lblDetailName != null) lblDetailName.setText("---");
            if (lblDetailStatus != null) lblDetailStatus.setText("---");
            if (lblDetailDateAdded != null) lblDetailDateAdded.setText("---");
            if (lblDetailDeadline != null) lblDetailDeadline.setText("---");
            if (lblDetailDaysRemaining != null) lblDetailDaysRemaining.setText("---");
            if (lblDetailTime != null) lblDetailTime.setText("---");
            if (lblDetailDifficulty != null) lblDetailDifficulty.setText("---");
            if (lblDetailPriorityScore != null) lblDetailPriorityScore.setText("---");
        }

        if (task != null && "COMPLETED".equals(task.getStatus())) {
            btnMarkComplete.setText("Unmark Task");
        } else {
            btnMarkComplete.setText("Mark as Complete");
        }
    }

    private void loadDataFromDatabase() {
        pendingTasks.clear();
        activeTasks.clear();
        backlogTasks.clear();
        completedTasks.clear();

        if (taskService != null) {
            pendingTasks.addAll(taskService.getAllActiveTasks());
            activeTasks.addAll(taskService.getAllActiveTasks());
            backlogTasks.addAll(taskService.getBacklogTasks());
            completedTasks.addAll(SessionManager.getInstance().getTaskDao().getCompletedTasks());
        }
    }


    @FXML
    private void handleAddNewTask() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/buzybee/app/fxml/add-task-modal.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add New Task");
            stage.setScene(new Scene(root));
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
        Task selectedTask = getSelectedTask();

        if (selectedTask == null) {
            System.out.println("Please select a task to edit!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/buzybee/app/fxml/add-task-modal.fxml"));
            Parent root = loader.load();
            AddTaskController controller = loader.getController();
            controller.setTaskToEdit(selectedTask);

            Stage stage = new Stage();
            stage.setTitle("Edit Task");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadDataFromDatabase();
            showTaskDetails(selectedTask);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleDeleteTask() {
        Task selected = getSelectedTask();
        if (selected != null) {
            taskService.deleteTask(selected.getId());
            loadDataFromDatabase();
            System.out.println("Deleted: " + selected.getName());
        } else {
            System.out.println("Please select a task first.");
        }
    }

    @FXML
    private void handleMarkComplete() {
        Task selected = getSelectedTask();
        if (selected != null) {

            if ("COMPLETED".equals(selected.getStatus())) {
                taskService.unmarkTaskComplete(selected.getId());
                System.out.println("Unmarked: " + selected.getName());
            }
            else {
                taskService.markTaskComplete(selected.getId());
                System.out.println("Completed: " + selected.getName());
            }

            loadDataFromDatabase();

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
        taskService.prioritizeActiveTasks();
        loadDataFromDatabase();
        System.out.println("Algorithm Finished! Lists refreshed.");
    }
}