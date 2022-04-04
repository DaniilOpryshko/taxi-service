package taxi.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import taxi.exception.AuthenticationException;
import taxi.lib.Inject;
import taxi.lib.Service;
import taxi.model.Driver;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private static final Logger logger = LogManager.getLogger(AuthenticationServiceImpl.class);
    @Inject
    private DriverService driverService;

    @Override
    public Driver login(String login, String password) throws AuthenticationException {
        logger.info("Method login was called. Login = {}", login);
        Driver driver = driverService.getByLogin(login);
        if (driver != null && driver.getPassword().equals(password)) {
            logger.info("Driver with login {} successfully logged in.", login);
            return driver;
        }
        logger.error("Driver with login {} failed to log in.", login);
        throw new AuthenticationException("User or password was incorrect");
    }
}
