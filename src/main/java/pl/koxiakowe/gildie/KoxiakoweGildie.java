package pl.koxiakowe.gildie;

import org.bukkit.plugin.java.JavaPlugin;
import pl.koxiakowe.gildie.database.BazaManager;

public class KoxiakoweGildie extends JavaPlugin {
    private GildiaManager gildiaManager;
    private Messages messages;
    private ConfigManager configManager;
    private BazaManager bazaManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("messages.yml", false);

        configManager = new ConfigManager(this);
        configManager.checkAndFixConfig();
        configManager.checkAndFixMessages();

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
        getLogger().info("Plugin KoxiakoweGildie został wyłączony!");
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

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void reloadMessages() {
        configManager.checkAndFixMessages();
        messages.loadMessages();
    }
} 