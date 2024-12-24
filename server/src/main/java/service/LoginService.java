package service;

import dataaccess.daointerface.AuthDAO;
import dataaccess.exception.DataAccessException;
import dataaccess.exception.UnauthorizedException;
import dataaccess.daointerface.UserDAO;
import models.AuthData;
import models.UserData;
import org.mindrot.jbcrypt.BCrypt;

public class LoginService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public LoginService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO=userDAO;
        this.authDAO = authDAO;
    }

    /**
     * Registers a user
     *
     * @param userData User to register
     * @return AuthData associated with new user
     */
    public AuthData login(UserData userData) throws DataAccessException {
        UserData returnedUser = userDAO.getUser(userData.username());

        if (returnedUser == null || !checkPw(userData.password(), returnedUser.password())) {
            throw new UnauthorizedException("Unauthorized!");
        }

        AuthData authData = new AuthData(userData.username());
        authDAO.insertAuthData(authData);

        return authData;
    }

    private boolean checkPw(String pw, String hashedPw) {
        return BCrypt.checkpw(pw, hashedPw);
    }
}
