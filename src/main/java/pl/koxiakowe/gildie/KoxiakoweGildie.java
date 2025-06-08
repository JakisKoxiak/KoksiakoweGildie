package pl.koxiakowe.gildie;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import pl.koxiakowe.gildie.database.BazaManager;

public class KoxiakoweGildie extends JavaPlugin {
    private static KoxiakoweGildie instance;
    private Economy economy;
    private FileConfiguration config;
    private GildiaManager gildiaManager;
    private Messages messages;
    private BazaManager bazaManager;

    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        config = getConfig();
        
        if (!setupEconomy()) {
            getLogger().severe("Nie znaleziono pluginu Vault! Wyłączanie...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        } else {
            getLogger().info("Zarejestrowano integrację z Vault!");
        }
        
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new GildieExpansion(this).register();
            getLogger().info("Zarejestrowano integrację z PlaceholderAPI!");
        }
        
        messages = new Messages(this);
        bazaManager = new BazaManager(this);
        gildiaManager = new GildiaManager(this);
        
        getCommand("gildia").setExecutor(new GildiaCommand(this));
        getLogger().info("Plugin KoxiakoweGildie został włączony!");
    }

    @Override
    public void onDisable() {
        if (gildiaManager != null) {
            gildiaManager.onDisable();
        }
        if (bazaManager != null) {
            bazaManager.close();
        }
        getLogger().info("Plugin KoxiakoweGildie został wyłączony!");
    }

    private boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public static KoxiakoweGildie getInstance() {
        return instance;
    }

    public Economy getEconomy() {
        return economy;
    }

    public GildiaManager getGildiaManager() {
        return gildiaManager;
    }

    public Messages getMessages() {
        return messages;
    }

    public BazaManager getBazaManager() {
        return bazaManager;
    }
} 