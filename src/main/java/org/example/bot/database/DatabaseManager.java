package org.example.bot.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager {
    private String URL;
    private String USER;
    private String PASSWORD;

    private Connection connection;
    private static final Logger logger = Logger.getLogger(DatabaseManager.class.getName());

    public DatabaseManager() {
        System.out.println("База данных успешно подключена!");
        loadConfig();
        connectToDatabase();
    }

    private void loadConfig() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream("src/main/resources/dbconfig.properties")) {
            properties.load(input);
            URL = properties.getProperty("db.url");
            USER = properties.getProperty("db.user");
            PASSWORD = properties.getProperty("db.password");
            logger.info("Database configuration loaded successfully.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading database configuration", e);
        }
    }

    public void connectToDatabase() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            logger.info("Successfully connected to the database.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error connecting to the database", e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public DatabaseManager(Connection connection) {
        this.connection = connection;
    }

    public void addUser(User user) {
        String insertUserSQL = "INSERT INTO public.users (user_id, user_name, language, is_vegan, is_vegetarian, has_allergies, allergies, registration_step) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (user_id) DO UPDATE SET user_name = EXCLUDED.user_name, language = EXCLUDED.language, is_vegan = EXCLUDED.is_vegan, is_vegetarian = EXCLUDED.is_vegetarian, has_allergies = EXCLUDED.has_allergies, allergies = EXCLUDED.allergies, registration_step = EXCLUDED.registration_step";
        try (PreparedStatement statement = connection.prepareStatement(insertUserSQL)) {
            statement.setLong(1, user.getUserId());
            statement.setString(2, user.getUserName());
            statement.setString(3, user.getLanguage());
            statement.setBoolean(4, user.isVegan());
            statement.setBoolean(5, user.isVegetarian());
            statement.setBoolean(6, user.hasAllergies());
            statement.setString(7, user.getAllergies());
            statement.setInt(8, user.getRegistrationStep());
            statement.executeUpdate();
            logger.info("User added/updated successfully: " + user.getUserId());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding/updating user", e);
        }
    }

    public int getRegistrationStep(long userId) {
        String selectStepSQL = "SELECT registration_step FROM public.users WHERE user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(selectStepSQL)) {
            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int step = resultSet.getInt("registration_step");
                logger.info("Retrieved registration step for user: " + userId + " - Step: " + step);
                return step;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving registration step for user: " + userId, e);
        }
        return 0;
    }

    public void updateRegistrationStep(long userId, int step) {
        String updateStepSQL = "UPDATE public.users SET registration_step = ? WHERE user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(updateStepSQL)) {
            statement.setInt(1, step);
            statement.setLong(2, userId);
            statement.executeUpdate();
            logger.info("Updated registration step for user: " + userId + " - New Step: " + step);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating registration step for user: " + userId, e);
        }
    }

    public void updateLanguage(long userId, String language) {
        String updateStepSQL = "UPDATE public.users SET language = ? WHERE user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(updateStepSQL)) {
            statement.setString(1, language);
            statement.setLong(2, userId);
            statement.executeUpdate();
            logger.info("Updated language for user: " + userId + " - New Language: " + language);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating language for user: " + userId, e);
        }
    }

    public void updateVegan(long userId, Boolean ans) {
        String updateStepSQL = "UPDATE public.users SET is_vegan = ? WHERE user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(updateStepSQL)) {
            statement.setBoolean(1, ans);
            statement.setLong(2, userId);
            statement.executeUpdate();
            logger.info("Updated vegan status for user: " + userId + " - Is Vegan: " + ans);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating vegan status for user: " + userId, e);
        }
    }

    public void updateVegetarian(long userId, Boolean ans) {
        String updateStepSQL = "UPDATE public.users SET is_vegetarian = ? WHERE user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(updateStepSQL)) {
            statement.setBoolean(1, ans);
            statement.setLong(2, userId);
            statement.executeUpdate();
            logger.info("Updated vegetarian status for user: " + userId + " - Is Vegetarian: " + ans);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating vegetarian status for user: " + userId, e);
        }
    }

    public void updateHasAllergies(long userId, Boolean ans) {
        String updateStepSQL = "UPDATE public.users SET has_allergies = ? WHERE user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(updateStepSQL)) {
            statement.setBoolean(1, ans);
            statement.setLong(2, userId);
            statement.executeUpdate();
            logger.info("Updated allergy status for user: " + userId + " - Has Allergies: " + ans);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating allergy status for user: " + userId, e);
        }
    }

    public void updateAllergies(long userId, String allergies) {
        String updateStepSQL = "UPDATE public.users SET allergies = ? WHERE user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(updateStepSQL)) {
            statement.setString(1, allergies);
            statement.setLong(2, userId);
            statement.executeUpdate();
            logger.info("Updated allergies for user: " + userId + " - Allergies: " + allergies);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating allergies for user: " + userId, e);
        }
    }

    public Boolean hasAllergies(long userId) {
        String selectStepSQL = "SELECT has_allergies FROM public.users WHERE user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(selectStepSQL)) {
            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Boolean hasAllergies = resultSet.getBoolean("has_allergies");
                logger.info("Retrieved allergy status for user: " + userId + " - Has Allergies: " + hasAllergies);
                return hasAllergies;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving allergy status for user: " + userId, e);
        }
        return false;
    }

    public String getAllergies(long userId) {
        String selectStepSQL = "SELECT allergies FROM public.users WHERE user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(selectStepSQL)) {
            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String allergies = resultSet.getString("allergies");
                logger.info("Retrieved allergies for user: " + userId + " - Allergies: " + allergies);
                return allergies;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving allergies for user: " + userId, e);
        }
        return "";
    }

    public Boolean isVegan(long userId) {
        String selectStepSQL = "SELECT is_vegan FROM public.users WHERE user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(selectStepSQL)) {
            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Boolean isVegan = resultSet.getBoolean("is_vegan");
                logger.info("Retrieved vegan status for user: " + userId + " - Is Vegan: " + isVegan);
                return isVegan;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving vegan status for user: " + userId, e);
        }
        return false;
    }

    public Boolean isVegetarian(long userId) {
        String selectStepSQL = "SELECT is_vegetarian FROM public.users WHERE user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(selectStepSQL)) {
            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Boolean isVegetarian = resultSet.getBoolean("is_vegetarian");
                logger.info("Retrieved vegetarian status for user: " + userId + " - Is Vegetarian: " + isVegetarian);
                return isVegetarian;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving vegetarian status for user: " + userId, e);
        }
        return false;
    }

    public String getLanguage(long userId) {
        String selectStepSQL = "SELECT language FROM public.users WHERE user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(selectStepSQL)) {
            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String language = resultSet.getString("language");
                logger.info("Retrieved language for user: " + userId);
                return language;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving language for user: " + userId, e);
        }
        return "";
    }
}
