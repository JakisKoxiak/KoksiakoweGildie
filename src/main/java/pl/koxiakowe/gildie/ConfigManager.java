package pl.koxiakowe.gildie;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final KoxiakoweGildie plugin;
    private final Map<String, Object> defaultConfig;
    private final Map<String, Object> defaultMessages;

    public ConfigManager(KoxiakoweGildie plugin) {
        this.plugin = plugin;
        this.defaultConfig = new HashMap<>();
        this.defaultMessages = new HashMap<>();
        loadDefaults();
    }

    private void loadDefaults() {
        defaultConfig.put("database.host", "localhost");
        defaultConfig.put("database.port", 3306);
        defaultConfig.put("database.database", "minecraft");
        defaultConfig.put("database.username", "root");
        defaultConfig.put("database.password", "password");
        defaultConfig.put("ekonomia.wlaczone", true);
        defaultConfig.put("ekonomia.koszt_zakladania_gildii", 1000.0);
        defaultConfig.put("ekonomia.koszt_zmiany_tagu", 500.0);
        defaultConfig.put("gildie.min_dlugosc_nazwy", 3);
        defaultConfig.put("gildie.max_dlugosc_nazwy", 16);
        defaultConfig.put("gildie.min_dlugosc_tagu", 2);
        defaultConfig.put("gildie.max_dlugosc_tagu", 4);
        defaultConfig.put("gildie.max_czlonkow", 10);
        defaultConfig.put("gildie.max_zastepcow", 2);

        defaultMessages.put("prefix", "&8[&6Gildie&8] &r");
        defaultMessages.put("gracz_nie_znaleziony", "&cNie znaleziono gracza!");
        defaultMessages.put("gildia.nie_jestes_w_gildii", "&cNie jesteś w żadnej gildii!");
        defaultMessages.put("gildia.nie_lider", "&cTylko lider może wykonać tę komendę!");
        defaultMessages.put("gildia.nie_mozna_zalozyc", "&cNie możesz założyć gildii! Powód: %powod%");
        defaultMessages.put("gildia.utworzono", "&aUtworzono gildię %nazwa% z tagiem %tag%!");
        defaultMessages.put("gildia.usunieto", "&cUsunięto gildię %nazwa%!");
        defaultMessages.put("gildia.usunieta_admin", "&cAdministrator %gracz% usunął gildię %nazwa%!");
        defaultMessages.put("gildia.nie_istnieje", "&cGildia %nazwa% nie istnieje!");
        defaultMessages.put("gildia.juz_istnieje", "&cGildia o takiej nazwie już istnieje!");
        defaultMessages.put("gildia.juz_jestes_w_gildii", "&cJuż jesteś w gildii!");
        defaultMessages.put("gildia.nie_masz_pieniedzy", "&cNie masz wystarczająco pieniędzy! Potrzebujesz %koszt%!");
        defaultMessages.put("gildia.uzycie_admin", "&cUżycie: /gildia admin <usun|reload> [nazwa]");
        defaultMessages.put("gildia.uzycie_admin_usun", "&cUżycie: /gildia admin usun <nazwa>");
        defaultMessages.put("gildia.nieznana_komenda_admin", "&cNieznana komenda administracyjna!");
        defaultMessages.put("gildia.przeladowano", "&aPrzeładowano konfigurację i wiadomości pluginu!");
        defaultMessages.put("czlonkowie.nie_czlonkiem", "&cTen gracz nie jest członkiem twojej gildii!");
        defaultMessages.put("czlonkowie.nie_mozna_wyrzucic_siebie", "&cNie możesz wyrzucić samego siebie!");
        defaultMessages.put("czlonkowie.nie_mozna_wyrzucic_lidera", "&cNie możesz wyrzucić lidera gildii!");
        defaultMessages.put("czlonkowie.wyrzucil", "&aWyrzuciłeś gracza %gracz% z gildii!");
        defaultMessages.put("czlonkowie.wyrzucony", "&cZostałeś wyrzucony z gildii %gildia% przez %gracz%!");
        defaultMessages.put("czlonkowie.nie_mozna_mianowac_siebie", "&cNie możesz mianować samego siebie zastępcą!");
        defaultMessages.put("czlonkowie.lider_nie_moze_byc_zastepca", "&cLider nie może być zastępcą!");
        defaultMessages.put("czlonkowie.mianowano_zastepce", "&aMianowano gracza %gracz% zastępcą!");
        defaultMessages.put("czlonkowie.zostales_zastepca", "&aZostałeś mianowany zastępcą w gildii %gildia%!");
        defaultMessages.put("czlonkowie.usunieto_zastepce", "&cUsunięto zastępcę %gracz%!");
        defaultMessages.put("czlonkowie.odebrano_zastepce", "&cOdebrano ci stanowisko zastępcy w gildii %gildia%!");
        defaultMessages.put("pomoc.naglowek", "&6=== Pomoc Gildii ===");
        defaultMessages.put("pomoc.admin", "&c/gildia admin usun <nazwa> &7- Usuń gildię (tylko admin)");
        defaultMessages.put("pomoc.admin_reload", "&c/gildia admin reload &7- Przeładuj konfigurację (tylko admin)");
        defaultMessages.put("pomoc.zaloz", "&e/gildia zaloz <nazwa> <tag> &7- Załóż nową gildię");
        defaultMessages.put("pomoc.usun", "&e/gildia usun <nazwa> &7- Usuń swoją gildię");
        defaultMessages.put("pomoc.zapros", "&e/gildia zapros <gracz> &7- Zaproś gracza do gildii");
        defaultMessages.put("pomoc.wyrzuc", "&e/gildia wyrzuc <gracz> &7- Wyrzuć gracza z gildii");
        defaultMessages.put("pomoc.zastepca", "&e/gildia zastepca <gracz> &7- Ustaw/usuń zastępcę");
        defaultMessages.put("pomoc.opusc", "&e/gildia opusc &7- Opuść gildię");
        defaultMessages.put("pomoc.info", "&e/gildia info [nazwa] &7- Zobacz informacje o gildii");
    }

    public void checkAndFixConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        boolean needsSave = false;

        for (Map.Entry<String, Object> entry : defaultConfig.entrySet()) {
            if (!config.contains(entry.getKey())) {
                config.set(entry.getKey(), entry.getValue());
                needsSave = true;
                plugin.getLogger().info("Naprawiono brakującą wartość w config.yml: " + entry.getKey());
            }
        }

        if (needsSave) {
            try {
                config.save(configFile);
                plugin.getLogger().info("Zapisano poprawiony plik config.yml");
            } catch (IOException e) {
                plugin.getLogger().severe("Nie udało się zapisać poprawionego pliku config.yml: " + e.getMessage());
            }
        }
    }

    public void checkAndFixMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        FileConfiguration messages = YamlConfiguration.loadConfiguration(messagesFile);
        boolean needsSave = false;

        for (Map.Entry<String, Object> entry : defaultMessages.entrySet()) {
            if (!messages.contains(entry.getKey())) {
                messages.set(entry.getKey(), entry.getValue());
                needsSave = true;
                plugin.getLogger().info("Naprawiono brakującą wartość w messages.yml: " + entry.getKey());
            }
        }

        if (needsSave) {
            try {
                messages.save(messagesFile);
                plugin.getLogger().info("Zapisano poprawiony plik messages.yml");
            } catch (IOException e) {
                plugin.getLogger().severe("Nie udało się zapisać poprawionego pliku messages.yml: " + e.getMessage());
            }
        }
    }
} 