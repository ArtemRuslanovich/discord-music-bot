package com.discord.bot;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Bot {
    public static void main(String[] args) throws Exception {
        JDABuilder.createDefault("MTE5ODIwMTU3MDY1MTgyMDA3NA.Gj-P4K.oBU3oGpWe9YboKkR6Oe4ASgcBuFWCEApSrJIyk")
            .addEventListeners(new MusicBot())
            .enableIntents(GatewayIntent.MESSAGE_CONTENT) 
            .build();
    }
}