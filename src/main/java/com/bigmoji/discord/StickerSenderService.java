package com.bigmoji.discord;

import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import org.springframework.stereotype.Service;

@Service
public class StickerSenderService {
  public void send(MessageChannelUnion channel, String imageUrl) {
    channel.sendMessage(imageUrl).queue();
  }
}
