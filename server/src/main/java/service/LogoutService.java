package service;

import dataaccess.daointerface.AuthDAO;
import dataaccess.exception.DataAccessException;
import models.AuthData;

public class LogoutService {
    private final AuthDAO authDAO;

    public LogoutService(AuthDAO authDAO) {
        this.authDAO = authDAO;
    }

    public void logout(AuthData authData) throws DataAccessException {
        ServiceUtils.verifyAuthData(authData);

        authDAO.deleteAuthData(authData.authToken());
    }
}
