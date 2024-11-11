package dev.oddbyte.tabgamemodes.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TabgamemodesClient implements ClientModInitializer {

    private static final Pattern GAME_MODE_PATTERN = Pattern.compile("§7\\[(Sp|S|C|A)] §r");

    @Override
    public void onInitializeClient() {
        // Register a tick event to periodically update the tab names
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);
    }

    private void onEndClientTick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return;
        }

        // Iterate over the player list and modify the display names
        for (PlayerListEntry entry : client.getNetworkHandler().getPlayerList()) {
            GameMode currentGameMode = entry.getGameMode();
            if (currentGameMode != null) {
                String gmText = getGameModeAbbreviation(currentGameMode);

                // Construct the game mode prefix with color codes
                MutableText gmPrefix = Text.literal("§7[" + gmText + "] §r");

                // Retrieve the current display name
                Text currentName = entry.getDisplayName();
                if (currentName == null) {
                    currentName = Text.literal(entry.getProfile().getName());
                }

                // Remove any existing game mode tags from the current Text
                Text sanitizedText = removeGameModeTags(currentName);

                // Combine the game mode prefix with the sanitized display name
                MutableText updatedName = gmPrefix.append(sanitizedText);

                // Set the updated display name
                entry.setDisplayName(updatedName);
            }
        }
    }

    private Text removeGameModeTags(Text text) {
        // Create a new MutableText to store sanitized components
        MutableText sanitizedText = Text.empty();

        // Traverse each sibling component of the Text object
        for (Text sibling : text.getSiblings()) {
            // Convert the sibling to a string and check for game mode tags
            String siblingString = sibling.getString();
            Matcher matcher = GAME_MODE_PATTERN.matcher(siblingString);

            // If the pattern is found, remove the game mode tag
            if (matcher.find()) {
                // Remove the tag and create a new Text component with the same style
                String sanitizedString = matcher.replaceAll("");
                MutableText sanitizedComponent = Text.literal(sanitizedString).setStyle(sibling.getStyle());
                sanitizedText.append(sanitizedComponent);
            } else {
                // If no pattern is found, append the sibling as is
                sanitizedText.append(sibling);
            }
        }

        // If the original text has no siblings, check the main text content itself
        if (text.getSiblings().isEmpty()) {
            String mainString = text.getString();
            Matcher matcher = GAME_MODE_PATTERN.matcher(mainString);

            if (matcher.find()) {
                String sanitizedString = matcher.replaceAll("");
                return Text.literal(sanitizedString).setStyle(text.getStyle());
            } else {
                return text;
            }
        }

        // Return the sanitized Text object
        return sanitizedText.setStyle(text.getStyle());
    }

    private String getGameModeAbbreviation(GameMode gm) {
        if (gm == null) {
            return "?";
        }
        return switch (gm) {
            case SPECTATOR -> "Sp";
            case SURVIVAL -> "S";
            case CREATIVE -> "C";
            case ADVENTURE -> "A";
        };
    }
}
