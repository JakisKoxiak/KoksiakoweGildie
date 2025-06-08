package pl.koxiakowe.gildie;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Messages {
    private final KoxiakoweGildie plugin;
    private FileConfiguration messagesConfig;
    private File messagesFile;
    private String prefix;
    private final Map<String, String> messages;

    public Messages(KoxiakoweGildie plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
        loadMessages();
    }

    public void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        
        prefix = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("prefix", "&8[&6Gildie&8]"));
        
        loadSection("guild", messagesConfig.getConfigurationSection("guild"));
        loadSection("members", messagesConfig.getConfigurationSection("members"));
        loadSection("deputy", messagesConfig.getConfigurationSection("deputy"));
        loadSection("info", messagesConfig.getConfigurationSection("info"));
    }

    private void loadSection(String prefix, org.bukkit.configuration.ConfigurationSection section) {
        if (section == null) return;
        
        for (String key : section.getKeys(false)) {
            if (section.isConfigurationSection(key)) {
                loadSection(prefix + "." + key, section.getConfigurationSection(key));
            } else {
                messages.put(prefix + "." + key, section.getString(key));
            }
        }
    }

    public String getMessage(String path) {
        String message = messages.get(path);
        if (message == null) {
            return "Brak wiadomości: " + path;
        }
        return ChatColor.translateAlternateColorCodes('&', prefix + " " + message);
    }

    public String getMessage(String path, Object... args) {
        String message = getMessage(path);
        for (int i = 0; i < args.length; i += 2) {
            if (args[i] != null && args[i + 1] != null) {
                message = message.replace("%" + args[i] + "%", args[i + 1].toString());
            }
        }
        return message;
    }

    public void reload() {
        messages.clear();
        loadMessages();
    }

    public String getPrefix() {
        return prefix;
    }
} 