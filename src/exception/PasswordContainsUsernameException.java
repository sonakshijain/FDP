package exception;

public class PasswordContainsUsernameException extends Exception {

    public PasswordContainsUsernameException() {
        super("Password can't contain the username/name.");
    }
}
