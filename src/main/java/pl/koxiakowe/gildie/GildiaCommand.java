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
            sender.sendMessage(messages.getMessage("tylko_gracz"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            wyswietlPomoc(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("admin")) {
            if (!player.hasPermission("gildie.admin")) {
                player.sendMessage(messages.getMessage("gildia.nie_lider"));
                return true;
            }
            
            if (args.length < 3) {
                player.sendMessage(messages.getMessage("gildia.uzycie_admin"));
                return true;
            }

            switch (args[1].toLowerCase()) {
                case "usun":
                    usunGildieAdmin(player, args[2]);
                    break;
                default:
                    player.sendMessage(messages.getMessage("gildia.nieznana_komenda_admin"));
                    break;
            }
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "zaloz":
                if (args.length < 3) {
                    player.sendMessage(messages.getMessage("gildia.uzycie_zaloz"));
                    return true;
                }
                zalozGildie(player, args[1], args[2]);
                break;

            case "usun":
                if (args.length < 2) {
                    player.sendMessage(messages.getMessage("gildia.uzycie_usun"));
                    return true;
                }
                usunGildie(player, args[1]);
                break;

            case "zapros":
                if (args.length < 2) {
                    player.sendMessage(messages.getMessage("gildia.uzycie_zapros"));
                    return true;
                }
                zaprosGracza(player, args[1]);
                break;

            case "wyrzuc":
                if (args.length < 2) {
                    player.sendMessage(messages.getMessage("gildia.uzycie_wyrzuc"));
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
                    player.sendMessage(messages.getMessage("gildia.uzycie_zastepca"));
                    return true;
                }
                ustawZastepce(player, args[1]);
                break;

            case "opusc":
                opuscGildie(player);
                break;

            default:
                wyswietlPomoc(player);
                break;
        }

        return true;
    }

    private void wyswietlPomoc(Player player) {
        player.sendMessage(messages.getMessage("pomoc.naglowek"));
        if (player.hasPermission("gildie.admin")) {
            player.sendMessage(messages.getMessage("pomoc.admin"));
        }
        player.sendMessage(messages.getMessage("pomoc.zaloz"));
        player.sendMessage(messages.getMessage("pomoc.usun"));
        player.sendMessage(messages.getMessage("pomoc.zapros"));
        player.sendMessage(messages.getMessage("pomoc.wyrzuc"));
        player.sendMessage(messages.getMessage("pomoc.zastepca"));
        player.sendMessage(messages.getMessage("pomoc.opusc"));
        player.sendMessage(messages.getMessage("pomoc.info"));
    }

    private void zalozGildie(Player player, String nazwa, String tag) {
        if (gildiaManager.jestWGildii(player.getUniqueId())) {
            player.sendMessage(messages.getMessage("gildia.juz_w_gildii"));
            return;
        }

        if (gildiaManager.stworzGildie(nazwa, tag, player)) {
            player.sendMessage(messages.getMessage("gildia.utworzona", "nazwa", nazwa, "tag", tag));
        } else {
            player.sendMessage(messages.getMessage("gildia.za_malo_pieniedzy", "koszt", plugin.getConfig().getDouble("koszt_zakladania_gildii")));
        }
    }

    private void usunGildie(Player player, String nazwa) {
        Gildia gildia = gildiaManager.getGildia(nazwa);
        if (gildia == null) {
            player.sendMessage(messages.getMessage("gildia.nie_istnieje"));
            return;
        }

        if (!gildia.getLider().equals(player.getUniqueId()) && !player.hasPermission("gildie.admin")) {
            player.sendMessage(messages.getMessage("gildia.nie_lider"));
            return;
        }

        gildiaManager.usunGildie(nazwa);
        player.sendMessage(messages.getMessage("gildia.usunieta", "nazwa", nazwa));
    }

    private void zaprosGracza(Player player, String nazwaGracza) {
        Player target = Bukkit.getPlayer(nazwaGracza);
        if (target == null) {
            player.sendMessage(messages.getMessage("gracz_nie_znaleziony"));
            return;
        }

        if (gildiaManager.zaprosGracza(player, target)) {
            player.sendMessage(messages.getMessage("czlonkowie.zaprosil", "gracz", target.getName()));
            target.sendMessage(messages.getMessage("czlonkowie.zaproszony", "gildia", gildiaManager.getGildiaGracza(player.getUniqueId()).getNazwa(), "gracz", player.getName()));
        } else {
            player.sendMessage(messages.getMessage("czlonkowie.nie_moze_zaprosic"));
        }
    }

    private void wyrzucGracza(Player player, String nazwaGracza) {
        Player target = Bukkit.getPlayer(nazwaGracza);
        if (target == null) {
            player.sendMessage(messages.getMessage("gracz_nie_znaleziony"));
            return;
        }

        Gildia gildia = gildiaManager.getGildiaGracza(player.getUniqueId());
        if (gildia == null) {
            player.sendMessage(messages.getMessage("gildia.nie_jestes_w_gildii"));
            return;
        }

        if (player.getUniqueId().equals(target.getUniqueId())) {
            player.sendMessage(messages.getMessage("czlonkowie.nie_mozna_wyrzucic_siebie"));
            return;
        }

        if (!gildia.getLider().equals(player.getUniqueId()) && !gildia.jestZastepca(player.getUniqueId())) {
            player.sendMessage(messages.getMessage("gildia.nie_lider"));
            return;
        }

        if (gildia.getLider().equals(target.getUniqueId())) {
            player.sendMessage(messages.getMessage("czlonkowie.nie_mozna_wyrzucic_lidera"));
            return;
        }

        if (!gildia.jestCzlonkiem(target.getUniqueId())) {
            player.sendMessage(messages.getMessage("czlonkowie.nie_czlonkiem"));
            return;
        }

        if (gildiaManager.wyrzucGracza(player, target)) {
            player.sendMessage(messages.getMessage("czlonkowie.wyrzucil", "gracz", target.getName()));
            target.sendMessage(messages.getMessage("czlonkowie.wyrzucony", "gildia", gildia.getNazwa(), "gracz", player.getName()));
        }
    }

    private void wyswietlInfoGildii(Player player) {
        Gildia gildia = gildiaManager.getGildiaGracza(player.getUniqueId());
        if (gildia == null) {
            player.sendMessage(messages.getMessage("gildia.nie_w_gildii"));
            return;
        }

        wyswietlInfoGildii(player, gildia.getNazwa());
    }

    private void wyswietlInfoGildii(Player player, String nazwa) {
        Gildia gildia = gildiaManager.getGildia(nazwa);
        if (gildia == null) {
            player.sendMessage(messages.getMessage("gildia.nie_istnieje"));
            return;
        }

        player.sendMessage(messages.getMessage("info.naglowek", "nazwa", gildia.getNazwa()));
        player.sendMessage(messages.getMessage("info.tag", "tag", gildia.getTag()));
        player.sendMessage(messages.getMessage("info.lider", "lider", Bukkit.getOfflinePlayer(gildia.getLider()).getName()));
        player.sendMessage(messages.getMessage("info.liczba_czlonkow", "liczba", gildia.getCzlonkowie().size()));
        
        player.sendMessage(messages.getMessage("info.lista_czlonkow"));
        for (UUID uuid : gildia.getCzlonkowie()) {
            player.sendMessage(messages.getMessage("info.format_czlonka", "gracz", Bukkit.getOfflinePlayer(uuid).getName()));
        }
    }

    private void ustawZastepce(Player player, String nazwaGracza) {
        Player target = Bukkit.getPlayer(nazwaGracza);
        if (target == null) {
            player.sendMessage(messages.getMessage("gracz_nie_znaleziony"));
            return;
        }

        Gildia gildia = gildiaManager.getGildiaGracza(player.getUniqueId());
        if (gildia == null) {
            player.sendMessage(messages.getMessage("gildia.nie_jestes_w_gildii"));
            return;
        }

        if (!gildia.getLider().equals(player.getUniqueId())) {
            player.sendMessage(messages.getMessage("gildia.nie_lider"));
            return;
        }

        if (player.getUniqueId().equals(target.getUniqueId())) {
            player.sendMessage(messages.getMessage("czlonkowie.nie_mozna_mianowac_siebie"));
            return;
        }

        if (gildia.getLider().equals(target.getUniqueId())) {
            player.sendMessage(messages.getMessage("czlonkowie.lider_nie_moze_byc_zastepca"));
            return;
        }

        if (!gildia.jestCzlonkiem(target.getUniqueId())) {
            player.sendMessage(messages.getMessage("czlonkowie.nie_czlonkiem"));
            return;
        }

        if (gildiaManager.mianujZastepce(player, target)) {
            if (gildia.jestZastepca(target.getUniqueId())) {
                player.sendMessage(messages.getMessage("czlonkowie.mianowano_zastepce", "gracz", target.getName()));
                target.sendMessage(messages.getMessage("czlonkowie.zostales_zastepca", "gildia", gildia.getNazwa()));
            } else {
                player.sendMessage(messages.getMessage("czlonkowie.usunieto_zastepce", "gracz", target.getName()));
                target.sendMessage(messages.getMessage("czlonkowie.odebrano_zastepce", "gildia", gildia.getNazwa()));
            }
        }
    }

    private void opuscGildie(Player player) {
        if (gildiaManager.opuscGildie(player)) {
            player.sendMessage(messages.getMessage("czlonkowie.opuscil", "gildia", gildiaManager.getGildiaGracza(player.getUniqueId()).getNazwa()));
        } else {
            player.sendMessage(messages.getMessage("gildia.nie_mozna_opuscic"));
        }
    }

    private void usunGildieAdmin(Player player, String nazwa) {
        Gildia gildia = gildiaManager.getGildia(nazwa);
        if (gildia == null) {
            player.sendMessage(messages.getMessage("gildia.nie_istnieje"));
            return;
        }

        gildiaManager.usunGildie(nazwa);
        player.sendMessage(messages.getMessage("gildia.usunieta_admin", "nazwa", nazwa));
    }
} 