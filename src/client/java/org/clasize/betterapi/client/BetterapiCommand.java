package org.clasize.betterapi.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class BetterapiCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(literal("betterapi")
            .executes(BetterapiCommand::runStatus)
        );
    }

    private static int runStatus(CommandContext<FabricClientCommandSource> context) {
        String status = NetworkManager.lastStatus;
        long lastSuccess = NetworkManager.lastSuccessTime;
        long timeAgo = (System.currentTimeMillis() - lastSuccess) / 1000;
        
        context.getSource().sendFeedback(Text.of("§8[§bBetterAPI§8] §7Durum: " + status));
        if (lastSuccess > 0) {
            context.getSource().sendFeedback(Text.of("§8[§bBetterAPI§8] §7Son Başarılı Bağlantı: §a" + timeAgo + " sn önce"));
        } else {
             context.getSource().sendFeedback(Text.of("§8[§bBetterAPI§8] §7Hiç bağlantı kurulamadı."));
        }
        
        return 1;
    }
}
