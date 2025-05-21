module com.musicsheetsmanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;


    opens com.musicsheetsmanager to javafx.fxml;
    exports com.musicsheetsmanager;
    exports com.musicsheetsmanager.controller;
    opens com.musicsheetsmanager.controller to javafx.fxml;

    opens com.musicsheetsmanager.model to com.google.gson;
}