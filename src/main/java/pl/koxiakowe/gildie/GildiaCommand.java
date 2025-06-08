package pl.koxiakowe.gildie;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class GildiaCommand implements CommandExecutor {
    private final KoxiakoweGildie plugin;
    private final GildiaManager gildiaManager;
    private final Messages messages;

    public GildiaCommand(KoxiakoweGildie plugin) {
        this.plugin = plugin;
        this.gildiaManager = plugin.getGildiaManager();
        this.messages = plugin.getMessages();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.getMessage("player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            wyswietlPomoc(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "zaloz":
                if (args.length < 3) {
                    player.sendMessage(messages.getMessage("guild.create-usage"));
                    return true;
                }
                zalozGildie(player, args[1], args[2]);
                break;

            case "usun":
                if (args.length < 2) {
                    player.sendMessage(messages.getMessage("guild.delete-usage"));
                    return true;
                }
                usunGildie(player, args[1]);
                break;

            case "zapros":
                if (args.length < 2) {
                    player.sendMessage(messages.getMessage("guild.invite-usage"));
                    return true;
                }
                zaprosGracza(player, args[1]);
                break;

            case "wyrzuc":
                if (args.length < 2) {
                    player.sendMessage(messages.getMessage("guild.kick-usage"));
                    return true;
                }
                wyrzucGracza(player, args[1]);
                break;

            case "info":
                if (args.length < 2) {
                    wyswietlInfoGildii(player);
                } else {
                    wyswietlInfoGildii(player, args[1]);
                }
                break;

            case "zastepca":
                if (args.length < 2) {
                    player.sendMessage(messages.getMessage("guild.deputy-usage"));
                    return true;
                }
                ustawZastepce(player, args[1]);
                break;

            default:
                wyswietlPomoc(player);
                break;
        }

        return true;
    }

    private void wyswietlPomoc(Player player) {
        player.sendMessage(messages.getMessage("info.header"));
        player.sendMessage(messages.getMessage("info.create"));
        player.sendMessage(messages.getMessage("info.delete"));
        player.sendMessage(messages.getMessage("info.invite"));
        player.sendMessage(messages.getMessage("info.kick"));
        player.sendMessage(messages.getMessage("info.deputy"));
    }

    private void zalozGildie(Player player, String nazwa, String tag) {
        if (gildiaManager.jestWGildii(player.getUniqueId())) {
            player.sendMessage(messages.getMessage("guild.already-in-guild"));
            return;
        }

        if (gildiaManager.stworzGildie(nazwa, tag, player)) {
            player.sendMessage(messages.getMessage("guild.created", "name", nazwa, "tag", tag));
        } else {
            player.sendMessage(messages.getMessage("guild.not-enough-money", "cost", plugin.getConfig().getDouble("koszt_zakladania_gildii")));
        }
    }

    private void usunGildie(Player player, String nazwa) {
        Gildia gildia = gildiaManager.getGildia(nazwa);
        if (gildia == null) {
            player.sendMessage(messages.getMessage("guild.not-found"));
            return;
        }

        if (!gildia.getLider().equals(player.getUniqueId())) {
            player.sendMessage(messages.getMessage("guild.not-leader"));
            return;
        }

        gildiaManager.usunGildie(nazwa);
        player.sendMessage(messages.getMessage("guild.deleted", "name", nazwa));
    }

    private void zaprosGracza(Player player, String nazwaGracza) {
        Player target = Bukkit.getPlayer(nazwaGracza);
        if (target == null) {
            player.sendMessage(messages.getMessage("player-not-found"));
            return;
        }

        if (gildiaManager.zaprosGracza(player, target)) {
            player.sendMessage(messages.getMessage("members.invited", "player", target.getName()));
            target.sendMessage(messages.getMessage("members.invited-target", "player", player.getName()));
        } else {
            player.sendMessage(messages.getMessage("members.cannot-invite"));
        }
    }

    private void wyrzucGracza(Player player, String nazwaGracza) {
        Player target = Bukkit.getPlayer(nazwaGracza);
        if (target == null) {
            player.sendMessage(messages.getMessage("player-not-found"));
            return;
        }

        gildiaManager.wyrzucGracza(player, target);
        player.sendMessage(messages.getMessage("members.kicked", "player", target.getName()));
        target.sendMessage(messages.getMessage("members.kicked-target", "player", player.getName()));
    }

    private void wyswietlInfoGildii(Player player) {
        Gildia gildia = gildiaManager.getGildiaGracza(player.getUniqueId());
        if (gildia == null) {
            player.sendMessage(messages.getMessage("guild.not-in-guild"));
            return;
        }

        wyswietlInfoGildii(player, gildia.getNazwa());
    }

    private void wyswietlInfoGildii(Player player, String nazwa) {
        Gildia gildia = gildiaManager.getGildia(nazwa);
        if (gildia == null) {
            player.sendMessage(messages.getMessage("guild.not-found"));
            return;
        }

        player.sendMessage(messages.getMessage("info.header", "name", gildia.getNazwa()));
        player.sendMessage(messages.getMessage("info.tag", "tag", gildia.getTag()));
        player.sendMessage(messages.getMessage("info.leader", "leader", Bukkit.getOfflinePlayer(gildia.getLider()).getName()));
        player.sendMessage(messages.getMessage("info.members", "count", gildia.getCzlonkowie().size()));
        
        player.sendMessage(messages.getMessage("info.member-list"));
        for (UUID uuid : gildia.getCzlonkowie()) {
            player.sendMessage(messages.getMessage("info.member-format", "player", Bukkit.getOfflinePlayer(uuid).getName()));
        }
    }

    private void ustawZastepce(Player player, String nazwaGracza) {
        Player target = Bukkit.getPlayer(nazwaGracza);
        if (target == null) {
            player.sendMessage(messages.getMessage("player-not-found"));
            return;
        }

        Gildia gildia = gildiaManager.getGildiaGracza(player.getUniqueId());
        if (gildia == null || !gildia.getLider().equals(player.getUniqueId())) {
            player.sendMessage(messages.getMessage("guild.not-leader"));
            return;
        }

        if (!gildia.jestCzlonkiem(target.getUniqueId())) {
            player.sendMessage(messages.getMessage("members.not-member"));
            return;
        }

        if (gildia.jestZastepca(target.getUniqueId())) {
            gildia.usunZastepce(target.getUniqueId());
            player.sendMessage(messages.getMessage("deputy.removed", "player", target.getName()));
        } else {
            gildia.dodajZastepce(target.getUniqueId());
            player.sendMessage(messages.getMessage("deputy.set", "player", target.getName()));
        }
        gildiaManager.saveGildie();
    }
} 