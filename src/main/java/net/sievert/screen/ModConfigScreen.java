package net.sievert.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.sievert.Config;

import java.util.function.Consumer;

/**
 * A simple config screen for Mod Menu integration.
 * Allows users to modify the block break speed and debug logging settings.
 */
public class ModConfigScreen extends Screen {
    private final Screen parent;
    private final Config config;
    
    private ButtonWidget speedButton;
    private ButtonWidget debugButton;
    
    // Speed presets
    private static final double[] SPEED_PRESETS = {0.5, 0.75, 1.0, 1.25, 1.5, 2.0, 3.0, 5.0};
    private int currentPresetIndex;

    public ModConfigScreen(Screen parent, Config config) {
        super(Text.literal("Simple Breaking Speed Modifier - Config"));
        this.parent = parent;
        this.config = config;
        
        // Find current preset index
        for (int i = 0; i < SPEED_PRESETS.length; i++) {
            if (Math.abs(SPEED_PRESETS[i] - config.playerBlockBreakSpeedBase) < 0.001) {
                currentPresetIndex = i;
                break;
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        
        int centerX = width / 2;
        int centerY = height / 2;
        
        // Title
        var titleWidget = new net.minecraft.client.gui.widget.TextWidget(
            Text.literal("Simple Breaking Speed Modifier Configuration"),
            textRenderer
        );
        titleWidget.setPosition(centerX - 150, centerY - 50);
        addDrawableChild(titleWidget);
        
        // Current speed display
        var speedDisplay = new net.minecraft.client.gui.widget.TextWidget(
            Text.literal("Current Speed: " + String.format("%.2f", config.playerBlockBreakSpeedBase) + " (" + describeSpeed(config.playerBlockBreakSpeedBase) + ")"),
            textRenderer
        );
        speedDisplay.setPosition(centerX - 150, centerY - 20);
        addDrawableChild(speedDisplay);
        
        // Speed adjustment button
        speedButton = ButtonWidget.builder(
            Text.literal("Set Speed: " + String.format("%.2f", SPEED_PRESETS[currentPresetIndex])),
            button -> cycleSpeed()
        )
        .dimensions(centerX - 100, centerY + 20, 200, 20)
        .build();
        addDrawableChild(speedButton);
        
        // Debug toggle button
        debugButton = ButtonWidget.builder(
            config.enableDebugLogging ? Text.literal("Debug Logging: ON") : Text.literal("Debug Logging: OFF"),
            button -> toggleDebug()
        )
        .dimensions(centerX - 100, centerY + 50, 200, 20)
        .build();
        addDrawableChild(debugButton);
        
        // Done button
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Done"),
            button -> close()
        )
        .dimensions(centerX - 100, centerY + 90, 200, 20)
        .build());
    }
    
    private void cycleSpeed() {
        currentPresetIndex = (currentPresetIndex + 1) % SPEED_PRESETS.length;
        config.playerBlockBreakSpeedBase = SPEED_PRESETS[currentPresetIndex];
        speedButton.setMessage(Text.literal("Set Speed: " + String.format("%.2f", SPEED_PRESETS[currentPresetIndex])));
        
        // Re-initialize the screen to update displays
        clearChildren();
        init();
    }
    
    private void toggleDebug() {
        config.enableDebugLogging = !config.enableDebugLogging;
        debugButton.setMessage(config.enableDebugLogging ? Text.literal("Debug Logging: ON") : Text.literal("Debug Logging: OFF"));
    }
    
    private String describeSpeed(double value) {
        if (value == 1.0) {
            return "default";
        }
        double percent = Math.abs((value - 1.0) * 100.0);
        if (value > 1.0) {
            return String.format("%.0f%% faster", percent);
        } else {
            return String.format("%.0f%% slower", percent);
        }
    }
    
    @Override
    public void close() {
        // Save config when closing
        try {
            java.nio.file.Files.createDirectories(Config.CONFIG_PATH.getParent());
            java.nio.file.Files.writeString(Config.CONFIG_PATH, Config.GSON.toJson(config));
            
            // Reload config and apply to current player if in singleplayer
            Config.load();
            if (client != null && client.player != null) {
                var attr = client.player.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.PLAYER_BLOCK_BREAK_SPEED);
                if (attr != null) {
                    attr.setBaseValue(Config.INSTANCE.playerBlockBreakSpeedBase);
                    if (Config.INSTANCE.enableDebugLogging) {
                        net.sievert.SimpleBreakingSpeedModifier.LOGGER.info("Applied new block break speed: {}", Config.INSTANCE.playerBlockBreakSpeedBase);
                    }
                }
            }
        } catch (Exception e) {
            // Silently fail on save error
        }
        assert client != null;
        client.setScreen(parent);
    }
}
