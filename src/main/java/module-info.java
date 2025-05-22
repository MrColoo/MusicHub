module com.musicsheetsmanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.desktop;


    opens com.musicsheetsmanager to javafx.fxml;
    exports com.musicsheetsmanager;
    exports com.musicsheetsmanager.controller;
    exports com.musicsheetsmanager.model;
    opens com.musicsheetsmanager.controller to javafx.fxml;
    opens com.musicsheetsmanager.model to com.google.gson;
}