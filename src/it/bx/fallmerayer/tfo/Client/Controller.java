package it.bx.fallmerayer.tfo.Client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

public class Controller {

    public RadioButton rb1, rb2, rb3, rb4;
    public ToggleGroup tg;
    public TextField inputField;
    public TextArea resArea;
    public Hyperlink help, about, code;
    public Button exitbtn;
    public Label loginStatus;

    protected static Socket s;
    private String connecteduser;

    //method executed when starting
    public void initialize() {
        //Sets the radio buttons in a toggle group to facilitate the selection
        tg = new ToggleGroup();
        rb1.setToggleGroup(tg);
        rb2.setToggleGroup(tg);
        rb3.setToggleGroup(tg);
        rb4.setToggleGroup(tg);

        //establishes a connection to the server, if it isn't possible, an error dialog is shown and the program exits automatically
        try {
            s = new Socket("localhost", 42069);
        } catch (IOException e) {
            Alert connectionerr = new Alert(AlertType.ERROR);
            connectionerr.setTitle("Error!");
            connectionerr.setHeaderText("Connection Error");
            connectionerr.setContentText("There was a problem reaching the server, make sure your connection is ok and try again!");

            connectionerr.showAndWait();
            System.exit(0);
        }

        //after a successful connection with the server, the login process is started
        logIn();

        //If the admin is connected, shutdown is shown instead of logout in the button
        if (connecteduser.equals("admin")){
            exitbtn.setText("Shut down");
        }
    }

    //logs the user in to the server via a custom login dialog
    private void logIn(){
        //Sets up the login dialog
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

        //Saves username and password as pair when the login button is pressed
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(username.getText(), password.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        final String[] user = new String[1];
        //Writes username and password to the server
        result.ifPresent(usernamePassword -> {
            user[0] = usernamePassword.getKey();
            try {
                writeMessage(s, usernamePassword.getKey() + ";" + usernamePassword.getValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        //Checks if the login process was successful or not
        String checklogin = null;
        try {
            checklogin=readMessage(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(checklogin!=null && checklogin.equals("FAILED")){        //if the login failed an error is shown and the login process starts over
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Login Failed");
            alert.setContentText("It seems like your credentials are wrong and/or expired, please try again!");

            alert.showAndWait();

            logIn();
        } else if (checklogin != null && checklogin.equals("ACONNECTED")){          //If the user is already connected, an error is shown and the login process starts over
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Login Failed");
            alert.setContentText("This user is already logged in, try with another one!");

            alert.showAndWait();

            logIn();
        } else if (checklogin != null && checklogin.equals("OK")){          //If the login process was succesful, the program continues and the username that is logged in is displayed on the GUI
            connecteduser = username.getText();
            loginStatus.setText("You are logged in as: " + connecteduser);
        }
    }

    //writes to the server
    private static void writeMessage(Socket s, String message) throws Exception {
        PrintWriter printWriter = new PrintWriter(s.getOutputStream(), true);
        printWriter.println(message);
    }

    //reads from the server
    private static String readMessage(Socket s) throws Exception{
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        return bufferedReader.readLine();
    }

    //if the logout button is pressed, the user gets logged out from the server and the program stops. If the user is the admin, the server is also shut down.
    public void logoutAndExit(ActionEvent actionEvent) {
        if (connecteduser.equals("admin")){
            try {
                writeMessage(s, "SHUTDOWN");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                writeMessage(s, "LOGOUT");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Platform.exit();
    }

    //sends and gets the calculation
    public void send(ActionEvent actionEvent) {
        //Send calculation
        String toWrite=null;
        String lbl=inputField.getText();
        if(lbl.equals("")){             //if the input field is empty, an error is shown
            Alert noinput = new Alert(AlertType.ERROR);
            noinput.setTitle("Error");
            noinput.setHeaderText("Input Error");
            noinput.setContentText("Please input your numbers in the following format: 1;2;3;...;n");
            noinput.showAndWait();
        } else {        //otherwise the operation is checked and the string is sent to the server
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
            //gets the result from the server and prints it in the textArea
            try {
                String result = readMessage(s);
                resArea.setText(resArea.getText() + result + "\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    //counts the amount of numbers given in a string with the format x1;x2;...;xn
    private int countNumbers(String input){
        String[] split = input.split(";");
        return split.length;
    }

    //Controls the actions for the links at the bottom
    public void linkController(ActionEvent actionEvent) {
        if(actionEvent.getSource().equals(code)){
            if(Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URI("https://github.com/Therealdanjo/ClientServerCalculator_with_GUI"));
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        } else if (actionEvent.getSource().equals(about)){
            Alert about = new Alert(AlertType.NONE);
            about.setTitle("About");
            about.setHeaderText("Coded and designed by Daniele Guidotti");
            about.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
            about.show();
        } else if(actionEvent.getSource().equals(help)){
            Alert help = new Alert(AlertType.INFORMATION);
            help.setTitle("Help");
            help.setHeaderText("Help");
            help.setContentText("1) Choose an operation on the left side of the screen\n2) input your desired numbers separating them with a semicolon (ex. 1;2;3;4;n)\n3) Click on 'Send'\nThe result will now appear in the dedicated section\nWhen you finish using the program, simply click the 'logout' button. The program will disconnect from the server and close itself automatically\n\nEnjoy!");
            help.showAndWait();
        }
    }

}