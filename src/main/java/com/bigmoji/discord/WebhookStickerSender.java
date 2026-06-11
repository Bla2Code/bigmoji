package com.bigmoji.discord;

import java.util.concurrent.ConcurrentHashMap;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Sends sticker files via Discord webhooks to impersonate the original message author.
 *
 * <p>Creates or reuses a webhook per channel, caches it in memory, and falls back to sending under
 * the bot's own name if webhook operations fail.
 */
@Service
public class WebhookStickerSender {

  private static final Logger log = LoggerFactory.getLogger(WebhookStickerSender.class);
  private static final String WEBHOOK_NAME = "Bigmoji Sticker Bot";
  private static final long DISCORD_FILE_SIZE_LIMIT = 8 * 1024 * 1024; // 8MB

  private final ConcurrentHashMap<String, net.dv8tion.jda.api.entities.Webhook> webhookCache =
      new ConcurrentHashMap<>();

  /**
   * Sends a sticker file via webhook impersonating the original message author.
   *
   * @param channel the target Discord channel
   * @param stickerBytes the sticker file data
   * @param fileName the filename for the attachment (e.g., "sticker.png")
   * @param authorName the original message author's display name
   * @param authorAvatarUrl the original message author's avatar URL
   * @return true if sent via webhook, false if fell back to bot name
   */
  public boolean sendAsAuthor(
      MessageChannelUnion channel,
      byte[] stickerBytes,
      String fileName,
      String authorName,
      String authorAvatarUrl) {

    if (stickerBytes.length > DISCORD_FILE_SIZE_LIMIT) {
      log.warn(
          "Sticker file too large for Discord: sizeBytes={}, limitBytes={}, channel={}",
          stickerBytes.length,
          DISCORD_FILE_SIZE_LIMIT,
          channel.getId());
      return false;
    }

    String channelId = channel.getId();
    String sanitizedName = WebhookUsernameSanitizer.sanitize(authorName);

    try {
      net.dv8tion.jda.api.entities.Webhook webhook = getOrCreateWebhook(channel, channelId);
      webhook
          .sendFiles(FileUpload.fromData(stickerBytes, fileName))
          .setUsername(sanitizedName)
          .setAvatarUrl(authorAvatarUrl)
          .queue();
      log.debug(
          "Sticker sent via webhook: channel={}, author={}, fileName={}, sizeBytes={}",
          channelId,
          sanitizedName,
          fileName,
          stickerBytes.length);
      return true;
    } catch (PermissionException e) {
      log.warn(
          "Webhook permission denied, falling back to bot name: channel={}, reason={}",
          channelId,
          e.getMessage());
      return false;
    } catch (ErrorResponseException e) {
      if (e.getErrorResponse() == ErrorResponse.UNKNOWN_WEBHOOK
          || e.getErrorResponse() == ErrorResponse.MISSING_PERMISSIONS) {
        log.warn(
            "Webhook error, invalidating cache and falling back: channel={}, error={}",
            channelId,
            e.getErrorResponse());
        webhookCache.remove(channelId);
        return false;
      }
      log.warn(
          "Webhook error response, falling back to bot name: channel={}, error={}",
          channelId,
          e.getErrorResponse());
      return false;
    } catch (Exception e) {
      log.warn(
          "Webhook send failed, falling back to bot name: channel={}, reason={}",
          channelId,
          e.getMessage());
      return false;
    }
  }

  private net.dv8tion.jda.api.entities.Webhook getOrCreateWebhook(
      MessageChannelUnion channel, String channelId) {
    net.dv8tion.jda.api.entities.Webhook cached = webhookCache.get(channelId);
    if (cached != null) {
      return cached;
    }

    log.debug("Creating webhook for channel: {}", channelId);

    // MessageChannelUnion doesn't have createWebhook(), need to use TextChannel
    if (!(channel instanceof TextChannel textChannel)) {
      throw new IllegalStateException("Webhooks can only be created in text channels");
    }

    net.dv8tion.jda.api.entities.Webhook webhook =
        textChannel.createWebhook(WEBHOOK_NAME).complete();
    webhookCache.put(channelId, webhook);
    return webhook;
  }
}
