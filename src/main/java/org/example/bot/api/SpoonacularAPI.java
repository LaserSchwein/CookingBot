package org.example.bot.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SpoonacularAPI {
    private final String apiToken;
    private static final String BASE_URL = "https://api.spoonacular.com";

    // Конструктор для передачи токена
    public SpoonacularAPI(String apiToken) {
        this.apiToken = apiToken;
    }

    // Метод для поиска рецептов по ключевым словам
    public String searchRecipes(String query, String diet, String intolerances) throws Exception {
        String endpoint = "/recipes/complexSearch";
        String url = BASE_URL + endpoint + "?query=" + URLEncoder.encode(query, "UTF-8") +
                "&diet=" + URLEncoder.encode(diet, "UTF-8") +
                "&intolerances=" + URLEncoder.encode(intolerances, "UTF-8") +
                "&apiKey=" + apiToken;

        try {
            // Выполнение HTTP-запроса
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Чтение ответа
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return response.toString();
            } else {
                return "Ошибка: API вернул код " + responseCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Ошибка выполнения запроса: " + e.getMessage());
        }
    }

    // Метод для получения информации о рецепте по ID
    public String getRecipeInformation(int recipeId) throws Exception {
        String endpoint = "/recipes/" + recipeId + "/information";
        String url = BASE_URL + endpoint + "?apiKey=" + apiToken;

        try {
            // Выполнение HTTP-запроса
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Чтение ответа
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return response.toString();
            } else {
                return "Ошибка: API вернул код " + responseCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Ошибка выполнения запроса: " + e.getMessage());
        }
    }
}
