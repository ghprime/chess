package service;

import dataaccess.daointerface.AuthDAO;
import dataaccess.exception.DataAccessException;
import dataaccess.exception.UnauthorizedException;
import models.AuthData;

public class ServiceUtils {
    private static AuthDAO authDAO = null;

    public static void setAuthDAO(AuthDAO authDAO) {
        ServiceUtils.authDAO = authDAO;
    }

    public static AuthData validateAuthToken(AuthData authData) throws DataAccessException {
        if (ServiceUtils.authDAO == null) {
            throw new DataAccessException("Service not initialized!");
        }

        AuthData returnedAuthData = authDAO.getAuthData(authData.authToken());

        if (returnedAuthData == null) {
            throw new UnauthorizedException("Unauthorized!");
        }

        return returnedAuthData;
    }
}
