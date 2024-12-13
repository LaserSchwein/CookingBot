package org.example.bot.api;

import okhttp3.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.bot.database.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TranslateService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TranslateService.class);
    private static final String TRANSLATE_API_URL = "https://api.mymemory.translated.net/get?q=%s&langpair=%s|%s";

    private final OkHttpClient client;
    private final DatabaseManager databaseManager;

    public TranslateService(OkHttpClient client, DatabaseManager databaseManager) {
        this.client = client;
        this.databaseManager = databaseManager;
    }

    public String translateToEnglish(String text, Long userId) {
        String sourceLang = databaseManager.getLanguage(userId);
        LOGGER.info("Source language for user ID {}: {}", userId, sourceLang);

        if (sourceLang == null || sourceLang.isEmpty()) {
            LOGGER.error("Source language is not set for user with ID: {}", userId);
            return "";
        }

        String url = String.format(TRANSLATE_API_URL, text, sourceLang, "en");
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                LOGGER.error("Unexpected code {}", response);
                return "";
            }

            assert response.body() != null;
            return parseResponse(response.body().string());
        } catch (IOException e) {
            LOGGER.error("Error during translation", e);
            return "";
        }
    }

    public String translateFromEnglish(String text, Long userId) {
        String targetLang = databaseManager.getLanguage(userId);
        LOGGER.info("Target language for user ID {}: {}", userId, targetLang);

        if (targetLang == null || targetLang.isEmpty()) {
            LOGGER.error("Target language is not set for user with ID: {}", userId);
            return "";
        }

        String url = String.format(TRANSLATE_API_URL, text, "en", targetLang);
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                LOGGER.error("Unexpected code {}", response);
                return "";
            }

            assert response.body() != null;
            return parseResponse(response.body().string());
        } catch (IOException e) {
            LOGGER.error("Error during translation", e);
            return "";
        }
    }

    private String parseResponse(String responseBody) {
        JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonObject responseData = jsonResponse.getAsJsonObject("responseData");
        if (responseData == null) {
            LOGGER.error("Invalid response format: missing 'responseData'");
            return null;
        }
        return responseData.get("translatedText").getAsString();
    }
}
