package com.legacyminecraft.poseidon.event;

import net.minecraft.server.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;

/**
 * Thrown whenever a player has a statistic updated
 */
public class PlayerStatisticEvent extends PlayerEvent {
    private final Statistic statistic;
    private final int value;

    public PlayerStatisticEvent(Player who, Statistic statistic, int value) {
        super(Type.PLAYER_STATISTIC, who);
        this.statistic = statistic;
        this.value = value;
    }

    /**
     * Gets the updated statistic
     *
     * @return The updated statistic
     */
    public Statistic getStatistic() {
        return this.statistic;
    }

    /**
     * Gets the value added to the player's statistic
     *
     * @return The value added to the player's statistic
     */
    public int getValue() {
        return this.value;
    }

}
