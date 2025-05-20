module com.musicsheetsmanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;


    opens com.musicsheetsmanager to javafx.fxml;
    exports com.musicsheetsmanager;
    exports com.musicsheetsmanager.controller;
    opens com.musicsheetsmanager.controller to javafx.fxml;
}