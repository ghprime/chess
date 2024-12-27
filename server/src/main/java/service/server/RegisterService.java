package service.server;

import dataaccess.daointerface.AuthDAO;
import dataaccess.exception.AlreadyTakenException;
import dataaccess.exception.DataAccessException;
import dataaccess.daointerface.UserDAO;
import models.AuthData;
import models.UserData;
import org.mindrot.jbcrypt.BCrypt;

public class RegisterService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public RegisterService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO=userDAO;
        this.authDAO = authDAO;
    }

    /**
     * Registers a user
     *
     * @param userData User to register
     * @return AuthData associated with new user
     */
    public AuthData register(UserData userData) throws DataAccessException {
        if (userDAO.getUser(userData.username()) != null) {
            throw new AlreadyTakenException("Username already taken!");
        }

        String newPs = hashPassword(userData.password());

        UserData userToInsert = new UserData(userData.username(), newPs, userData.email());

        userDAO.insertUser(userToInsert);
        AuthData authData = new AuthData(userToInsert.username());
        authDAO.insertAuthData(authData);

        return authData;
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}
