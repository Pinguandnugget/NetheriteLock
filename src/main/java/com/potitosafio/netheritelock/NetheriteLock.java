package com.potitosafio.netheritelock;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.EnumSet;
import java.util.Set;

public final class NetheriteLock extends JavaPlugin implements Listener {
    private static final Set<Material> NETHERITE_GEAR = EnumSet.of(
        Material.NETHERITE_SWORD, Material.NETHERITE_AXE, Material.NETHERITE_PICKAXE,
        Material.NETHERITE_SHOVEL, Material.NETHERITE_HOE, Material.NETHERITE_HELMET,
        Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS);
    private boolean locked;

    @Override public void onEnable() {
        saveDefaultConfig();
        locked = getConfig().getBoolean("locked", true);
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("NetheriteLock activo. Bloqueado: " + locked);
    }

    private boolean isUpgrade(SmithingInventory inv) {
        ItemStack base = inv.getInputEquipment();
        ItemStack addition = inv.getInputMineral();
        ItemStack template = inv.getInputTemplate();
        if (base == null || addition == null || template == null) return false;
        return template.getType() == Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE
            && addition.getType() == Material.NETHERITE_INGOT
            && (base.getType().name().startsWith("DIAMOND_"));
    }

    @EventHandler public void onPrepare(PrepareSmithingEvent event) {
        if (locked && isUpgrade(event.getInventory())) event.setResult(null);
    }

    @EventHandler public void onClick(InventoryClickEvent event) {
        if (!locked || !(event.getView().getTopInventory() instanceof SmithingInventory inv)) return;
        if (event.getRawSlot() != 3 || !isUpgrade(inv)) return;
        event.setCancelled(true);
        if (event.getWhoClicked() instanceof Player p) {
            p.sendMessage(color(getConfig().getString("blocked-message", "&c&l¡EL NETHERITE ESTÁ BLOQUEADO! &7Aún no se ha liberado.")));
        }
    }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("netheritelock.admin")) {
            sender.sendMessage(color("&cNo tienes permiso.")); return true;
        }
        if (args.length != 1) {
            sender.sendMessage(color("&eUso: /netheritelock <bloquear|desbloquear|estado>")); return true;
        }
        switch (args[0].toLowerCase(java.util.Locale.ROOT)) {
            case "bloquear" -> setLocked(true, sender);
            case "desbloquear" -> setLocked(false, sender);
            case "estado" -> sender.sendMessage(color("&6Netherite: " + (locked ? "&cBLOQUEADO" : "&aDESBLOQUEADO")));
            default -> sender.sendMessage(color("&eUso: /netheritelock <bloquear|desbloquear|estado>"));
        }
        return true;
    }
    private void setLocked(boolean value, CommandSender sender) {
        locked = value;
        getConfig().set("locked", locked);
        saveConfig();
        sender.sendMessage(color(locked ? "&cNetherite bloqueado." : "&aNetherite desbloqueado."));
        // Refresca los resultados abiertos al cambiar el estado.
        getServer().getOnlinePlayers().forEach(p -> p.updateInventory());
    }
    private String color(String s) { return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s); }
}
