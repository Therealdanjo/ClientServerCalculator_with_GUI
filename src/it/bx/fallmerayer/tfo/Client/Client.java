package it.bx.fallmerayer.tfo.Client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;

public class Client extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("gui.fxml"));
        primaryStage.setTitle("Calculator");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
        //Sends Logout message if "Close" button is pressed
        primaryStage.setOnCloseRequest(event -> {
            PrintWriter printWriter = null;
            try {
                printWriter = new PrintWriter(Controller.s.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert printWriter != null;
            printWriter.println("logout");
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
