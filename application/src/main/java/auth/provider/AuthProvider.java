package auth.provider;

/**
 * @author csieflyman
 */
public interface AuthProvider {

    String createAccount(String brandName, String adminName, String account, String password);

    void changePassword(String oldPassword, String newPassword);

    String login(String brandName, String account, String password);

    void logout();
}