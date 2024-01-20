package com.discord.bot;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Bot {
    public static void main(String[] args) throws Exception {
        JDABuilder.createDefault("MTE5ODIwMTU3MDY1MTgyMDA3NA.Gqeig5.FWsn8-at87mf2E7KKxM-sTKmsxenu1UVxOuJAM")
            .addEventListeners(new MusicBot())
            .enableIntents(GatewayIntent.MESSAGE_CONTENT) 
            .build();
    }
}