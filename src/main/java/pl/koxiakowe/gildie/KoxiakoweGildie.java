package pl.koxiakowe.gildie;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class KoxiakoweGildie extends JavaPlugin {
    private static KoxiakoweGildie instance;
    private Economy economy;
    private FileConfiguration config;
    private GildiaManager gildiaManager;
    private Messages messages;

    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        config = getConfig();
        
        if (!setupEconomy()) {
            getLogger().severe("Nie znaleziono pluginu Vault! Plugin zostanie wyłączony!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        messages = new Messages(this);
        gildiaManager = new GildiaManager(this);
        
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new GildieExpansion(this).register();
        }
        
        getCommand("gildia").setExecutor(new GildiaCommand(this));
        getLogger().info("Plugin KoxiakoweGildie został włączony!");
    }

    @Override
    public void onDisable() {
        if (gildiaManager != null) {
            gildiaManager.saveGildie();
        }
        getLogger().info("Plugin KoxiakoweGildie został wyłączony!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
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
} 