package com.discord.bot;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Bot {
    public static void main(String[] args) throws Exception {
        JDABuilder.createDefault("MTE5ODIwMTU3MDY1MTgyMDA3NA.GAzWsf.XO4ADT756g4BTFmUF3UyPYIAcBiHRJlYkq_IiU")
            .addEventListeners(new MusicBot())
            .enableIntents(GatewayIntent.MESSAGE_CONTENT) 
            .build();
    }
}