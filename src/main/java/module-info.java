module com.musicsheetsmanager {
    requires javafx.fxml;
    requires com.google.gson;
    requires javafx.web;
    requires java.desktop;
    requires javafx.graphics;


    opens com.musicsheetsmanager to javafx.fxml;
    exports com.musicsheetsmanager;
    exports com.musicsheetsmanager.controller;
    exports com.musicsheetsmanager.model;
    opens com.musicsheetsmanager.controller to javafx.fxml;
    opens com.musicsheetsmanager.model to com.google.gson;
}