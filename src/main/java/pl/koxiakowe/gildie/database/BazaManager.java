package pl.koxiakowe.gildie.database;

import org.bukkit.Bukkit;
import pl.koxiakowe.gildie.Gildia;
import pl.koxiakowe.gildie.KoxiakoweGildie;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

public class BazaManager {
    private final KoxiakoweGildie plugin;
    private Connection connection;
    private final String type;
    private final int saveInterval;

    public BazaManager(KoxiakoweGildie plugin) {
        this.plugin = plugin;
        this.type = plugin.getConfig().getString("baza_danych.typ", "sqlite");
        this.saveInterval = plugin.getConfig().getInt("baza_danych.interwal_zapisywania", 5);
        initializeDatabase();
        startAutoSave();
    }

    private void initializeDatabase() {
        try {
            if (type.equalsIgnoreCase("mysql")) {
                initializeMySQL();
            } else {
                initializeSQLite();
            }
            createTables();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Nie udało się zainicjalizować bazy danych!", e);
        }
    }

    private void initializeMySQL() throws SQLException {
        String host = plugin.getConfig().getString("baza_danych.mysql.host", "localhost");
        int port = plugin.getConfig().getInt("baza_danych.mysql.port", 3306);
        String database = plugin.getConfig().getString("baza_danych.mysql.baza", "gildie");
        String user = plugin.getConfig().getString("baza_danych.mysql.uzytkownik", "root");
        String password = plugin.getConfig().getString("baza_danych.mysql.haslo", "");

        String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false", host, port, database);
        connection = DriverManager.getConnection(url, user, password);
    }

    private void initializeSQLite() throws SQLException {
        String url = "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/gildie.db";
        connection = DriverManager.getConnection(url);
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS gildie (" +
                    "nazwa VARCHAR(255) PRIMARY KEY," +
                    "tag VARCHAR(255) NOT NULL," +
                    "lider VARCHAR(36) NOT NULL" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS czlonkowie (" +
                    "gildia VARCHAR(255) NOT NULL," +
                    "uuid VARCHAR(36) NOT NULL," +
                    "PRIMARY KEY (gildia, uuid)," +
                    "FOREIGN KEY (gildia) REFERENCES gildie(nazwa) ON DELETE CASCADE" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS zastepcy (" +
                    "gildia VARCHAR(255) NOT NULL," +
                    "uuid VARCHAR(36) NOT NULL," +
                    "PRIMARY KEY (gildia, uuid)," +
                    "FOREIGN KEY (gildia) REFERENCES gildie(nazwa) ON DELETE CASCADE" +
                    ")");
        }
    }

    private void startAutoSave() {
        long ticks = saveInterval * 20L * 60;
        Bukkit.getScheduler().runTaskTimer(plugin, this::saveAllGildie, ticks, ticks);
    }

    public void saveAllGildie() {
        try {
            connection.setAutoCommit(false);
            try {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("DELETE FROM zastepcy");
                    stmt.execute("DELETE FROM czlonkowie");
                    stmt.execute("DELETE FROM gildie");
                }

                for (Gildia gildia : plugin.getGildiaManager().getAllGildie()) {
                    saveGildia(gildia);
                }
                
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Błąd podczas zapisywania gildii!", e);
        }
    }

    private void saveGildia(Gildia gildia) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO gildie (nazwa, tag, lider) VALUES (?, ?, ?)")) {
            ps.setString(1, gildia.getNazwa());
            ps.setString(2, gildia.getTag());
            ps.setString(3, gildia.getLider().toString());
            ps.executeUpdate();
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO czlonkowie (gildia, uuid) VALUES (?, ?)")) {
            for (UUID uuid : gildia.getCzlonkowie()) {
                ps.setString(1, gildia.getNazwa());
                ps.setString(2, uuid.toString());
                ps.addBatch();
            }
            ps.executeBatch();
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO zastepcy (gildia, uuid) VALUES (?, ?)")) {
            for (UUID uuid : gildia.getZastepcy()) {
                ps.setString(1, gildia.getNazwa());
                ps.setString(2, uuid.toString());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public void loadAllGildie() {
        try {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM gildie")) {
                while (rs.next()) {
                    String nazwa = rs.getString("nazwa");
                    String tag = rs.getString("tag");
                    UUID lider = UUID.fromString(rs.getString("lider"));
                    
                    Gildia gildia = new Gildia(nazwa, tag, lider);
                    
                    try (PreparedStatement ps = connection.prepareStatement(
                            "SELECT uuid FROM czlonkowie WHERE gildia = ?")) {
                        ps.setString(1, nazwa);
                        ResultSet membersRs = ps.executeQuery();
                        while (membersRs.next()) {
                            gildia.dodajCzlonka(UUID.fromString(membersRs.getString("uuid")));
                        }
                    }
                    
                    try (PreparedStatement ps = connection.prepareStatement(
                            "SELECT uuid FROM zastepcy WHERE gildia = ?")) {
                        ps.setString(1, nazwa);
                        ResultSet deputiesRs = ps.executeQuery();
                        while (deputiesRs.next()) {
                            gildia.dodajZastepce(UUID.fromString(deputiesRs.getString("uuid")));
                        }
                    }
                    
                    plugin.getGildiaManager().addGildia(gildia);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Błąd podczas wczytywania gildii!", e);
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Błąd podczas zamykania połączenia z bazą danych!", e);
        }
    }
} 