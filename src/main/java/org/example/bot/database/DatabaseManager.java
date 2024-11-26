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

public class DatabaseManager {
    private String URL;
    private String USER;
    private String PASSWORD;

    private Connection connection;

    public DatabaseManager() {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connectToDatabase() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("База данных: " + URL + " успешно подключена.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getRegistrationStep(long userId) {
        String selectStepSQL = "SELECT registration_step FROM public.users WHERE user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(selectStepSQL)) {
            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("registration_step");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void updateRegistrationStep(long userId, int step) {
        String updateStepSQL = "UPDATE public.users SET registration_step = ? WHERE user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(updateStepSQL)) {
            statement.setInt(1, step);
            statement.setLong(2, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateLanguage(long userId, String language) {
        String updateStepSQL = "UPDATE public.users SET language = ? WHERE user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(updateStepSQL)) {
            statement.setString(1, language);
            statement.setLong(2, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateVegan(long userId, Boolean ans) {
        String updateStepSQL = "UPDATE public.users SET is_vegan = ? WHERE user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(updateStepSQL)) {
            statement.setBoolean(1, ans);
            statement.setLong(2, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateVegetarian(long userId, Boolean ans) {
        String updateStepSQL = "UPDATE public.users SET is_vegetarian = ? WHERE user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(updateStepSQL)) {
            statement.setBoolean(1, ans);
            statement.setLong(2, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateHasAllergies(long userId, Boolean ans) {
        String updateStepSQL = "UPDATE public.users SET has_allergies = ? WHERE user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(updateStepSQL)) {
            statement.setBoolean(1, ans);
            statement.setLong(2, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateAllergies(long userId, String allergies) {
        String updateStepSQL = "UPDATE public.users SET allergies = ? WHERE user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(updateStepSQL)) {
            statement.setString(1, allergies);
            statement.setLong(2, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Boolean hasAllergies(long userId) {
        String selectStepSQL = "SELECT has_allergies FROM public.users WHERE user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(selectStepSQL)) {
            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getBoolean("has_allergies");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getAllergies(long userId) {
        String selectStepSQL = "SELECT allergies FROM public.users WHERE user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(selectStepSQL)) {
            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("allergies");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public Boolean isVegan(long userId) {
        String selectStepSQL = "SELECT is_vegan FROM public.users WHERE user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(selectStepSQL)) {
            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getBoolean("is_vegan");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Boolean isVegetarian(long userId) {
        String selectStepSQL = "SELECT is_vegetarian FROM public.users WHERE user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(selectStepSQL)) {
            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getBoolean("is_vegetarian");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
