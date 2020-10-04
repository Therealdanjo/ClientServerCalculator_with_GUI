package it.bx.fallmerayer.tfo.Server;

import javafx.application.Platform;

import java.io.*;
import java.util.*;
import java.net.*;

public class Server {

    // Vector to store active clients
    static Vector<ClientHandler> ar = new Vector<>();       //Equals to a synchronized ArrayList

    // counter for clients
    static int i = 0;

    protected static ArrayList<User> userslist;
    public static void main(String[] args) throws IOException {
        //Reads the users from users.csv and saves them to userslist
        importUsers();
        // server is listening on port 42069
        ServerSocket ss = new ServerSocket(42069);

        Socket s;

        // running loop for getting client requests
        while (true) {
            // Accept the incoming request
            s = ss.accept();

            System.out.println("Received new Client request: " + s);

            System.out.println("Creating a new handler for this client...");

            // Create a new handler object for handling this request.
            ClientHandler mtch = new ClientHandler(s,"client " + i);

            // Create a new Thread with this object.
            Thread t = new Thread(mtch);

            // add this client to active clients list
            ar.add(mtch);
            System.out.println("Client added to active clients list");

            // start the thread.
            t.start();

            // increment i for new client
            i++;

        }
    }
    //Reads the users from the users.csv file and imports them into the user list
    protected static void importUsers() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Daniele\\Google Drive\\TP\\Uebungen\\01_ClientServer_GUI\\src\\it\\bx\\fallmerayer\\tfo\\Server\\users.csv"));
        String line;
        userslist = new ArrayList<>();
        while ((line = br.readLine()) != null){
            String[] values = line.split(";");
            userslist.add(new User(values[0], values[1], false));
        }
    }

}

// ClientHandler class
class ClientHandler implements Runnable {
    private final String name;
    private final Socket s;
    boolean isloggedin;
    private User activeUser;

    public ClientHandler(Socket s, String name) {
        this.name = name;
        this.s = s;
        this.isloggedin=false;
    }

    //Splits the received string and selects the operation to perform, then passes the numbers on to the respective methods
    private String calculate(String message) throws Exception {
        String[] parser = message.split(";");
        int mode = Integer.parseInt(parser[0]);
        int numbersCount = Integer.parseInt(parser[1]);
        long[] numbers = new long[numbersCount];
        int j=0;
        for (int i = 2; i < parser.length; i++) {
            numbers[j] = Integer.parseInt(parser[i]);
            j++;
        }
        System.out.println("Received Numbers: " + Arrays.toString(numbers));
        String res = null;
        switch (mode){
            case 1: res = add(numbers); break;
            case 2: res = subtract(numbers); break;
            case 3: res = multiplicate(numbers); break;
            case 4: res = primeNumbers(numbers);break;
        }

        return res;
    }

    //adds all numbers and returns a string in the following format: x1+x2+...+xn=result
    private String add(long[] numbers){
        long res = 0;
        StringBuilder tmpstr = new StringBuilder();
        for (int i = 0; i< numbers.length; i++) {
            res +=numbers[i];
            if(i== numbers.length-1) {
                tmpstr.append(numbers[i]);
                break;
            }
            tmpstr.append(numbers[i]);
            tmpstr.append("+");
        }
        String ms = tmpstr.toString();
        ms += "=" + res;
        return ms;
    }

    //subtracts all numbers and returns a string in the following format: x1-x2-...-xn=result
    private String subtract(long[] numbers){
        long res = 0;
        StringBuilder tmpstr = new StringBuilder();
        for (int i = 0; i< numbers.length; i++) {
            res -=numbers[i];
            if(i== numbers.length-1) {
                tmpstr.append(numbers[i]);
                break;
            }
            tmpstr.append(numbers[i]);
            tmpstr.append("-");
        }
        String ms = tmpstr.toString();
        ms += "=" + res;
        return ms;
    }

    //multiplicates all numbers and returns a string in the following format: x1*x2*...*xn=result
    private String multiplicate(long[] numbers){
        long res = 1;
        StringBuilder tmpstr = new StringBuilder();
        for (int i = 0; i< numbers.length; i++) {
            res *=numbers[i];
            if(i== numbers.length-1) {
                tmpstr.append(numbers[i]);
                break;
            }
            tmpstr.append(numbers[i]);
            tmpstr.append("*");
        }
        String ms = tmpstr.toString();
        ms += "=" + res;
        return ms;
    }
    //Checks if the numbers are prime and returns a string in the following format: x1:tf x2:tf ... -> tf=true/false
    private String primeNumbers(long[] numbers) throws Exception {
        PoolManager manager = new PoolManager(numbers);
        Boolean[] result = manager.calc();
        StringBuilder tmpstr = new StringBuilder();
        for (int i = 0; i < result.length; i++) {
            tmpstr.append(numbers[i]);
            tmpstr.append(":");
            tmpstr.append(result[i]);
            tmpstr.append(" ");
        }
        return tmpstr.toString();
    }

    //checks if the user exists. If it exists and it's not logged in writes OK to the client, otherwise it sends the respective errors
    private void logIn() {
        String[] data = null;

        try {
            String login = readMessage(s);
            data = login.split(";");
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert data != null;
        for (User u: Server.userslist) {
            if(data[0].equals(u.getUsername()) && data[1].equals(u.getPassword()) && !u.isLoggedin()){
                try {
                    u.setLoggedin(true);
                    writeMessage(s, "OK");
                    System.out.println(name + " logged in successfully!");
                    activeUser=new User(u.getUsername(), u.getPassword(), true);
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (data[0].equals(u.getUsername()) && data[1].equals(u.getPassword()) && u.isLoggedin()){
                try {
                    writeMessage(s, "ACONNECTED");
                    System.out.println(name + " is already logged in!");
                    logIn();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            writeMessage(s, "FAILED");
            System.out.println(name + " login failed!");
            logIn();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Handles all the operations for a client
    @Override
    public void run() {
        //Starts the login process
        logIn();

        String received;
        //listens to messages from client
        label:
        while (true) {
            try {
                // receive the string
                received = readMessage(s);

                //print the received string on the server console
                System.out.println("From: " + name + ": " + received);

                //if logout received, close connection and set isloggedin to false, if shutdown, the server is closed
                switch (received) {
                    case "LOGOUT":
                        this.isloggedin = false;
                        int userIndex = searchElement(Server.userslist, activeUser);
                        if (userIndex > 0) {
                            Server.userslist.get(userIndex).setLoggedin(false);
                        }
                        this.s.close();
                        break label;
                    case "help":
                        writeMessage(s, "Choose an option from the menu, type the respective number in the console and press enter. You then will be prompted to input your numbers. Press enter after very number to confirm your input. Press q to exit the number input. Your selected operation will now be processed automatically");
                        break label;
                    case "SHUTDOWN":
                        this.s.close();
                        System.exit(0);
                }

                //Calculate and write back to the client
                writeMessage(s, calculate(received));

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    //Reads from the client
    public static String readMessage(Socket s) throws Exception{
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        return bufferedReader.readLine();
    }

    //writes to the client
    public static void writeMessage(Socket s, String message) throws Exception {
        PrintWriter printWriter = new PrintWriter(s.getOutputStream());
        printWriter.println(message);
        printWriter.flush();
    }

    //Searches an user in a list and returns its index
    private int searchElement(ArrayList<User> list, User user){
        for (int i = 0; i < list.size(); i++) {
            if(user.compareUsers(list.get(i))){
                return i;
            }
        }
        return -1;
    }
}