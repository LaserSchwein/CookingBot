package org.example.bot.database;

public class User {
    private final long userId;
    private final String userName;
    private String language;
    private boolean isVegan;
    private boolean isVegetarian;
    private boolean hasAllergies;
    private String allergies;
    private int registrationStep;
    private String list;

    // Конструктор
    public User(long userId, String userName) {
        this.userId = userId;
        this.userName = userName;
        this.registrationStep = 0; // Устанавливаем начальный шаг в 0
    }

    // Геттеры и сеттеры
    public long getUserId() { return userId; }

    public String getUserName() {
        return userName;
    }

    public String getLanguage() {
        return language;
    }

    public String getList() {
        return list;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isVegan() {
        return isVegan;
    }

    public void setVegan(boolean vegan) {
        isVegan = vegan;
    }

    public boolean isVegetarian() {
        return isVegetarian;
    }

    public void setVegetarian(boolean vegetarian) {
        isVegetarian = vegetarian;
    }

    public boolean hasAllergies() {
        return hasAllergies;
    }

    public void setHasAllergies(boolean hasAllergies) {
        this.hasAllergies = hasAllergies;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public int getRegistrationStep() {
        return registrationStep;
    }

    public void setRegistrationStep(int registrationStep) {
        this.registrationStep = registrationStep;
    }
}
