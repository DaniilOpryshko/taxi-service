package taxi.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import taxi.exception.DataProcessingException;
import taxi.lib.Dao;
import taxi.model.Car;
import taxi.model.Driver;
import taxi.model.Manufacturer;
import taxi.util.ConnectionUtil;

@Dao
public class CarDaoImpl implements CarDao {
    private static final int ZERO_PLACEHOLDER = 0;
    private static final int SHIFT = 2;
    private static final Logger logger = LogManager.getLogger(CarDaoImpl.class);

    @Override
    public Car create(Car car) {
        logger.info("Attempting to add car to DB. Params: car model = {}, car manufacturer = {}",
                car.getModel(), car.getManufacturer().getName());
        String insertQuery = "INSERT INTO cars (model, manufacturer_id)"
                + "VALUES (?, ?)";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement createCarStatement =
                        connection.prepareStatement(
                             insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            createCarStatement.setString(1, car.getModel());
            createCarStatement.setLong(2, car.getManufacturer().getId());
            createCarStatement.executeUpdate();
            ResultSet resultSet = createCarStatement.getGeneratedKeys();
            if (resultSet.next()) {
                car.setId(resultSet.getObject(1, Long.class));
            }
        } catch (SQLException e) {
            logger.error("Failed to add car to DB. Params: car model = {}, car manufacturer = {}",
                    car.getModel(), car.getManufacturer().getName());
            throw new DataProcessingException("Can't create car " + car, e);
        }
        insertAllDrivers(car);
        logger.info("Successfully added car to DB.Params: car model = {}, car manufacturer = {}",
                car.getModel(), car.getManufacturer().getName());
        return car;
    }

    @Override
    public Optional<Car> get(Long id) {
        logger.info("Attempting to get car from DB. Params: car id = {}", id);
        String selectQuery = "SELECT c.id AS id, "
                + "model, "
                + "manufacturer_id, "
                + "m.name AS manufacturer_name, "
                + "m.country AS manufacturer_country "
                + "FROM cars c"
                + " JOIN manufacturers m ON c.manufacturer_id = m.id"
                + " WHERE c.id = ? AND c.is_deleted = FALSE";
        Car car = null;
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement getCarStatement =
                        connection.prepareStatement(selectQuery)) {
            getCarStatement.setLong(1, id);
            ResultSet resultSet = getCarStatement.executeQuery();
            if (resultSet.next()) {
                car = parseCarFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            logger.error("Failed to fetch car from DB. Params: car id = {}", id);
            throw new DataProcessingException("Can't get car by id: " + id, e);
        }
        if (car != null) {
            car.setDrivers(getAllDriversByCarId(car.getId()));
        }
        logger.info("Successfully fetched car from DB. Params: car id = {}", id);
        return Optional.ofNullable(car);
    }

    @Override
    public List<Car> getAll() {
        logger.info("Attempting to fetch all cars from DB.");
        String selectQuery = "SELECT c.id AS id, "
                + "model, "
                + "manufacturer_id, "
                + "m.name AS manufacturer_name, "
                + "m.country AS manufacturer_country "
                + "FROM cars c"
                + " JOIN manufacturers m ON c.manufacturer_id = m.id"
                + " WHERE c.is_deleted = FALSE";
        List<Car> cars = new ArrayList<>();
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement getAllCarsStatement =
                        connection.prepareStatement(selectQuery)) {
            ResultSet resultSet = getAllCarsStatement.executeQuery();
            while (resultSet.next()) {
                cars.add(parseCarFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            logger.error("Failed to fetch all cars from DB.");
            throw new DataProcessingException("Can't get all cars", e);
        }
        cars.forEach(car -> car.setDrivers(getAllDriversByCarId(car.getId())));
        logger.info("Successfully fetched all car from DB.");
        return cars;
    }

    @Override
    public Car update(Car car) {
        logger.info("Attempting to update car in DB. Params: car id = {}", car.getId());
        String selectQuery = "UPDATE cars SET model = ?, manufacturer_id = ? WHERE id = ?"
                + " AND is_deleted = FALSE";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement updateCarStatement =
                        connection.prepareStatement(selectQuery)) {
            updateCarStatement.setString(1, car.getModel());
            updateCarStatement.setLong(2, car.getManufacturer().getId());
            updateCarStatement.setLong(3, car.getId());
            updateCarStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to update car in DB. Params: car id = {}", car.getId());
            throw new DataProcessingException("Can't update car " + car, e);
        }
        deleteAllDriversExceptList(car);
        insertAllDrivers(car);
        logger.info("Successfully updated car in DB. Params: car id = {}", car.getId());
        return car;
    }

    @Override
    public boolean delete(Long id) {
        logger.info("Attempting to delete car from DB. Params: car id = {}", id);
        String selectQuery = "UPDATE cars SET is_deleted = TRUE WHERE id = ?"
                + " AND is_deleted = FALSE";
        try (Connection connection = ConnectionUtil.getConnection();
                 PreparedStatement deleteCarStatement =
                         connection.prepareStatement(selectQuery)) {
            deleteCarStatement.setLong(1, id);
            logger.info("Successfully deleted car from DB. Params: car id = {}", id);
            return deleteCarStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to delete car from DB. Params: car id = {}", id);
            throw new DataProcessingException("Can't delete car by id " + id, e);
        }
    }

    @Override
    public List<Car> getAllByDriver(Long driverId) {
        logger.info("Attempting to fetch all cars by driver. Params: driver id = {}", driverId);
        String selectQuery = "SELECT c.id AS id, "
                + "model, "
                + "manufacturer_id, "
                + "m.name AS manufacturer_name, "
                + "m.country AS manufacturer_country "
                + "FROM cars c"
                + " JOIN manufacturers m ON c.manufacturer_id = m.id"
                + " JOIN cars_drivers cd ON c.id = cd.car_id"
                + " JOIN drivers d ON cd.driver_id = d.id"
                + " WHERE c.is_deleted = FALSE AND driver_id = ?"
                + " AND d.is_deleted = FALSE";
        List<Car> cars = new ArrayList<>();
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement getAllCarsByDriverStatement =
                        connection.prepareStatement(selectQuery)) {
            getAllCarsByDriverStatement.setLong(1, driverId);
            ResultSet resultSet = getAllCarsByDriverStatement.executeQuery();
            while (resultSet.next()) {
                cars.add(parseCarFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            logger.error("Failed to fetch all cars by driver. Params: driver id = {}", driverId);
            throw new DataProcessingException("Can't get all cars", e);
        }
        cars.forEach(car -> car.setDrivers(getAllDriversByCarId(car.getId())));
        logger.info("Successfully fetched all cars by driver. Params: driver id = {}", driverId);
        return cars;
    }

    private void insertAllDrivers(Car car) {
        logger.info("Attempting to add drivers to car. Params: car id = {}", car.getId());
        Long carId = car.getId();
        List<Driver> drivers = car.getDrivers();
        if (drivers.size() == 0) {
            return;
        }
        String insertQuery = "INSERT INTO cars_drivers (car_id, driver_id) VALUES "
                + drivers.stream().map(driver -> "(?, ?)").collect(Collectors.joining(", "))
                + " ON DUPLICATE KEY UPDATE car_id = car_id";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement linkDriverToCarStatement =
                        connection.prepareStatement(insertQuery)) {
            for (int i = 0; i < drivers.size(); i++) {
                Driver driver = drivers.get(i);
                linkDriverToCarStatement.setLong((i * SHIFT) + 1, carId);
                linkDriverToCarStatement.setLong((i * SHIFT) + 2, driver.getId());
            }
            linkDriverToCarStatement.executeUpdate();
            logger.info("Successfully added drivers to car. Params: car id = {}", car.getId());
        } catch (SQLException e) {
            logger.error("Failed to add drivers to car. Params: car id = {}", car.getId());
            throw new DataProcessingException("Can't insert drivers " + drivers, e);
        }
    }

    private void deleteAllDriversExceptList(Car car) {
        logger.info("Attempting to delete drivers from car. Params: car id = {}", car.getId());
        Long carId = car.getId();
        List<Driver> exceptions = car.getDrivers();
        int size = exceptions.size();
        String insertQuery = "DELETE FROM cars_drivers WHERE car_id = ? "
                + "AND NOT driver_id IN ("
                + ZERO_PLACEHOLDER + ", ?".repeat(size)
                + ");";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement deleteAllDriversExceptLinkedStatement =
                        connection.prepareStatement(insertQuery)) {
            deleteAllDriversExceptLinkedStatement.setLong(1, carId);
            for (int i = 0; i < size; i++) {
                Driver driver = exceptions.get(i);
                deleteAllDriversExceptLinkedStatement.setLong((i) + SHIFT, driver.getId());
            }
            deleteAllDriversExceptLinkedStatement.executeUpdate();
            logger.info("Successfully deleted drivers from car. Params: car id = {}", car.getId());
        } catch (SQLException e) {
            logger.error("Failed to delete drivers from car. Params: car id = {}", car.getId());
            throw new DataProcessingException("Can't delete drivers " + exceptions, e);
        }
    }

    private List<Driver> getAllDriversByCarId(Long carId) {
        logger.info("Attempting to fetch all car drivers. Params: car id = {}", carId);
        String selectQuery = "SELECT id, name, license_number, login, password "
                + "FROM cars_drivers cd "
                + "JOIN drivers d ON cd.driver_id = d.id "
                + "WHERE car_id = ? AND is_deleted = false";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement getDriversByCarIdStatement =
                        connection.prepareStatement(selectQuery)) {
            getDriversByCarIdStatement.setLong(1, carId);
            ResultSet resultSet = getDriversByCarIdStatement.executeQuery();
            List<Driver> drivers = new ArrayList<>();
            while (resultSet.next()) {
                drivers.add(parseDriverFromResultSet(resultSet));
            }
            logger.info("Successfully fetched all car drivers. Params: car id = {}", carId);
            return drivers;
        } catch (SQLException e) {
            logger.error("Failed to fetch all car drivers. Params: car id = {}", carId);
            throw new DataProcessingException("Can't get all drivers by car id" + carId, e);
        }
    }

    private Driver parseDriverFromResultSet(ResultSet resultSet) throws SQLException {
        Long driverId = resultSet.getObject("id", Long.class);
        String name = resultSet.getNString("name");
        String licenseNumber = resultSet.getNString("license_number");
        String login = resultSet.getNString("login");
        String password = resultSet.getNString("password");
        Driver driver = new Driver();
        driver.setId(driverId);
        driver.setName(name);
        driver.setLicenseNumber(licenseNumber);
        driver.setLogin(login);
        driver.setPassword(password);
        return driver;
    }

    private Car parseCarFromResultSet(ResultSet resultSet) throws SQLException {
        Long manufacturerId = resultSet.getObject("manufacturer_id", Long.class);
        String manufacturerName = resultSet.getNString("manufacturer_name");
        String manufacturerCountry = resultSet.getNString("manufacturer_country");
        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setId(manufacturerId);
        manufacturer.setName(manufacturerName);
        manufacturer.setCountry(manufacturerCountry);
        Long carId = resultSet.getObject("id", Long.class);
        String model = resultSet.getNString("model");
        Car car = new Car();
        car.setId(carId);
        car.setModel(model);
        car.setManufacturer(manufacturer);
        return car;
    }
}
