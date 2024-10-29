package org.example.bot;

import java.sql.*;

public class DatabaseManager {
    private String URL = "jdbc:postgresql://localhost:5432/userdata"; // замените на ваш URL базы данных
    private String USER = "postgres"; // замените на ваше имя пользователя
    private String PASSWORD = "postgres"; // замените на ваш пароль

    private Connection connection;

    public void DatabaseHandler() {
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