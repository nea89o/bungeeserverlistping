package moe.nea89;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


public class ServerlistCheck extends Plugin implements Listener {

    private File hashFile;

    private Set<Integer> oldHashes = new HashSet<>();
    private final Set<Integer> newHashes = new HashSet<>();

    @Override
    public void onEnable() {
        hashFile = new File(getDataFolder(), "iphashes.txt");
        hashFile.getParentFile().mkdirs();
        if (!hashFile.exists()) {
            try {
                hashFile.createNewFile();
            } catch (IOException e) {
                System.err.println("Could not create iphashes.txt file. This is probably a permission error");
            }
        }
        try {
            oldHashes = Files.readAllLines(hashFile.toPath()).stream().map(Integer::parseInt).collect(Collectors.toSet());
        } catch (IOException e) {
            System.err.println("Server list checker could not load hash file.");
        }
        getProxy().getPluginManager().registerListener(this, this);
        getProxy().getPluginManager().registerCommand(this, new Command("uniqueserverlists", "serverlist.unique") {
            @Override
            public void execute(CommandSender sender, String[] args) {
                sender.sendMessage(new TextComponent(String.format("There have been %d detected unique server list pings.", oldHashes.size() + newHashes.size())));
            }
        });
        getProxy().getPluginManager().registerCommand(this, new Command("uniqueserverlistsave") {
            @Override
            public void execute(CommandSender sender, String[] args) {
                save();
            }
        });
    }

    @Override
    public void onDisable() {
        save();
    }

    public void save() {
        synchronized (newHashes) {
            try (BufferedWriter w = Files.newBufferedWriter(hashFile.toPath(), StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
                for (int h : newHashes) {
                    w.write(h + "\n");
                }
            } catch (IOException e) {
                System.err.println("Server list checker failed to write hashes");
            }
            oldHashes.addAll(newHashes);
            newHashes.clear();
        }
    }

    public int condenseIP(InetAddress address) {
        int i = 0;
        for (byte x : address.getAddress()) {
            i = i * 31 + x;
        }
        return i;
    }

    @EventHandler
    public void onPing(ProxyPingEvent event) {
        synchronized (newHashes) {
            int ipHash = condenseIP(event.getConnection().getAddress().getAddress());
            if (!oldHashes.contains(ipHash))
                newHashes.add(ipHash);
        }
    }

}
