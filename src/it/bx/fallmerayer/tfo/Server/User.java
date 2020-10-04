package it.bx.fallmerayer.tfo.Server;

public class User{
    private String username;
    private String password;
    private boolean isLoggedin;

    public User(String username, String password, boolean isLoggedin) {
        this.username = username;
        this.password = password;
        this.isLoggedin = isLoggedin;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isLoggedin() {
        return isLoggedin;
    }

    public void setLoggedin(boolean loggedin) {
        isLoggedin = loggedin;
    }

    //Compares two users
    public boolean compareUsers(User user) {
        return this.username.equals(user.getUsername()) && this.username.equals(getPassword()) && this.isLoggedin == user.isLoggedin;
    }

}
