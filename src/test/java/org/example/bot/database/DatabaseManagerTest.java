package org.example.bot.database;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseManagerTest {

    private DatabaseManager databaseManager;
    private Connection connectionMock;
    private PreparedStatement statementMock;
    private ResultSet resultSetMock;
    private User testUser;

    @BeforeEach
    void setUp() throws SQLException {
        connectionMock = Mockito.mock(Connection.class);
        statementMock = Mockito.mock(PreparedStatement.class);
        resultSetMock = Mockito.mock(ResultSet.class);

        databaseManager = Mockito.spy(new DatabaseManager());

        testUser = new User(1L, "testUser");
    }


    @Test
    void testLoadConfig() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("src/main/resources/dbconfig.properties"));
        assertNotNull(properties.getProperty("db.url"));
        assertNotNull(properties.getProperty("db.user"));
        assertNotNull(properties.getProperty("db.password"));
    }

    @Test
    void testAddUser() throws SQLException {
        String insertUserSQL = "INSERT INTO public.users (user_id, user_name, language, is_vegan, is_vegetarian, has_allergies, allergies, registration_step) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (user_id) DO UPDATE SET user_name = EXCLUDED.user_name, language = EXCLUDED.language, is_vegan = EXCLUDED.is_vegan, is_vegetarian = EXCLUDED.is_vegetarian, has_allergies = EXCLUDED.has_allergies, allergies = EXCLUDED.allergies, registration_step = EXCLUDED.registration_step";

        when(connectionMock.prepareStatement(insertUserSQL)).thenReturn(statementMock);

        testUser.setLanguage("ru");
        testUser.setVegan(true);
        testUser.setVegetarian(false);
        testUser.setHasAllergies(true);
        testUser.setAllergies("nuts");
        testUser.setRegistrationStep(3);

        databaseManager.addUser(testUser);

        verify(statementMock, times(1)).setLong(1, testUser.getUserId());
        verify(statementMock, times(1)).setString(2, testUser.getUserName());
        verify(statementMock, times(1)).setString(3, testUser.getLanguage());
        verify(statementMock, times(1)).setBoolean(4, testUser.isVegan());
        verify(statementMock, times(1)).setBoolean(5, testUser.isVegetarian());
        verify(statementMock, times(1)).setBoolean(6, testUser.hasAllergies());
        verify(statementMock, times(1)).setString(7, testUser.getAllergies());
        verify(statementMock, times(1)).setInt(8, testUser.getRegistrationStep());
        verify(statementMock, times(1)).executeUpdate();
    }

    @Test
    void testUpdateRegistrationStep() throws SQLException {
        String updateStepSQL = "UPDATE public.users SET registration_step = ? WHERE user_id = ?";

        when(connectionMock.prepareStatement(updateStepSQL)).thenReturn(statementMock);

        databaseManager.updateRegistrationStep(testUser.getUserId(), 2);

        verify(statementMock, times(1)).setInt(1, 2);
        verify(statementMock, times(1)).setLong(2, testUser.getUserId());
        verify(statementMock, times(1)).executeUpdate();
    }

    @Test
    void testGetRegistrationStep() throws SQLException {
        String selectStepSQL = "SELECT registration_step FROM public.users WHERE user_id = ?";

        when(connectionMock.prepareStatement(selectStepSQL)).thenReturn(statementMock);
        when(statementMock.executeQuery()).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(true);
        when(resultSetMock.getInt("registration_step")).thenReturn(3);

        int step = databaseManager.getRegistrationStep(testUser.getUserId());

        assertEquals(3, step);
        verify(statementMock, times(1)).setLong(1, testUser.getUserId());
        verify(statementMock, times(1)).executeQuery();
    }

    @Test
    void testHasAllergies() throws SQLException {
        String selectHasAllergiesSQL = "SELECT has_allergies FROM public.users WHERE user_id = ?";

        when(connectionMock.prepareStatement(selectHasAllergiesSQL)).thenReturn(statementMock);
        when(statementMock.executeQuery()).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(true);
        when(resultSetMock.getBoolean("has_allergies")).thenReturn(true);

        boolean hasAllergies = databaseManager.hasAllergies(testUser.getUserId());

        assertTrue(hasAllergies);
        verify(statementMock, times(1)).setLong(1, testUser.getUserId());
        verify(statementMock, times(1)).executeQuery();
    }

    @Test
    void testUpdateLanguage() throws SQLException {
        String updateLanguageSQL = "UPDATE public.users SET language = ? WHERE user_id = ?";

        when(connectionMock.prepareStatement(updateLanguageSQL)).thenReturn(statementMock);

        databaseManager.updateLanguage(testUser.getUserId(), "en");

        verify(statementMock, times(1)).setString(1, "en");
        verify(statementMock, times(1)).setLong(2, testUser.getUserId());
        verify(statementMock, times(1)).executeUpdate();
    }
}
