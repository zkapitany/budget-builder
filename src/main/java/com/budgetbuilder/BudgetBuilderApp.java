package com.budgetbuilder;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class BudgetBuilderApp extends Application {
    private static final Logger logger = LoggerFactory.getLogger(BudgetBuilderApp.class);

    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Scene scene = new Scene(loader.load(), 1400, 800);
            
            String css = getClass().getResource("/css/styles.css").toExternalForm();
            scene.getStylesheets().add(css);
            
            primaryStage.setTitle("Budget Builder - Költségvetés Builder");
            primaryStage.setScene(scene);
            primaryStage.setWidth(1400);
            primaryStage.setHeight(800);
            primaryStage.show();
            
            logger.info("Budget Builder alkalmazás indult");
        } catch (Exception e) {
            logger.error("Hiba az alkalmazás indításakor", e);
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}