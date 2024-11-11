package org.example.bot;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
        String insertUserSQL = "INSERT INTO public.users (user_id, user_name) " +
                "VALUES (?, ?) " +
                "ON CONFLICT (user_id) DO UPDATE SET user_name = EXCLUDED.user_name";
        try (PreparedStatement statement = connection.prepareStatement(insertUserSQL)) {
            statement.setLong(1, user.getUserId());
            statement.setString(2, user.getUserName());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
