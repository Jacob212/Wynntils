/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.core.framework.instances.data;

import com.wynntils.McIf;
import com.wynntils.core.framework.enums.ClassType;
import com.wynntils.core.framework.instances.containers.PlayerData;
import com.wynntils.core.framework.instances.containers.UnprocessedAmount;
import com.wynntils.core.utils.ItemUtils;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InventoryData extends PlayerData {

    private static final Pattern UNPROCESSED_NAME_REGEX = Pattern.compile("^§fUnprocessed [a-zA-Z ]+§8 \\[(?:0|[1-9][0-9]*)/([1-9][0-9]*)]$");
    private static final Pattern UNPROCESSED_LORE_REGEX = Pattern.compile("^§7Unprocessed Material \\[Weight: ([1-9][0-9]*)]$");

    public InventoryData() { }

    /**
     * @return The number of free slots in the user's inventory
     *
     * -1 if unable to determine
     */
    public int getFreeInventorySlots() {
        ClientPlayerEntity player = getPlayer();
        ClassType currentClass = get(CharacterData.class).getCurrentClass();

        if (currentClass == ClassType.NONE || player == null) return -1;
        return (int) player.inventory.items.stream().filter(ItemStack::isEmpty).count();
    }

    /**
     * @return The amount of items inside the players ingredient pouch (parsed from the items lore)
     * If countSlotsOnly is true, it only counts the number of used slots
     *
     * -1 if unable to determine
     */
    public int getIngredientPouchCount(boolean countSlotsOnly) {
        ClientPlayerEntity player = getPlayer();
        ClassType currentClass = get(CharacterData.class).getCurrentClass();

        if (currentClass == ClassType.NONE || player == null) return -1;
        ItemStack pouch = player.inventory.items.get(13);
        int count = 0;

        List<String> lore = ItemUtils.getLore(pouch);

        for (int i = 4; i < lore.size(); i++) {
            String line = McIf.getTextWithoutFormattingCodes(lore.get(i));

            int end = line.indexOf(" x ");

            if (end == -1) break;

            if (countSlotsOnly) {
                count++;
            } else {
                line = line.substring(0, end);
                count = count + Integer.parseInt(line);
            }
        }

        return count;
    }

    /**
     * @return UnprocessedAmount((total weight of unprocessed materials), (maximum weight that can be held)).
     *
     * If there are no unprocessed materials, maximum will be -1.
     */
    public UnprocessedAmount getUnprocessedAmount() {
        ClientPlayerEntity player = getPlayer();
        if (player == null) return new UnprocessedAmount(0, 0);

        int maximum = -1;
        int amount = 0;

        for (int i = 0, len = player.inventory.getContainerSize(); i < len; i++) {
            ItemStack it = player.inventory.getItem(i);
            if (it.isEmpty()) continue;

            Matcher nameMatcher = UNPROCESSED_NAME_REGEX.matcher(McIf.toText(it.getDisplayName()));
            if (!nameMatcher.matches()) continue;

            ListNBT lore = ItemUtils.getLoreTag(it);
            if (lore == null || lore.size() == 0) continue;

            Matcher loreMatcher = UNPROCESSED_LORE_REGEX.matcher(lore.getString(0));
            if (!loreMatcher.matches()) continue;

            // Found an unprocessed item
            if (maximum == -1) {
                maximum = Integer.parseInt(nameMatcher.group(1));
            }

            amount += Integer.parseInt(loreMatcher.group(1)) * it.getCount();
        }

        return new UnprocessedAmount(amount, maximum);
    }

    /**
     * @return Total number of health potions in inventory
     */
    public int getHealthPotions() {
        ClientPlayerEntity player = getPlayer();
        if (player == null) return 0;

        NonNullList<ItemStack> contents = player.inventory.items;

        int count = 0;

        for (ItemStack item : contents) {
            if (!item.isEmpty() && item.hasCustomHoverName() && McIf.toText(item.getDisplayName()).contains("Potion of Healing")) {
                count++;
            }
        }

        return count;
    }

    /**
     * @return Total number of mana potions in inventory
     */
    public int getManaPotions() {
        ClientPlayerEntity player = getPlayer();
        if (player == null) return 0;

        NonNullList<ItemStack> contents = player.inventory.items;

        int count = 0;

        for (ItemStack item : contents) {
            if (!item.isEmpty() && item.hasCustomHoverName() && McIf.toText(item.getDisplayName()).contains("Potion of Mana")) {
                count++;
            }
        }

        return count;
    }

    /**
     * @return Total number of emeralds in inventory (Including blocks and LE)
     */
    public int getMoney() {
        ClientPlayerEntity player = getPlayer();
        if (player == null) return 0;

        return ItemUtils.countMoney(player.inventory);
    }

    /**
     * @return The maximum number of soul points the current player can have
     *
     * Note: If veteran, this should always be 15, but currently might return the wrong value
     */
    public int getMaxSoulPoints() {
        int maxIfNotVeteran = 10 + MathHelper.clamp(get(CharacterData.class).getLevel() / 15, 0, 5);
        if (getSoulPoints() > maxIfNotVeteran) {
            return 15;
        }
        return maxIfNotVeteran;
    }

    /**
     * @return The current number of soul points the current player has
     *
     * -1 if unable to determine
     */
    public int getSoulPoints() {
        ClientPlayerEntity player = getPlayer();
        ClassType currentClass = get(CharacterData.class).getCurrentClass();
        if (currentClass == ClassType.NONE || player == null) return -1;

        ItemStack soulPoints = player.inventory.items.get(8);
        if (soulPoints.getItem() != Items.NETHER_STAR && soulPoints.getItem() != Item.byBlock(Blocks.SNOW)) {
            return -1;
        }

        return soulPoints.getCount();
    }

    /**
     * @return Time in game ticks (1/20th of a second, 50ms) until next soul point
     *
     * -1 if unable to determine
     *
     * Also check that {@code {@link #getMaxSoulPoints()} >= {@link #getSoulPoints()}},
     * in which case soul points are already full
     */
    public int getTicksToNextSoulPoint() {
        ClientPlayerEntity player = getPlayer();
        ClassType currentClass = get(CharacterData.class).getCurrentClass();

        if (currentClass == ClassType.NONE || player.level == null) return -1;
        int ticks = ((int) (player.level.getGameTime() % 24000) + 24000) % 24000;

        return ((24000 - ticks) % 24000);
    }

}
