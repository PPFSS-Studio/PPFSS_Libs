package com.ontadev.libs.player;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.entity.Player;

import java.util.UUID;


@Accessors(fluent = true)
@Getter
@Setter
@RequiredArgsConstructor
public final class PlayerSnapshot {
    private final UUID uuid;
    private final String name;
}