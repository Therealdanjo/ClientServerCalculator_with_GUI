package it.bx.fallmerayer.tfo.Client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;

public class Controller {

    public RadioButton rb1, rb2, rb3, rb4;
    public ToggleGroup tg;
    public TextField inputField;
    public TextArea resArea;
    protected Socket s;

    public void initialize() {
        tg = new ToggleGroup();
        rb1.setToggleGroup(tg);
        rb2.setToggleGroup(tg);
        rb3.setToggleGroup(tg);
        rb4.setToggleGroup(tg);

        try {
            s = new Socket("localhost", 42069);
        } catch (IOException e) {
            e.printStackTrace();
        }

        logIn();

    }

    private void logIn(){
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Login");
        dialog.setHeaderText("Login to the Client-Server calculator");

        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(username::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(username.getText(), password.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(usernamePassword -> {
            try {
                writeMessage(s, usernamePassword.getKey() + ";" + usernamePassword.getValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        String checklogin = null;
        try {
            checklogin=readMessage(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(checklogin!=null && checklogin.equals("FAILED")){
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Login Failed");
            alert.setContentText("It seems like your credentials are wrong and/or expired, please try again!");

            alert.showAndWait();

            logIn();
        }
    }

    private static void writeMessage(Socket s, String nachricht) throws Exception {
        PrintWriter printWriter = new PrintWriter(s.getOutputStream(), true);
        printWriter.println(nachricht);
    }

    private static String readMessage(Socket s) throws Exception{
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        return bufferedReader.readLine();
    }

    public void logoutAndExit(ActionEvent actionEvent) {
        try {
            writeMessage(s, "logout");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Platform.exit();
    }

    public void send(ActionEvent actionEvent) {
        String toWrite=null;
        String lbl=inputField.getText();
        int nmbrs = countNumbers(lbl);
        if (tg.getSelectedToggle().equals(rb1)){
            toWrite="1;" + nmbrs + ";" +lbl;
            inputField.clear();
        } else if (tg.getSelectedToggle().equals(rb2)){
            toWrite="2;" + nmbrs + ";" +lbl;
            inputField.clear();
        } else if (tg.getSelectedToggle().equals(rb3)){
            toWrite="3;" + nmbrs + ";" +lbl;
            inputField.clear();
        } else if (tg.getSelectedToggle().equals(rb4)){
            toWrite="4;" + nmbrs + ";" +lbl;
            inputField.clear();
        }

        if(toWrite!=null){
            try {
                writeMessage(s, toWrite);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            String result = readMessage(s);
            resArea.setText(resArea.getText() + "\n" + result);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private int countNumbers(String input){
        String[] split = input.split(";");
        return split.length;
    }
}
