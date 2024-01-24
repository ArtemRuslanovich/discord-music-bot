package com.discord.bot;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Bot {
    public static void main(String[] args) throws Exception {
        JDABuilder.createDefault("MTE5ODIwMTU3MDY1MTgyMDA3NA.G3N2Ks.YSXPnt8myUs8S2ds7NdeVh9eijmkSUGMftPTD4")
            .addEventListeners(new MusicBot())
            .enableIntents(GatewayIntent.MESSAGE_CONTENT) 
            .build();
    }
}