package com.musicsheetsmanager.view.components;

import com.musicsheetsmanager.model.User;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class UserListRow extends HBox {
    private User user;
    private Runnable onDeleteAction;
    private Runnable onMakeAdminAction;

    public UserListRow(User user, Runnable onDeleteAction, Runnable onMakeAdminAction) {
        this.user = user;
        this.onDeleteAction = onDeleteAction;
        this.onMakeAdminAction = onMakeAdminAction;
        initialize();
    }

    private void initialize() {
        this.setSpacing(10);
        this.setPadding(new Insets(5));
        this.setStyle("-fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;");

        // Username label
        Label usernameLabel = new Label(user.getUsername());
        usernameLabel.setPrefWidth(150);

        // Spacer to push buttons to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Make Admin button
        Button makeAdminButton = new Button("Make Admin");
        makeAdminButton.setOnAction(e -> onMakeAdminAction.run());

        // Delete button
        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("button-secondary");
        deleteButton.setOnAction(e -> onDeleteAction.run());

        this.getChildren().addAll(usernameLabel, spacer, makeAdminButton, deleteButton);
    }
}