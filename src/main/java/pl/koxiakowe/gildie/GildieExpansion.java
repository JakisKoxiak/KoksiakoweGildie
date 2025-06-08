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
        return "Koxiakowe";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
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

        if (params.equals("gracz")) {
            Gildia gildia = plugin.getGildiaManager().getGildiaGracza(player.getUniqueId());
            return gildia != null ? gildia.getNazwa() : "Brak";
        }

        return null;
    }
} 