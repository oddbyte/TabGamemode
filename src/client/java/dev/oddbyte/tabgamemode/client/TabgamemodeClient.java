/*
    * Author: Oddbyte
    * Description: I liked the idea of Meteor client's Better tab module,
    * specifically the part where it shows player gamemodes in tab.
    * I don't know why no one else has made a standalone mod for this,
    * but here is mine :>
 */

package dev.oddbyte.tabgamemode.client;

// Imports:
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.GameType;
import java.util.Collection;

// Main class:
public class TabgamemodeClient implements ClientModInitializer {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final String GAMEMODE_TAG_PATTERN = "§0§7\\[(S|C|A|Sp)]§r ";

    @Override
    public void onInitializeClient() {
        // Use HudRenderCallback to update the tab list regularly, but only if the tab GUI is shown
        HudRenderCallback.EVENT.register((matrixStack, delta) -> {
            if (mc.options.keyPlayerList.isDown()) { // Check if the tab key (player list) is pressed
                updateTabList();
            }
        });
    }

    private void updateTabList() {
        if (mc.player == null || mc.level == null) return; // Stop crashing because the player is null.

        Collection<PlayerInfo> entries = mc.player.connection.getOnlinePlayers();
        for (PlayerInfo entry : entries) {
            // Update the display name with the gamemode
            Component displayName = getPlayerName(entry);
            entry.setTabListDisplayName(displayName);
        }
    }

    private Component getPlayerName(PlayerInfo playerInfo) {
        Component originalName = playerInfo.getTabListDisplayName();
        String nameString = originalName != null ? originalName.getString() : playerInfo.getProfile().getName();

        // Remove any existing gamemode tag if present
        nameString = nameString.replaceAll(GAMEMODE_TAG_PATTERN, "");

        GameType gameType = playerInfo.getGameMode();
        String gmText = "?";
        if (gameType != null) {
            gmText = switch (gameType) {
                case SPECTATOR -> "Sp";
                case SURVIVAL -> "S";
                case CREATIVE -> "C";
                case ADVENTURE -> "A";
                default -> "?"; // I don't think that this is possible, but I will put this here just in case.
            };
        }

        // Using Minecraft color codes, §7 is gray, §r resets the color, and the §0 is there to make sure that it doesn't interfere with plugins that do the same thing.
        String formattedGmTag = "§0§7[" + gmText + "]§r ";

        // Append the gamemode tag before the original name
        MutableComponent displayName = Component.literal(formattedGmTag)
                .append(Component.literal(nameString).setStyle(Style.EMPTY));

        return displayName;
    }
}
