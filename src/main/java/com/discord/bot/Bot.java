package com.discord.bot;

import net.dv8tion.jda.api.JDABuilder;

public class Bot {
    public static void main(String[] args) throws Exception {
        JDABuilder.createDefault("YOUR_BOT_TOKEN_HERE")
            .addEventListeners(new MyListener()) // Замените MyListener на ваш собственный класс для обработки событий
            .build();
    }
}