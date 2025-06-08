package pl.koxiakowe.gildie;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GildiaManager {
    private final KoxiakoweGildie plugin;
    private final Map<String, Gildia> gildie;
    private final Map<UUID, String> graczeGildie;
    private File gildieFile;
    private FileConfiguration gildieConfig;

    public GildiaManager(KoxiakoweGildie plugin) {
        this.plugin = plugin;
        this.gildie = new HashMap<>();
        this.graczeGildie = new HashMap<>();
        loadGildie();
    }

    public void loadGildie() {
        gildieFile = new File(plugin.getDataFolder(), "gildie.yml");
        if (!gildieFile.exists()) {
            try {
                gildieFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        gildieConfig = YamlConfiguration.loadConfiguration(gildieFile);
        
        if (gildieConfig.contains("gildie")) {
            for (String nazwa : gildieConfig.getConfigurationSection("gildie").getKeys(false)) {
                String tag = gildieConfig.getString("gildie." + nazwa + ".tag");
                UUID lider = UUID.fromString(gildieConfig.getString("gildie." + nazwa + ".lider"));
                Gildia gildia = new Gildia(nazwa, tag, lider);
                
                for (String uuidStr : gildieConfig.getStringList("gildie." + nazwa + ".czlonkowie")) {
                    UUID uuid = UUID.fromString(uuidStr);
                    gildia.dodajCzlonka(uuid);
                    graczeGildie.put(uuid, nazwa);
                }
                
                for (String uuidStr : gildieConfig.getStringList("gildie." + nazwa + ".zastepcy")) {
                    gildia.dodajZastepce(UUID.fromString(uuidStr));
                }
                
                gildie.put(nazwa, gildia);
            }
        }
    }

    public void saveGildie() {
        for (Map.Entry<String, Gildia> entry : gildie.entrySet()) {
            String nazwa = entry.getKey();
            Gildia gildia = entry.getValue();
            
            gildieConfig.set("gildie." + nazwa + ".tag", gildia.getTag());
            gildieConfig.set("gildie." + nazwa + ".lider", gildia.getLider().toString());
            
            gildieConfig.set("gildie." + nazwa + ".czlonkowie", 
                gildia.getCzlonkowie().stream().map(UUID::toString).collect(Collectors.toList()));
            gildieConfig.set("gildie." + nazwa + ".zastepcy", 
                gildia.getZastepcy().stream().map(UUID::toString).collect(Collectors.toList()));
        }
        
        try {
            gildieConfig.save(gildieFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean stworzGildie(String nazwa, String tag, Player lider) {
        if (gildie.containsKey(nazwa)) {
            return false;
        }
        
        double koszt = plugin.getConfig().getDouble("koszt_zakladania_gildii");
        if (plugin.getEconomy().getBalance(lider) < koszt) {
            return false;
        }
        
        plugin.getEconomy().withdrawPlayer(lider, koszt);
        Gildia gildia = new Gildia(nazwa, tag, lider.getUniqueId());
        gildie.put(nazwa, gildia);
        graczeGildie.put(lider.getUniqueId(), nazwa);
        saveGildie();
        return true;
    }

    public void usunGildie(String nazwa) {
        Gildia gildia = gildie.get(nazwa);
        if (gildia != null) {
            for (UUID uuid : gildia.getCzlonkowie()) {
                graczeGildie.remove(uuid);
            }
            gildie.remove(nazwa);
            saveGildie();
        }
    }

    public Gildia getGildia(String nazwa) {
        return gildie.get(nazwa);
    }

    public Gildia getGildiaGracza(UUID uuid) {
        String nazwa = graczeGildie.get(uuid);
        return nazwa != null ? gildie.get(nazwa) : null;
    }

    public boolean jestWGildii(UUID uuid) {
        return graczeGildie.containsKey(uuid);
    }

    public boolean zaprosGracza(Player zapraszajacy, Player zapraszany) {
        Gildia gildia = getGildiaGracza(zapraszajacy.getUniqueId());
        if (gildia == null || !gildia.maUprawnienie("zapraszanie")) {
            return false;
        }
        
        if (jestWGildii(zapraszany.getUniqueId())) {
            return false;
        }
        
        gildia.dodajCzlonka(zapraszany.getUniqueId());
        graczeGildie.put(zapraszany.getUniqueId(), gildia.getNazwa());
        saveGildie();
        return true;
    }

    public void wyrzucGracza(Player wyrzucajacy, Player wyrzucany) {
        Gildia gildia = getGildiaGracza(wyrzucajacy.getUniqueId());
        if (gildia != null && gildia.maUprawnienie("wyrzucanie")) {
            gildia.usunCzlonka(wyrzucany.getUniqueId());
            graczeGildie.remove(wyrzucany.getUniqueId());
            saveGildie();
        }
    }
} 