package auth;

/**
 * @author csieflyman
 */
public interface AuthProvider {

    String createAccount(String account, String password);

    void changePassword(String oldPassword, String newPassword);

    String login(String account, String password);

    void logout();
}