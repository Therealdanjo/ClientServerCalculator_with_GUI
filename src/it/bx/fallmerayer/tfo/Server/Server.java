package it.bx.fallmerayer.tfo.Server;

import java.io.*;
import java.util.*;
import java.net.*;

public class Server {

    // Vector to store active clients
    static Vector<ClientHandler> ar = new Vector<>();           //Gleich wie eine synchronisierte Arraylist

    // counter for clients
    static int i = 0;

    public static void main(String[] args) throws IOException
    {
        // server is listening on port 42069
        ServerSocket ss = new ServerSocket(42069);

        Socket s;

        // running loop for getting client requests, breaks up after 10 clients
        while (i<=10)
        {
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

}

// ClientHandler class
class ClientHandler implements Runnable
{
    private String name;
    private Socket s;
    boolean isloggedin;

    // constructor
    public ClientHandler(Socket s, String name) {
        this.name = name;
        this.s = s;
        this.isloggedin=true;
    }

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

    @Override
    public void run() {
        String[] data = null;
        while(true) {
            try {
                String login = readMessage(s);
                data = login.split(";");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (data != null && data[0].equals("admin") && data[1].equals("admin")) {
                try {
                    writeMessage(s, "OK");
                    System.out.println("Client "+ Server.i +" logged in successfully!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            } else {
                try {
                    writeMessage(s, "FAILED");
                    System.out.println("Client "+ Server.i +" login failed!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        String received;
        //listens to messages from client
        while (true) {
            try {
                // receive the string
                received = readMessage(s);


                //print the received string on the server console
                System.out.println("From: " + name + ": " + received);

                //if logout received, close connection and set isloggedin to false
                if(received.equals("logout")){
                    this.isloggedin=false;
                    this.s.close();
                    break;
                }if(received.equals("help")){
                    writeMessage(s, "Choose an option from the menu, type the respective number in the console and press enter. You then will be prompted to input your numbers. Press enter after very number to confirm your input. Press q to exit the number input. Your selected operation will now be processed automatically");
                    break;
                }

                writeMessage(s, calculate(received));

            } catch (Exception e) {

                e.printStackTrace();
            }

        }
    }

    public static String readMessage(Socket s) throws Exception{
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        return bufferedReader.readLine();
    }

    public static void writeMessage(Socket s, String message) throws Exception {
        PrintWriter printWriter = new PrintWriter(s.getOutputStream());
        printWriter.println(message);
        printWriter.flush();
    }

}