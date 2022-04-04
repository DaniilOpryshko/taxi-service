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
import taxi.model.Manufacturer;
import taxi.util.ConnectionUtil;

@Dao
public class ManufacturerDaoImpl implements ManufacturerDao {
    private static final Logger logger = LogManager.getLogger(ManufacturerDaoImpl.class);

    @Override
    public Manufacturer create(Manufacturer manufacturer) {
        logger.info("Attempting to add manufacturer to DB. Params: manufacturer name = {}, "
                + "manufacturer country = {}", manufacturer.getName(), manufacturer.getCountry());
        String query = "INSERT INTO manufacturers (name, country) VALUES (?,?)";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement createManufacturerStatement
                        = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            setUpdate(createManufacturerStatement, manufacturer).executeUpdate();
            ResultSet resultSet = createManufacturerStatement.getGeneratedKeys();
            if (resultSet.next()) {
                manufacturer.setId(resultSet.getObject(1, Long.class));
            }
            logger.info("Successfully added manufacturer to DB. Params: manufacturer name = {}, "
                    + "manufacturer country = {}",
                    manufacturer.getName(), manufacturer.getCountry());
            return manufacturer;
        } catch (SQLException e) {
            logger.error("Failed to add manufacturer to DB. Params: manufacturer name = {}, "
                    + "manufacturer country = {}",
                    manufacturer.getName(), manufacturer.getCountry());
            throw new DataProcessingException("Couldn't create manufacturer. " + manufacturer, e);
        }
    }

    @Override
    public Optional<Manufacturer> get(Long id) {
        logger.info("Attempting to fetch manufacturer from DB. Params: manufacturer id = {}", id);
        String query = "SELECT * FROM manufacturers WHERE id = ? AND is_deleted = FALSE";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement getManufacturerStatement = connection.prepareStatement(query)) {
            getManufacturerStatement.setLong(1, id);
            ResultSet resultSet = getManufacturerStatement.executeQuery();
            Manufacturer manufacturer = null;
            if (resultSet.next()) {
                manufacturer = parseManufacturerFromResultSet(resultSet);
            }
            logger.info("Successfully fetched manufacturer from DB. "
                    + "Params: manufacturer id = {}", id);
            return Optional.ofNullable(manufacturer);
        } catch (SQLException e) {
            logger.error("Failed to fetch manufacturer from DB. Params: manufacturer id = {}", id);
            throw new DataProcessingException("Couldn't get manufacturer by id " + id, e);
        }
    }

    @Override
    public List<Manufacturer> getAll() {
        logger.info("Attempting to fetch all manufacturers from DB.");
        String query = "SELECT * FROM manufacturers WHERE is_deleted = FALSE";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement getAllManufacturersStatement
                        = connection.prepareStatement(query)) {
            List<Manufacturer> manufacturers = new ArrayList<>();
            ResultSet resultSet = getAllManufacturersStatement.executeQuery();
            while (resultSet.next()) {
                manufacturers.add(parseManufacturerFromResultSet(resultSet));
            }
            logger.info("Successfully fetched all manufacturers from DB.");
            return manufacturers;
        } catch (SQLException e) {
            logger.error("Failed to fetch all manufacturers from DB.");
            throw new DataProcessingException("Couldn't get a list of manufacturers "
                    + "from manufacturers table. ", e);
        }
    }

    @Override
    public Manufacturer update(Manufacturer manufacturer) {
        logger.info("Attempting to update manufacturer. Params: manufacturer id = {}",
                manufacturer.getId());
        String query = "UPDATE manufacturers SET name = ?, country = ?"
                + " WHERE id = ? AND is_deleted = FALSE";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement updateManufacturerStatement
                        = setUpdate(connection.prepareStatement(query), manufacturer)) {
            updateManufacturerStatement.setLong(3, manufacturer.getId());
            updateManufacturerStatement.executeUpdate();
            logger.info("Successfully updated manufacturer. Params: manufacturer id = {}",
                    manufacturer.getId());
            return manufacturer;
        } catch (SQLException e) {
            logger.error("Failed to update manufacturer. Params: manufacturer id = {}",
                    manufacturer.getId());
            throw new DataProcessingException("Couldn't update a manufacturer "
                    + manufacturer, e);
        }
    }

    @Override
    public boolean delete(Long id) {
        logger.info("Attempting to delete manufacturer from DB. Params: manufacturer id = {}", id);
        String query = "UPDATE manufacturers SET is_deleted = TRUE WHERE id = ?";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement deleteManufacturerStatement
                        = connection.prepareStatement(query)) {
            deleteManufacturerStatement.setLong(1, id);
            logger.info("Successfully deleted manufacturer from DB. "
                    + "Params: manufacturer id = {}", id);
            return deleteManufacturerStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to delete manufacturer from DB. Params: manufacturer id = {}", id);
            throw new DataProcessingException("Couldn't delete a manufacturer by id " + id, e);
        }
    }

    private Manufacturer parseManufacturerFromResultSet(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getObject("id", Long.class);
        String name = resultSet.getString("name");
        String country = resultSet.getString("country");
        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setId(id);
        manufacturer.setName(name);
        manufacturer.setCountry(country);
        return manufacturer;
    }

    private PreparedStatement setUpdate(PreparedStatement statement,
                                        Manufacturer manufacturer) throws SQLException {
        statement.setString(1, manufacturer.getName());
        statement.setString(2, manufacturer.getCountry());
        return statement;
    }
}
