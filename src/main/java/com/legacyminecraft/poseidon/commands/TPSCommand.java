package com.legacyminecraft.poseidon.commands;

import com.legacyminecraft.poseidon.Poseidon;
import com.legacyminecraft.poseidon.PoseidonConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.stream.Collectors;

public class TPSCommand extends Command {
    private static final Map<String, Integer> intervals;

    public TPSCommand(String name) {
        super(name);
        this.description = "Shows the server's TPS for various intervals";
        this.usageMessage = "/tps";
        this.setPermission("poseidon.command.tps");
    }

    static {
        intervals = Arrays.stream(PoseidonConfig.getInstance().getString("command.tps.intervals", "5s,1m,5m").split(","))
                .map(String::trim)
                .collect(Collectors.toMap(
                        s -> s,
                        s -> {
                            int num = Integer.parseInt(s.substring(0, s.length() - 1));
                            return switch (s.charAt(s.length() - 1)) {
                                case 'h' -> num * 3600;
                                case 'm' -> num * 60;
                                case 's' -> num;
                                default -> throw new IllegalArgumentException("Invalid interval: " + s);
                            };
                        },
                        (a, _) -> a,
                        LinkedHashMap::new
                ));
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (!testPermission(sender)) return true;

        LinkedList<Double> tpsRecords = Poseidon.getTpsRecords();
        StringBuilder message = new StringBuilder(ChatColor.GRAY + "Server TPS: ");

        // Calculate and format TPS for each interval dynamically
        for (Map.Entry<String, Integer> entry : intervals.entrySet()) {
            double averageTps = calculateAverage(tpsRecords, entry.getValue());
            message.append(formatTps(averageTps)).append(ChatColor.GRAY).append(" (").append(entry.getKey()).append("), ");
        }

        // Remove the trailing comma and space
        message.setLength(message.length() - 2);

        sender.sendMessage(message.toString());

        long memoryFree = Runtime.getRuntime().freeMemory() / 1024 / 1024;
        long memoryTotal = Runtime.getRuntime().totalMemory() / 1024 / 1024;
        sender.sendMessage(ChatColor.GRAY + "Memory usage: " + formatMemory(memoryFree, memoryTotal) + " MiB" + ChatColor.GRAY + " / " + memoryTotal + " MiB");

        return true;
    }

    private double calculateAverage(LinkedList<Double> records, int seconds) {
        int size = Math.min(records.size(), seconds);
        if (size == 0) return 20.0;

        double total = 0;
        for (int i = 0; i < size; i++) {
            total += records.get(i);
        }
        return total / size;
    }

    private String formatTps(double tps) {
        ChatColor colorCode;
        if (tps >= 18) {
            colorCode = ChatColor.GREEN;
        } else if (tps >= 15) {
            colorCode = ChatColor.YELLOW;
        } else {
            colorCode = ChatColor.RED;
        }
        return colorCode + String.format("%.2f", tps);
    }

    private String formatMemory(long free, long total) {
        long used = total - free;
        double percentage = (double) used / total;
        ChatColor colorCode;
        if (percentage < 0.8) {
            colorCode = ChatColor.GREEN;
        } else if (percentage < 0.9) {
            colorCode = ChatColor.YELLOW;
        } else {
            colorCode = ChatColor.RED;
        }
        return colorCode + String.valueOf(used);
    }
}
