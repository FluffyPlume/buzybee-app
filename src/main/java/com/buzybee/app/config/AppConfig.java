package com.buzybee.app.config;

import com.buzybee.app.dao.TaskDAO;
import com.buzybee.app.service.TaskService;

public class AppConfig {
    // create these ONCE and share them everywhere
    public static final TaskDAO taskDAO = new TaskDAO();
    public static final TaskService taskService = new TaskService(taskDAO);
}