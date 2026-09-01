package com.example.task_management_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TaskManagementApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskManagementApiApplication.class, args);
	}

}

//Чтобы сразу привыкать к разделению ответственности:
//HTTP request
//     ↓
//Controller
//«получает HTTP-запрос»
//    ↓
//Service
//«содержит бизнес-логику»
//    ↓
//Repository
//«работает с данными»
//    ↓
//Database
//«хранит данные»
