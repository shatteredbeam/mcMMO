package com.gmail.nossr50.listeners;

import java.util.List;

import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.config.Config;
import com.gmail.nossr50.datatypes.AbilityType;
import com.gmail.nossr50.datatypes.PlayerProfile;
import com.gmail.nossr50.datatypes.SkillType;
import com.gmail.nossr50.datatypes.ToolType;
import com.gmail.nossr50.skills.Excavation;
import com.gmail.nossr50.skills.Herbalism;
import com.gmail.nossr50.skills.Mining;
import com.gmail.nossr50.skills.Repair;
import com.gmail.nossr50.skills.Skills;
import com.gmail.nossr50.skills.WoodCutting;
import com.gmail.nossr50.spout.SpoutSounds;
import com.gmail.nossr50.util.BlockChecks;
import com.gmail.nossr50.util.ItemChecks;
import com.gmail.nossr50.util.Permissions;
import com.gmail.nossr50.util.Users;
import com.gmail.nossr50.events.fake.FakeBlockBreakEvent;
import com.gmail.nossr50.events.fake.FakePlayerAnimationEvent;

import org.bukkit.CropState;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import org.getspout.spoutapi.sound.SoundEffect;

public class BlockListener implements Listener {
    private final mcMMO plugin;

    public BlockListener(final mcMMO plugin) {
        this.plugin = plugin;
    }

    /**
     * Monitor BlockPistonExtend events.
     *
     * @param event The event to monitor
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        List<Block> blocks = event.getBlocks();
        BlockFace direction = event.getDirection();

        for (Block b : blocks) {
            if (b.hasMetadata("mcmmoPlacedBlock")) {
                b.getRelative(direction).setMetadata("mcmmoNeedsTracking", new FixedMetadataValue(plugin, true));
                b.removeMetadata("mcmmoPlacedBlock", plugin);
            }
        }

        for (Block b : blocks) {
            if (b.getRelative(direction).hasMetadata("mcmmoNeedsTracking")) {
                b.getRelative(direction).setMetadata("mcmmoPlacedBlock", new FixedMetadataValue(plugin, true));
                b.getRelative(direction).removeMetadata("mcmmoNeedsTracking", plugin);
            }
        }
    }

    /**
     * Monitor BlockPistonRetract events.
     *
     * @param event The event to monitor
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        Block block = event.getRetractLocation().getBlock();

        if (block.hasMetadata("mcmmoPlacedBlock")) {
            block.removeMetadata("mcmmoPlacedBlock", plugin);
            event.getBlock().getRelative(event.getDirection()).setMetadata("mcmmoPlacedBlock", new FixedMetadataValue(plugin, true));
        }
    }

    /**
     * Monitor BlockPlace events.
     *
     * @param event The event to monitor
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        int id = block.getTypeId();
        Material mat = block.getType();

        /* Code to prevent issues with placed falling Sand/Gravel not being tracked */
        if (mat.equals(Material.SAND) || mat.equals(Material.GRAVEL)) {
            for (int y = -1;  y + block.getY() >= 0; y--) {
                if (block.getRelative(0, y, 0).getType().equals(Material.AIR)) {
                    continue;
                }
                else {
                    Block newLocation = block.getRelative(0, y + 1, 0);
                    newLocation.setMetadata("mcmmoPlacedBlock", new FixedMetadataValue(plugin, true));
                    break;
                }
            }
        }

        /* Check if the blocks placed should be monitored so they do not give out XP in the future */
        if (BlockChecks.shouldBeWatched(mat)) {
            block.setMetadata("mcmmoPlacedBlock", new FixedMetadataValue(plugin, true));
        }

        if (id == Config.getInstance().getRepairAnvilId() && Config.getInstance().getRepairAnvilMessagesEnabled()) {
            Repair.placedAnvilCheck(player, id);
        }
    }

    /**
     * Monitor BlockBreak events.
     *
     * @param event The event to monitor
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerProfile PP = Users.getProfile(player);
        Block block = event.getBlock();
        Material mat = block.getType();
        ItemStack inhand = player.getItemInHand();

        if (event instanceof FakeBlockBreakEvent) {
            return;
        }

        /*
         * HERBALISM
         */

        /* Green Terra */
        if (PP.getToolPreparationMode(ToolType.HOE) && Permissions.getInstance().greenTerra(player) && ((mat.equals(Material.CROPS) && block.getData() == CropState.RIPE.getData()) || BlockChecks.canBeGreenTerra(mat))) {
            Skills.abilityCheck(player, SkillType.HERBALISM);
        }

        /* Triple drops */
        if (PP.getAbilityMode(AbilityType.GREEN_TERRA) && BlockChecks.canBeGreenTerra(mat)) {
            Herbalism.herbalismProcCheck(block, player, event, plugin);
        }

        if (Permissions.getInstance().herbalism(player) && BlockChecks.canBeGreenTerra(mat)) {
            Herbalism.herbalismProcCheck(block, player, event, plugin);
        }

        /*
         * MINING
         */

        if (Permissions.getInstance().mining(player) && BlockChecks.canBeSuperBroken(mat)) {
            if (Config.getInstance().getMiningRequiresTool() && ItemChecks.isMiningPick(inhand)) {
                Mining.miningBlockCheck(player, block);
            }
            else if (!Config.getInstance().getMiningRequiresTool()) {
                Mining.miningBlockCheck(player, block);
            }
        }

        /*
         * WOOD CUTTING
         */

        if (Permissions.getInstance().woodcutting(player) && mat.equals(Material.LOG)) {
            if (Config.getInstance().getWoodcuttingRequiresTool() && ItemChecks.isAxe(inhand)) {
                WoodCutting.woodcuttingBlockCheck(player, block);
            }
            else if (!Config.getInstance().getWoodcuttingRequiresTool()) {
                WoodCutting.woodcuttingBlockCheck(player, block);
            }
        }

        if (PP.getAbilityMode(AbilityType.TREE_FELLER) && Permissions.getInstance().treeFeller(player) && ItemChecks.isAxe(inhand)) {
            WoodCutting.treeFeller(event);
        }

        /*
         * EXCAVATION
         */

        if (BlockChecks.canBeGigaDrillBroken(mat) && Permissions.getInstance().excavation(player) && !block.hasMetadata("mcmmoPlacedBlock")) {
            if (Config.getInstance().getExcavationRequiresTool() && ItemChecks.isShovel(inhand)) {
                Excavation.excavationProcCheck(block, player);
            }
            else if (!Config.getInstance().getExcavationRequiresTool()) {
                Excavation.excavationProcCheck(block, player);
            }
        }

        //Remove metadata when broken
        if (block.hasMetadata("mcmmoPlacedBlock") && BlockChecks.shouldBeWatched(mat)) {
            block.removeMetadata("mcmmoPlacedBlock", plugin);
        }
    }

    /**
     * Monitor BlockDamage events.
     *
     * @param event The event to monitor
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        final int LEAF_BLOWER_LEVEL = 100;

        Player player = event.getPlayer();
        PlayerProfile PP = Users.getProfile(player);
        ItemStack inhand = player.getItemInHand();
        Block block = event.getBlock();
        Material mat = block.getType();

        /*
         * ABILITY PREPARATION CHECKS
         */
        if (BlockChecks.abilityBlockCheck(mat)) {
            if (PP.getToolPreparationMode(ToolType.HOE) && (BlockChecks.canBeGreenTerra(mat) || BlockChecks.makeMossy(mat))) {
                Skills.abilityCheck(player, SkillType.HERBALISM);
            }
            else if (PP.getToolPreparationMode(ToolType.AXE) && mat.equals(Material.LOG) && Permissions.getInstance().treeFeller(player)) {  //TODO: Why are we checking the permissions here?
                Skills.abilityCheck(player, SkillType.WOODCUTTING);
            }
            else if (PP.getToolPreparationMode(ToolType.PICKAXE) && BlockChecks.canBeSuperBroken(mat)) {
                Skills.abilityCheck(player, SkillType.MINING);
            }
            else if (PP.getToolPreparationMode(ToolType.SHOVEL) && BlockChecks.canBeGigaDrillBroken(mat)) {
                Skills.abilityCheck(player, SkillType.EXCAVATION);
            }
            else if (PP.getToolPreparationMode(ToolType.FISTS) && (BlockChecks.canBeGigaDrillBroken(mat) || mat.equals(Material.SNOW))) {
                Skills.abilityCheck(player, SkillType.UNARMED);
            }
        }

        /* TREE FELLER SOUNDS */
        if (Config.getInstance().spoutEnabled && mat.equals(Material.LOG) && PP.getAbilityMode(AbilityType.TREE_FELLER)) {
            SpoutSounds.playSoundForPlayer(SoundEffect.FIZZ, player, block.getLocation());
        }

        /*
         * ABILITY TRIGGER CHECKS
         */
        if (PP.getAbilityMode(AbilityType.GREEN_TERRA) && Permissions.getInstance().greenTerra(player) && BlockChecks.makeMossy(mat)) {
            Herbalism.greenTerra(player, block);
        }
        else if (PP.getAbilityMode(AbilityType.GIGA_DRILL_BREAKER) && Skills.triggerCheck(player, block, AbilityType.GIGA_DRILL_BREAKER)) {
            if (Config.getInstance().getExcavationRequiresTool() && ItemChecks.isShovel(inhand)) {
                event.setInstaBreak(true);
                Excavation.gigaDrillBreaker(player, block);
            }
            else if (!Config.getInstance().getExcavationRequiresTool()) {
                event.setInstaBreak(true);
                Excavation.gigaDrillBreaker(player, block);
            }
        }
        else if (PP.getAbilityMode(AbilityType.BERSERK) && Skills.triggerCheck(player, block, AbilityType.BERSERK)) {
            if (inhand.getType().equals(Material.AIR)) {
                FakePlayerAnimationEvent armswing = new FakePlayerAnimationEvent(player);
                plugin.getServer().getPluginManager().callEvent(armswing);

                event.setInstaBreak(true);
            }

            if (Config.getInstance().spoutEnabled) {
                SpoutSounds.playSoundForPlayer(SoundEffect.POP, player, block.getLocation());
            }
        }
        else if (PP.getAbilityMode(AbilityType.SUPER_BREAKER) && Skills.triggerCheck(player, block, AbilityType.SUPER_BREAKER)) {
            if(!player.getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH)) {  
                if (Config.getInstance().getMiningRequiresTool() && ItemChecks.isMiningPick(inhand)) {
                    event.setInstaBreak(true);
                    Mining.SuperBreakerBlockCheck(player, block);
                }
                else if (!Config.getInstance().getMiningRequiresTool()) {
                    event.setInstaBreak(true);
                    Mining.SuperBreakerBlockCheck(player, block);
                }
            }
        }
        else if (PP.getSkillLevel(SkillType.WOODCUTTING) >= LEAF_BLOWER_LEVEL && mat.equals(Material.LEAVES)) {
            if (Config.getInstance().getWoodcuttingRequiresTool() && ItemChecks.isAxe(inhand)) {
                if (Skills.triggerCheck(player, block, AbilityType.LEAF_BLOWER)) {
                    event.setInstaBreak(true);
                    WoodCutting.leafBlower(player, block);
                }
            }
            else if (!Config.getInstance().getWoodcuttingRequiresTool() && !inhand.getType().equals(Material.SHEARS)) {
                if (Skills.triggerCheck(player, block, AbilityType.LEAF_BLOWER)) {
                    event.setInstaBreak(true);
                    WoodCutting.leafBlower(player, block);
                }
            }
        }
    }
}
