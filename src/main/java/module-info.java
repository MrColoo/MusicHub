module it.univr.musicsheetsmanager {
    requires javafx.controls;
    requires javafx.fxml;


    opens it.univr.musicsheetsmanager to javafx.fxml;
    exports it.univr.musicsheetsmanager;
}