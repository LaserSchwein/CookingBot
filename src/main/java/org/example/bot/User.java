package org.example.bot;

public class User {
    private long userId;
    private String userName;

    // Конструктор
    public User(long userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    // Геттеры
    public long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

}
