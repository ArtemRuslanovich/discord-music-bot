package com.discord.bot;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Bot {
    public static void main(String[] args) throws Exception {
        JDABuilder.createDefault("y")
            .addEventListeners(new MusicBot())
            .enableIntents(GatewayIntent.MESSAGE_CONTENT) 
            .build();
    }
}