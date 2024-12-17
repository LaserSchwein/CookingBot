package org.example.bot.api;

import okhttp3.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.bot.database.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TranslateService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TranslateService.class);
    private static final String TRANSLATE_API_URL = "https://api.mymemory.translated.net/get?q=%s&langpair=%s|%s";
    private static final int MAX_CHARACTERS = 500;

    private final OkHttpClient client;
    private final DatabaseManager databaseManager;

    public TranslateService(OkHttpClient client, DatabaseManager databaseManager) {
        this.client = client;
        this.databaseManager = databaseManager;
    }

    public String translateToEnglish(String text, Long userId) {
        String sourceLang = databaseManager.getLanguage(userId);
        LOGGER.info("Source language for user ID {}: {}", userId, sourceLang);

        if (Objects.equals(sourceLang, "en")) {
            return text;
        }

        if (sourceLang == null || sourceLang.isEmpty()) {
            LOGGER.error("Source language is not set for user with ID: {}", userId);
            return "";
        }

        List<String> textParts = splitTextByWords(text);
        StringBuilder translatedText = new StringBuilder();

        for (String part : textParts) {
            String url = String.format(TRANSLATE_API_URL, part, sourceLang, "en");
            Request request = new Request.Builder().url(url).build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    LOGGER.error("Unexpected code {}", response);
                    return "";
                }

                assert response.body() != null;
                translatedText.append(parseResponse(response.body().string()));
            } catch (IOException e) {
                LOGGER.error("Error during translation", e);
                return "";
            }
        }

        return translatedText.toString();
    }

    public String translateFromEnglish(String text, Long userId) {
        String targetLang = databaseManager.getLanguage(userId);
        LOGGER.info("Target language for user ID {}: {}", userId, targetLang);

        if (Objects.equals(targetLang, "en")) {
            return text;
        }

        if (targetLang == null || targetLang.isEmpty()) {
            LOGGER.error("Target language is not set for user with ID: {}", userId);
            return "";
        }

        List<String> textParts = splitTextByWords(text);
        StringBuilder translatedText = new StringBuilder();

        for (String part : textParts) {
            String url = String.format(TRANSLATE_API_URL, part, "en", targetLang);
            Request request = new Request.Builder().url(url).build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    LOGGER.error("Unexpected code {}", response);
                    return "";
                }

                assert response.body() != null;
                translatedText.append(parseResponse(response.body().string()));
            } catch (IOException e) {
                LOGGER.error("Error during translation", e);
                return "";
            }
        }

        return translatedText.toString();
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

    private List<String> splitTextByWords(String text) {
        List<String> parts = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder currentPart = new StringBuilder();

        for (String word : words) {
            if (currentPart.length() + word.length() + 1 > MAX_CHARACTERS) {
                parts.add(currentPart.toString().trim());
                currentPart = new StringBuilder();
            }
            if (!currentPart.isEmpty()) {
                currentPart.append(" ");
            }
            currentPart.append(word);
        }

        if (!currentPart.isEmpty()) {
            parts.add(currentPart.toString().trim());
        }

        return parts;
    }
}
