package pl.koxiakowe.gildie;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class Gildia {
    private String nazwa;
    private String tag;
    private UUID lider;
    private Set<UUID> czlonkowie;
    private Set<UUID> zastepcy;
    private Map<String, Boolean> uprawnienia;

    public Gildia(String nazwa, String tag, UUID lider) {
        this.nazwa = nazwa;
        this.tag = tag;
        this.lider = lider;
        this.czlonkowie = new HashSet<>();
        this.zastepcy = new HashSet<>();
        this.uprawnienia = new HashMap<>();
        this.czlonkowie.add(lider);
        
        uprawnienia.put("zapraszanie", true);
        uprawnienia.put("wyrzucanie", true);
        uprawnienia.put("zarzadzanie_uprawnieniami", true);
    }

    public String getNazwa() {
        return nazwa;
    }

    public String getTag() {
        return tag;
    }

    public UUID getLider() {
        return lider;
    }

    public void setLider(UUID lider) {
        this.lider = lider;
    }

    public Set<UUID> getCzlonkowie() {
        return czlonkowie;
    }

    public Set<UUID> getZastepcy() {
        return zastepcy;
    }

    public void dodajCzlonka(UUID uuid) {
        czlonkowie.add(uuid);
    }

    public void usunCzlonka(UUID uuid) {
        czlonkowie.remove(uuid);
        zastepcy.remove(uuid);
    }

    public void dodajZastepce(UUID uuid) {
        if (czlonkowie.contains(uuid)) {
            zastepcy.add(uuid);
        }
    }

    public void usunZastepce(UUID uuid) {
        zastepcy.remove(uuid);
    }

    public boolean maUprawnienie(String uprawnienie) {
        return uprawnienia.getOrDefault(uprawnienie, false);
    }

    public void ustawUprawnienie(String uprawnienie, boolean wartosc) {
        uprawnienia.put(uprawnienie, wartosc);
    }

    public boolean jestCzlonkiem(UUID uuid) {
        return czlonkowie.contains(uuid);
    }

    public boolean jestZastepca(UUID uuid) {
        return zastepcy.contains(uuid);
    }

    public void wyslijWiadomosc(String wiadomosc) {
        for (UUID uuid : czlonkowie) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendMessage("§8[§6" + tag + "§8] §f" + wiadomosc);
            }
        }
    }
} 