package pl.koxiakowe.gildie;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.koxiakowe.gildie.database.BazaManager;

import java.util.*;

public class GildiaManager {
    private final KoxiakoweGildie plugin;
    private final Map<String, Gildia> gildie;
    private final Map<UUID, String> graczeGildie;
    private final BazaManager bazaManager;

    public GildiaManager(KoxiakoweGildie plugin) {
        this.plugin = plugin;
        this.gildie = new HashMap<>();
        this.graczeGildie = new HashMap<>();
        this.bazaManager = plugin.getBazaManager();
        loadGildie();
    }

    public void loadGildie() {
        bazaManager.loadAllGildie();
    }

    public void saveGildie() {
        bazaManager.saveAllGildie();
    }

    public void addGildia(Gildia gildia) {
        gildie.put(gildia.getNazwa(), gildia);
        for (UUID uuid : gildia.getCzlonkowie()) {
            graczeGildie.put(uuid, gildia.getNazwa());
        }
    }

    public Collection<Gildia> getAllGildie() {
        return gildie.values();
    }

    public boolean stworzGildie(String nazwa, String tag, Player lider) {
        if (gildie.containsKey(nazwa)) {
            return false;
        }
        
        double koszt = plugin.getConfig().getDouble("ekonomia.koszt_zakladania_gildii");
        if (koszt > 0) {
            if (!plugin.getServer().getPluginManager().isPluginEnabled("Vault")) {
                plugin.getLogger().warning("Vault nie jest zainstalowany! Ekonomia jest wyłączona.");
                return false;
            }
            
            net.milkbowl.vault.economy.Economy economy = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class).getProvider();
            double saldo = economy.getBalance(lider);
            if (saldo < koszt) {
                return false;
            }
            economy.withdrawPlayer(lider, koszt);
        }
        
        Gildia gildia = new Gildia(nazwa, tag, lider.getUniqueId());
        addGildia(gildia);
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
        if (gildia == null) {
            return false;
        }

        if (!gildia.getLider().equals(zapraszajacy.getUniqueId()) && !gildia.jestZastepca(zapraszajacy.getUniqueId())) {
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

    public boolean wyrzucGracza(Player wyrzucajacy, Player wyrzucany) {
        Gildia gildia = getGildiaGracza(wyrzucajacy.getUniqueId());
        if (gildia == null) {
            return false;
        }

        if (wyrzucajacy.getUniqueId().equals(wyrzucany.getUniqueId())) {
            return false;
        }

        if (!gildia.getLider().equals(wyrzucajacy.getUniqueId()) && !gildia.jestZastepca(wyrzucajacy.getUniqueId())) {
            return false;
        }

        if (gildia.getLider().equals(wyrzucany.getUniqueId())) {
            return false;
        }

        if (!gildia.jestCzlonkiem(wyrzucany.getUniqueId())) {
            return false;
        }

        gildia.usunCzlonka(wyrzucany.getUniqueId());
        graczeGildie.remove(wyrzucany.getUniqueId());
        saveGildie();
        return true;
    }

    public boolean opuscGildie(Player player) {
        Gildia gildia = getGildiaGracza(player.getUniqueId());
        if (gildia == null) {
            return false;
        }

        if (gildia.getLider().equals(player.getUniqueId())) {
            return false;
        }

        String nazwaGildii = gildia.getNazwa();
        gildia.usunCzlonka(player.getUniqueId());
        graczeGildie.remove(player.getUniqueId());
        saveGildie();
        return true;
    }

    public boolean mianujZastepce(Player lider, Player nowyZastepca) {
        Gildia gildia = getGildiaGracza(lider.getUniqueId());
        if (gildia == null) {
            return false;
        }

        if (!gildia.getLider().equals(lider.getUniqueId())) {
            return false;
        }

        if (lider.getUniqueId().equals(nowyZastepca.getUniqueId())) {
            return false;
        }
        
        if (gildia.getLider().equals(nowyZastepca.getUniqueId())) {
            return false;
        }
        
        if (!gildia.jestCzlonkiem(nowyZastepca.getUniqueId())) {
            return false;
        }

        if (gildia.jestZastepca(nowyZastepca.getUniqueId())) {
            gildia.usunZastepce(nowyZastepca.getUniqueId());
            saveGildie();
            return true;
        } else {
            gildia.dodajZastepce(nowyZastepca.getUniqueId());
            saveGildie();
            return true;
        }
    }

    public void onDisable() {
        bazaManager.close();
    }
} 