module com.musicsheetsmanager {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.musicsheetsmanager to javafx.fxml;
    exports com.musicsheetsmanager;
    exports com.musicsheetsmanager.controller;
    opens com.musicsheetsmanager.controller to javafx.fxml;
}