package me.eccentric_nz.bromleymassivetools;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ProfessionListener implements Listener {

    private final BromleyMassiveTools plugin;
    private final Map<Material, Villager.Profession> lookup = new HashMap<>();

    public ProfessionListener(BromleyMassiveTools plugin) {
        this.plugin = plugin;
        lookup.put(Material.ARROW, Villager.Profession.FLETCHER);
        lookup.put(Material.BEEF, Villager.Profession.BUTCHER);
        lookup.put(Material.BOOK, Villager.Profession.LIBRARIAN);
        lookup.put(Material.COD, Villager.Profession.FISHERMAN);
        lookup.put(Material.FILLED_MAP, Villager.Profession.CARTOGRAPHER);
        lookup.put(Material.GOLD_INGOT, Villager.Profession.ARMORER);
        lookup.put(Material.IRON_INGOT, Villager.Profession.WEAPONSMITH);
        lookup.put(Material.LEATHER, Villager.Profession.LEATHERWORKER);
        lookup.put(Material.REDSTONE, Villager.Profession.CLERIC);
        lookup.put(Material.RED_MUSHROOM, Villager.Profession.NITWIT);
        lookup.put(Material.STICK, Villager.Profession.TOOLSMITH);
        lookup.put(Material.STONE, Villager.Profession.MASON);
        lookup.put(Material.WHEAT, Villager.Profession.FARMER);
        lookup.put(Material.WHITE_WOOL, Villager.Profession.SHEPHERD);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        // get player
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        Material material = player.getInventory().getItemInMainHand().getType();
        if (entity.getType().equals(EntityType.VILLAGER)) {
            if (player.hasPermission("profession.change")) {
                // check if the config setting for this world allows profession changing
                String world = player.getWorld().getName();
                    if (lookup.containsKey(material)) {
                        Villager.Profession profession = lookup.get(material);
                        Villager villager = (Villager) entity;
                        villager.setProfession(profession);
                        player.sendMessage("The villager was changed to a "+ profession.getKey().getKey());
                        // should the material be used up?
                        if (plugin.getConfig().getBoolean("profession.consume")) {
                            int a = player.getInventory().getItemInMainHand().getAmount();
                            int a2 = a - 1;
                            if (a2 > 0) {
                                player.getInventory().getItemInMainHand().setAmount(a2);
                            } else {
                                player.getInventory().removeItem(new ItemStack(material, 1));
                            }
                        }
                        event.setCancelled(true);
                    }
            } else {
                if (plugin.getConfig().getBoolean("profession.message")) {
                    player.sendMessage("You do not have permission to change villager professions");
                }
            }
        }
    }
}
