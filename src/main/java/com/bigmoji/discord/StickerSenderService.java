package com.bigmoji.discord;

import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.utils.FileUpload;
import org.springframework.stereotype.Service;

@Service
public class StickerSenderService {

  /**
   * Sends a sticker file as a Discord attachment.
   *
   * @param channel the target Discord channel
   * @param bytes the sticker file data
   * @param fileName the filename for the attachment (e.g., "sticker.png")
   */
  public void send(MessageChannelUnion channel, byte[] bytes, String fileName) {
    channel.sendFiles(FileUpload.fromData(bytes, fileName)).queue();
  }
}
