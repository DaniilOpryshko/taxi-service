package taxi.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import taxi.exception.DataProcessingException;
import taxi.lib.Dao;
import taxi.model.Driver;
import taxi.util.ConnectionUtil;

@Dao
public class DriverDaoImpl implements DriverDao {
    private static final Logger logger = LogManager.getLogger(DriverDaoImpl.class);

    @Override
    public Driver create(Driver driver) {
        logger.info("Attempting to add driver to DB. "
                        + "Params: driver name = {}, driver license number = {}",
                        driver.getName(), driver.getLicenseNumber());
        String query = "INSERT INTO drivers (name, license_number, login, password) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement createDriverStatement = connection.prepareStatement(query,
                        Statement.RETURN_GENERATED_KEYS)) {
            createDriverStatement.setString(1, driver.getName());
            createDriverStatement.setString(2, driver.getLicenseNumber());
            createDriverStatement.setString(3, driver.getLogin());
            createDriverStatement.setString(4, driver.getPassword());
            createDriverStatement.executeUpdate();
            ResultSet resultSet = createDriverStatement.getGeneratedKeys();
            if (resultSet.next()) {
                driver.setId(resultSet.getObject(1, Long.class));
            }
            logger.info("Successfully added driver to DB. "
                            + "Params: driver name = {}, driver license number = {}",
                            driver.getName(), driver.getLicenseNumber());
            return driver;
        } catch (SQLException e) {
            logger.error("Failed to add driver to DB. "
                            + "Params: driver name = {}, driver license number = {}",
                            driver.getName(), driver.getLicenseNumber());
            throw new DataProcessingException("Couldn't create "
                    + driver + ". ", e);
        }
    }

    @Override
    public Optional<Driver> get(Long id) {
        logger.info("Attempting to fetch driver from DB. Params: driver id = {}", id);
        String query = "SELECT * FROM drivers WHERE id = ? AND is_deleted = FALSE";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement getDriverStatement = connection.prepareStatement(query)) {
            getDriverStatement.setLong(1, id);
            ResultSet resultSet = getDriverStatement.executeQuery();
            Driver driver = null;
            if (resultSet.next()) {
                driver = parseDriverFromResultSet(resultSet);
            }
            logger.info("Successfully fetched driver from DB. Params: driver id = {}", id);
            return Optional.ofNullable(driver);
        } catch (SQLException e) {
            logger.error("Failed to fetch driver from DB. Params: driver id = {}", id);
            throw new DataProcessingException("Couldn't get driver by id " + id, e);
        }
    }

    @Override
    public List<Driver> getAll() {
        logger.info("Attempting to fetch all drivers from DB.");
        String query = "SELECT * FROM drivers WHERE is_deleted = FALSE";
        List<Driver> drivers = new ArrayList<>();
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement getAllDriversStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = getAllDriversStatement.executeQuery();
            while (resultSet.next()) {
                drivers.add(parseDriverFromResultSet(resultSet));
            }
            logger.info("Successfully fetched all drivers from DB.");
            return drivers;
        } catch (SQLException e) {
            logger.error("Failed to fetch all drivers from DB.");
            throw new DataProcessingException("Couldn't get a list of drivers from driversDB.",
                    e);
        }
    }

    @Override
    public Driver update(Driver driver) {
        logger.info("Attempting to update driver in DB. Params: driver id = {}", driver.getId());
        String query = "UPDATE drivers "
                + "SET name = ?, license_number = ?, login = ?, password = ? "
                + "WHERE id = ? AND is_deleted = FALSE";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement updateDriverStatement
                        = connection.prepareStatement(query)) {
            updateDriverStatement.setString(1, driver.getName());
            updateDriverStatement.setString(2, driver.getLicenseNumber());
            updateDriverStatement.setString(3, driver.getLogin());
            updateDriverStatement.setString(4, driver.getPassword());
            updateDriverStatement.setLong(5, driver.getId());
            updateDriverStatement.executeUpdate();
            logger.info("Successfully updated driver in DB. Params: driver id = {}",
                    driver.getId());
            return driver;
        } catch (SQLException e) {
            logger.error("Failed to update driver in DB. Params: driver id = {}", driver.getId());
            throw new DataProcessingException("Couldn't update "
                    + driver + " in driversDB.", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        logger.info("Attempting to delete driver from DB. Params: driver id = {}", id);
        String query = "UPDATE drivers SET is_deleted = TRUE WHERE id = ?";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement deleteDriverStatement = connection.prepareStatement(query)) {
            deleteDriverStatement.setLong(1, id);
            logger.info("Successfully deleted driver from DB. Params: driver id = {}", id);
            return deleteDriverStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to delete driver from DB. Params: driver id = {}", id);
            throw new DataProcessingException("Couldn't delete driver with id " + id, e);
        }
    }

    @Override
    public Optional<Driver> getByLogin(String login) {
        logger.info("Attempting to get driver by login. Params: login = {}", login);
        String query = "SELECT * FROM drivers "
                + "WHERE login = ? AND is_deleted = FALSE";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement getByLoginStatement
                        = connection.prepareStatement(query)) {
            getByLoginStatement.setString(1, login);
            ResultSet resultSet = getByLoginStatement.executeQuery();
            Driver driver = null;
            if (resultSet.next()) {
                driver = parseDriverFromResultSet(resultSet);
            }
            logger.info("Successfully fetched driver by login. Params: login = {}", login);
            return Optional.ofNullable(driver);
        } catch (SQLException e) {
            logger.error("Failed to fetch driver by login. Params: login = {}", login);
            throw new DataProcessingException("Can't find driver with login " + login, e);
        }
    }

    private Driver parseDriverFromResultSet(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getObject("id", Long.class);
        String name = resultSet.getString("name");
        String licenseNumber = resultSet.getString("license_number");
        String login = resultSet.getString("login");
        String password = resultSet.getString("password");
        Driver driver = new Driver();
        driver.setId(id);
        driver.setName(name);
        driver.setLicenseNumber(licenseNumber);
        driver.setLogin(login);
        driver.setPassword(password);
        return driver;
    }
}
