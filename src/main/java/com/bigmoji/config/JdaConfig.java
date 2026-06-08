package com.bigmoji.config;

import com.bigmoji.discord.EmojiMessageListener;
import jakarta.annotation.Nullable;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JdaConfig {
  @Bean
  @Nullable
  JDA jda(@Value("${bigmoji.discord.token:}") String token, EmojiMessageListener listener) throws InterruptedException {
    if (token == null || token.isBlank()) {
      return null;
    }
    return JDABuilder.createDefault(token)
        .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
        .addEventListeners(listener)
        .build()
        .awaitReady();
  }
}
