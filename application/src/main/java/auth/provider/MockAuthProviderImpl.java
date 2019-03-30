package auth.provider;

/**
 * @author csieflyman
 */
public class MockAuthProviderImpl implements AuthProvider {
    @Override
    public String createAccount(String brandName, String adminName, String account, String password) {
        return "MockTester";
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {

    }

    @Override
    public String login(String brandName, String account, String password) {
        return "MockTester";
    }

    @Override
    public void logout() {

    }
}
