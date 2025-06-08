package pl.koxiakowe.gildie;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GildieExpansion extends PlaceholderExpansion {
    private final KoxiakoweGildie plugin;

    public GildieExpansion(KoxiakoweGildie plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "gildie";
    }

    @Override
    public @NotNull String getAuthor() {
        return "JakisKoxiak";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        Gildia gildia = plugin.getGildiaManager().getGildiaGracza(player.getUniqueId());
        if (gildia == null) {
            switch (params.toLowerCase()) {
                case "gracz":
                    return plugin.getConfig().getString("placeholdery.brak_gildii.nazwa", "Brak");
                case "tag":
                    return plugin.getConfig().getString("placeholdery.brak_gildii.tag", "Brak");
                case "czlonkowie":
                    return plugin.getConfig().getString("placeholdery.brak_gildii.czlonkowie", "0");
                default:
                    return "";
            }
        }

        switch (params.toLowerCase()) {
            case "gracz":
                return gildia.getNazwa();
            case "tag":
                return gildia.getTag();
            case "czlonkowie":
                return String.valueOf(gildia.getCzlonkowie().size());
            default:
                return "";
        }
    }
} 