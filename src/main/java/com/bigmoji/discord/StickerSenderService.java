package com.bigmoji.discord;

import net.dv8tion.jda.api.entities.MessageChannel;
import org.springframework.stereotype.Service;

@Service
public class StickerSenderService {
  public void send(MessageChannel channel, String imageUrl) {
    channel.sendMessage(imageUrl).queue();
  }
}
