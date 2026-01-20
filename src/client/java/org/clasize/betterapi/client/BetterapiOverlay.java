package org.clasize.betterapi.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class BetterapiOverlay implements HudRenderCallback {

    @Override
    public void onHudRender(DrawContext context, float tickDelta) {
        if (NetworkManager.lastError == null) return;
        
        long timeSinceError = System.currentTimeMillis() - NetworkManager.lastErrorTime;
        if (timeSinceError > 10000) { // Show for 10 seconds
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        String errorText = "[BetterAPI] " + NetworkManager.lastError;
        int width = textRenderer.getWidth(errorText);
        int infoWidth = textRenderer.getWidth("[BetterAPI] ");
        
        int x = client.getWindow().getScaledWidth() - width - 10;
        int y = 10;

        // Draw background (optional, but makes it readable)
        context.fill(x - 2, y - 2, x + width + 2, y + 10, 0x80000000); // Semi-transparent black

        // Draw text
        // "BetterAPI" in Red, rest in White or Red
        context.drawTextWithShadow(textRenderer, Text.of("§c[BetterAPI] §f" + NetworkManager.lastError), x, y, 0xFFFFFF);
    }
}
