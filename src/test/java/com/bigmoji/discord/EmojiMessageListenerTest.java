package com.bigmoji.discord;

import static org.mockito.Mockito.*;

import com.bigmoji.emoji.EmojiDetector;
import com.bigmoji.sticker.StickerAsset;
import com.bigmoji.sticker.StickerMappingService;
import java.util.Optional;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EmojiMessageListenerTest {

  private WebhookStickerSender mockWebhookSender() {
    return mock(WebhookStickerSender.class);
  }

  @Test
  void ignoresInvalidMessage() {
    EmojiDetector detector = mock(EmojiDetector.class);
    StickerMappingService mappingService = mock(StickerMappingService.class);
    StickerSenderService sender = mock(StickerSenderService.class);
    WebhookStickerSender webhookSender = mockWebhookSender();
    EmojiMessageListener listener =
        new EmojiMessageListener(detector, mappingService, sender, webhookSender);

    MessageReceivedEvent event = mock(MessageReceivedEvent.class);
    Message message = mock(Message.class);
    User user = mock(User.class);

    MessageChannelUnion channel = mock(MessageChannelUnion.class);

    when(event.getAuthor()).thenReturn(user);
    when(user.isBot()).thenReturn(false);
    when(event.isFromGuild()).thenReturn(false);
    when(event.getChannel()).thenReturn(channel);
    when(channel.getId()).thenReturn("ch-1");
    when(event.getMessage()).thenReturn(message);
    when(message.getContentRaw()).thenReturn("hello");
    when(detector.isSingleEmojiMessage("hello")).thenReturn(false);

    listener.onMessageReceived(event);

    verify(detector).isSingleEmojiMessage("hello");
    verifyNoInteractions(mappingService, sender, webhookSender);
  }

  @Test
  void ignoresBotMessage() {
    EmojiDetector detector = mock(EmojiDetector.class);
    StickerMappingService mappingService = mock(StickerMappingService.class);
    StickerSenderService sender = mock(StickerSenderService.class);
    WebhookStickerSender webhookSender = mockWebhookSender();
    EmojiMessageListener listener =
        new EmojiMessageListener(detector, mappingService, sender, webhookSender);

    MessageReceivedEvent event = mock(MessageReceivedEvent.class);
    Message message = mock(Message.class);
    User user = mock(User.class);

    MessageChannelUnion channel = mock(MessageChannelUnion.class);

    when(event.getAuthor()).thenReturn(user);
    when(user.isBot()).thenReturn(true);
    when(event.isFromGuild()).thenReturn(false);
    when(event.getChannel()).thenReturn(channel);
    when(channel.getId()).thenReturn("ch-1");
    when(event.getMessage()).thenReturn(message);
    when(message.getContentRaw()).thenReturn("😊");

    listener.onMessageReceived(event);

    verifyNoInteractions(detector, mappingService, sender, webhookSender);
  }

  @Test
  void sendsReplacementWhenMappingExists() throws Exception {
    EmojiDetector detector = mock(EmojiDetector.class);
    StickerMappingService mappingService = mock(StickerMappingService.class);
    StickerSenderService sender = mock(StickerSenderService.class);
    WebhookStickerSender webhookSender = mockWebhookSender();
    EmojiMessageListener listener =
        new EmojiMessageListener(detector, mappingService, sender, webhookSender);

    MessageReceivedEvent event = mock(MessageReceivedEvent.class);
    Message message = mock(Message.class);
    User user = mock(User.class);
    Guild guild = mock(Guild.class);
    MessageChannelUnion channel = mock(MessageChannelUnion.class);

    byte[] stickerBytes = new byte[] {1, 2, 3};
    StickerAsset asset = new StickerAsset("sticker.png", stickerBytes, false, "o.png");

    when(event.getAuthor()).thenReturn(user);
    when(user.isBot()).thenReturn(false);
    when(user.getName()).thenReturn("Alice");
    when(user.getEffectiveAvatarUrl()).thenReturn("http://avatar.url");
    when(event.getMessage()).thenReturn(message);
    when(message.getContentRaw()).thenReturn("😊");
    when(detector.isSingleEmojiMessage("😊")).thenReturn(true);
    when(detector.normalize("😊")).thenReturn("smile");
    when(event.isFromGuild()).thenReturn(true);
    when(event.getGuild()).thenReturn(guild);
    when(guild.getId()).thenReturn("1");
    when(mappingService.resolveSticker("1", "smile")).thenReturn(Optional.of(asset));
    when(event.getChannel()).thenReturn(channel);
    when(channel.getId()).thenReturn("ch-2");
    when(webhookSender.sendAsAuthor(
            eq(channel), eq(stickerBytes), eq("sticker.png"), eq("Alice"), eq("http://avatar.url")))
        .thenReturn(true);

    listener.onMessageReceived(event);

    verify(webhookSender)
        .sendAsAuthor(
            eq(channel), eq(stickerBytes), eq("sticker.png"), eq("Alice"), eq("http://avatar.url"));
    verify(sender, never()).send(any(), any(byte[].class), anyString());
    verify(message).delete();
    verify(mappingService).resolveSticker("1", "smile");
  }

  @ParameterizedTest
  @CsvSource({"😇,innocent", "😄,smile", "😬,grimacing"})
  void resolvesDiscordUnicodeThroughCanonicalShortcode(String unicode, String shortcode)
      throws Exception {
    EmojiDetector detector = new EmojiDetector();
    StickerMappingService mappingService = mock(StickerMappingService.class);
    StickerSenderService sender = mock(StickerSenderService.class);
    WebhookStickerSender webhookSender = mockWebhookSender();
    EmojiMessageListener listener =
        new EmojiMessageListener(detector, mappingService, sender, webhookSender);

    MessageReceivedEvent event = mock(MessageReceivedEvent.class);
    Message message = mock(Message.class);
    User user = mock(User.class);
    Guild guild = mock(Guild.class);
    MessageChannelUnion channel = mock(MessageChannelUnion.class);
    byte[] stickerBytes = new byte[] {1, 2, 3};
    StickerAsset asset = new StickerAsset("sticker.png", stickerBytes, false, "custom.png");

    when(event.getAuthor()).thenReturn(user);
    when(user.isBot()).thenReturn(false);
    when(user.getName()).thenReturn("Alice");
    when(user.getEffectiveAvatarUrl()).thenReturn("http://avatar.url");
    when(event.getMessage()).thenReturn(message);
    when(message.getContentRaw()).thenReturn(unicode);
    when(event.isFromGuild()).thenReturn(true);
    when(event.getGuild()).thenReturn(guild);
    when(guild.getId()).thenReturn("guild-1");
    when(event.getChannel()).thenReturn(channel);
    when(channel.getId()).thenReturn("channel-1");
    when(mappingService.resolveSticker("guild-1", shortcode)).thenReturn(Optional.of(asset));
    when(webhookSender.sendAsAuthor(
            eq(channel), eq(stickerBytes), eq("sticker.png"), eq("Alice"), eq("http://avatar.url")))
        .thenReturn(true);

    listener.onMessageReceived(event);

    verify(mappingService).resolveSticker("guild-1", shortcode);
    verify(webhookSender)
        .sendAsAuthor(
            eq(channel), eq(stickerBytes), eq("sticker.png"), eq("Alice"), eq("http://avatar.url"));
  }

  @Test
  void fallsBackToDirectSendWhenWebhookFails() throws Exception {
    EmojiDetector detector = mock(EmojiDetector.class);
    StickerMappingService mappingService = mock(StickerMappingService.class);
    StickerSenderService sender = mock(StickerSenderService.class);
    WebhookStickerSender webhookSender = mockWebhookSender();
    EmojiMessageListener listener =
        new EmojiMessageListener(detector, mappingService, sender, webhookSender);

    MessageReceivedEvent event = mock(MessageReceivedEvent.class);
    Message message = mock(Message.class);
    User user = mock(User.class);
    Guild guild = mock(Guild.class);
    MessageChannelUnion channel = mock(MessageChannelUnion.class);

    byte[] stickerBytes = new byte[] {1, 2, 3};
    StickerAsset asset = new StickerAsset("sticker.png", stickerBytes, false, "o.png");

    when(event.getAuthor()).thenReturn(user);
    when(user.isBot()).thenReturn(false);
    when(user.getName()).thenReturn("Alice");
    when(user.getEffectiveAvatarUrl()).thenReturn("http://avatar.url");
    when(event.getMessage()).thenReturn(message);
    when(message.getContentRaw()).thenReturn("😊");
    when(detector.isSingleEmojiMessage("😊")).thenReturn(true);
    when(detector.normalize("😊")).thenReturn("smile");
    when(event.isFromGuild()).thenReturn(true);
    when(event.getGuild()).thenReturn(guild);
    when(guild.getId()).thenReturn("1");
    when(mappingService.resolveSticker("1", "smile")).thenReturn(Optional.of(asset));
    when(event.getChannel()).thenReturn(channel);
    when(channel.getId()).thenReturn("ch-2");
    when(webhookSender.sendAsAuthor(any(), any(), anyString(), anyString(), anyString()))
        .thenReturn(false);

    listener.onMessageReceived(event);

    verify(webhookSender).sendAsAuthor(any(), any(), anyString(), anyString(), anyString());
    verify(sender).send(eq(channel), eq(stickerBytes), eq("sticker.png"));
    verify(message).delete();
  }

  @Test
  void usesGuildMemberEffectiveNameForWebhookAuthor() throws Exception {
    EmojiDetector detector = mock(EmojiDetector.class);
    StickerMappingService mappingService = mock(StickerMappingService.class);
    StickerSenderService sender = mock(StickerSenderService.class);
    WebhookStickerSender webhookSender = mockWebhookSender();
    EmojiMessageListener listener =
        new EmojiMessageListener(detector, mappingService, sender, webhookSender);

    MessageReceivedEvent event = mock(MessageReceivedEvent.class);
    Message message = mock(Message.class);
    User user = mock(User.class);
    Member member = mock(Member.class);
    Guild guild = mock(Guild.class);
    MessageChannelUnion channel = mock(MessageChannelUnion.class);

    byte[] stickerBytes = new byte[] {1, 2, 3};
    StickerAsset asset = new StickerAsset("sticker.png", stickerBytes, false, "o.png");

    when(event.getAuthor()).thenReturn(user);
    when(user.isBot()).thenReturn(false);
    when(user.getName()).thenReturn(".ne_tort БОТ");
    when(user.getEffectiveAvatarUrl()).thenReturn("http://avatar.url");
    when(event.getMember()).thenReturn(member);
    when(member.getEffectiveName()).thenReturn("Ne_Tort");
    when(event.getMessage()).thenReturn(message);
    when(message.getContentRaw()).thenReturn("😊");
    when(detector.isSingleEmojiMessage("😊")).thenReturn(true);
    when(detector.normalize("😊")).thenReturn("smile");
    when(event.isFromGuild()).thenReturn(true);
    when(event.getGuild()).thenReturn(guild);
    when(guild.getId()).thenReturn("1");
    when(mappingService.resolveSticker("1", "smile")).thenReturn(Optional.of(asset));
    when(event.getChannel()).thenReturn(channel);
    when(channel.getId()).thenReturn("ch-2");
    when(webhookSender.sendAsAuthor(
            eq(channel),
            eq(stickerBytes),
            eq("sticker.png"),
            eq("Ne_Tort"),
            eq("http://avatar.url")))
        .thenReturn(true);

    listener.onMessageReceived(event);

    verify(webhookSender)
        .sendAsAuthor(
            eq(channel),
            eq(stickerBytes),
            eq("sticker.png"),
            eq("Ne_Tort"),
            eq("http://avatar.url"));
    verify(user, never()).getName();
    verify(sender, never()).send(any(), any(byte[].class), anyString());
    verify(message).delete();
  }

  @Test
  void fallsBackToUserNameWhenMemberContextUnavailable() throws Exception {
    EmojiDetector detector = mock(EmojiDetector.class);
    StickerMappingService mappingService = mock(StickerMappingService.class);
    StickerSenderService sender = mock(StickerSenderService.class);
    WebhookStickerSender webhookSender = mockWebhookSender();
    EmojiMessageListener listener =
        new EmojiMessageListener(detector, mappingService, sender, webhookSender);

    MessageReceivedEvent event = mock(MessageReceivedEvent.class);
    Message message = mock(Message.class);
    User user = mock(User.class);
    Guild guild = mock(Guild.class);
    MessageChannelUnion channel = mock(MessageChannelUnion.class);

    byte[] stickerBytes = new byte[] {1, 2, 3};
    StickerAsset asset = new StickerAsset("sticker.png", stickerBytes, false, "o.png");

    when(event.getAuthor()).thenReturn(user);
    when(user.isBot()).thenReturn(false);
    when(user.getName()).thenReturn("Alice");
    when(user.getEffectiveAvatarUrl()).thenReturn("http://avatar.url");
    when(event.getMember()).thenReturn(null);
    when(event.getMessage()).thenReturn(message);
    when(message.getContentRaw()).thenReturn("😊");
    when(detector.isSingleEmojiMessage("😊")).thenReturn(true);
    when(detector.normalize("😊")).thenReturn("smile");
    when(event.isFromGuild()).thenReturn(true);
    when(event.getGuild()).thenReturn(guild);
    when(guild.getId()).thenReturn("1");
    when(mappingService.resolveSticker("1", "smile")).thenReturn(Optional.of(asset));
    when(event.getChannel()).thenReturn(channel);
    when(channel.getId()).thenReturn("ch-2");
    when(webhookSender.sendAsAuthor(
            eq(channel), eq(stickerBytes), eq("sticker.png"), eq("Alice"), eq("http://avatar.url")))
        .thenReturn(true);

    listener.onMessageReceived(event);

    verify(webhookSender)
        .sendAsAuthor(
            eq(channel), eq(stickerBytes), eq("sticker.png"), eq("Alice"), eq("http://avatar.url"));
    verify(sender, never()).send(any(), any(byte[].class), anyString());
    verify(message).delete();
  }

  @Test
  void skipsReplacementWhenMappingMissing() throws Exception {
    EmojiDetector detector = mock(EmojiDetector.class);
    StickerMappingService mappingService = mock(StickerMappingService.class);
    StickerSenderService sender = mock(StickerSenderService.class);
    WebhookStickerSender webhookSender = mockWebhookSender();
    EmojiMessageListener listener =
        new EmojiMessageListener(detector, mappingService, sender, webhookSender);

    MessageReceivedEvent event = mock(MessageReceivedEvent.class);
    Message message = mock(Message.class);
    User user = mock(User.class);
    Guild guild = mock(Guild.class);
    MessageChannelUnion channel = mock(MessageChannelUnion.class);

    when(event.getAuthor()).thenReturn(user);
    when(user.isBot()).thenReturn(false);
    when(event.getMessage()).thenReturn(message);
    when(message.getContentRaw()).thenReturn(":party:");
    when(detector.isSingleEmojiMessage(":party:")).thenReturn(true);
    when(detector.normalize(":party:")).thenReturn("party");
    when(event.isFromGuild()).thenReturn(true);
    when(event.getGuild()).thenReturn(guild);
    when(guild.getId()).thenReturn("guild-1");
    when(event.getChannel()).thenReturn(channel);
    when(channel.getId()).thenReturn("ch-3");
    when(mappingService.resolveSticker("guild-1", "party")).thenReturn(Optional.empty());

    listener.onMessageReceived(event);

    verify(mappingService).resolveSticker("guild-1", "party");
    verifyNoInteractions(sender, webhookSender);
  }

  @Test
  void sendsReplacementForDiscordCustomEmojiMention() throws Exception {
    EmojiDetector detector = new EmojiDetector();
    StickerMappingService mappingService = mock(StickerMappingService.class);
    StickerSenderService sender = mock(StickerSenderService.class);
    WebhookStickerSender webhookSender = mockWebhookSender();
    EmojiMessageListener listener =
        new EmojiMessageListener(detector, mappingService, sender, webhookSender);

    MessageReceivedEvent event = mock(MessageReceivedEvent.class);
    Message message = mock(Message.class);
    User user = mock(User.class);
    Guild guild = mock(Guild.class);
    MessageChannelUnion channel = mock(MessageChannelUnion.class);

    byte[] stickerBytes = new byte[] {1, 2, 3};
    StickerAsset asset = new StickerAsset("sticker.png", stickerBytes, false, "o.png");

    when(event.getAuthor()).thenReturn(user);
    when(user.isBot()).thenReturn(false);
    when(user.getName()).thenReturn("Alice");
    when(user.getEffectiveAvatarUrl()).thenReturn("http://avatar.url");
    when(event.getMessage()).thenReturn(message);
    when(message.getContentRaw()).thenReturn("<:aaa:933444648909832222>");
    when(event.isFromGuild()).thenReturn(true);
    when(event.getGuild()).thenReturn(guild);
    when(guild.getId()).thenReturn("1");
    when(mappingService.resolveSticker("1", "aaa")).thenReturn(Optional.of(asset));
    when(event.getChannel()).thenReturn(channel);
    when(channel.getId()).thenReturn("ch-2");
    when(webhookSender.sendAsAuthor(
            eq(channel), eq(stickerBytes), eq("sticker.png"), eq("Alice"), eq("http://avatar.url")))
        .thenReturn(true);

    listener.onMessageReceived(event);

    verify(mappingService).resolveSticker("1", "aaa");
    verify(webhookSender)
        .sendAsAuthor(
            eq(channel), eq(stickerBytes), eq("sticker.png"), eq("Alice"), eq("http://avatar.url"));
    verify(sender, never()).send(any(), any(byte[].class), anyString());
    verify(message).delete();
  }

  @Test
  void sendsReplacementForNewDefaultEmojiInput() throws Exception {
    EmojiDetector detector = new EmojiDetector();
    StickerMappingService mappingService = mock(StickerMappingService.class);
    StickerSenderService sender = mock(StickerSenderService.class);
    WebhookStickerSender webhookSender = mockWebhookSender();
    EmojiMessageListener listener =
        new EmojiMessageListener(detector, mappingService, sender, webhookSender);

    MessageReceivedEvent event = mock(MessageReceivedEvent.class);
    Message message = mock(Message.class);
    User user = mock(User.class);
    Guild guild = mock(Guild.class);
    MessageChannelUnion channel = mock(MessageChannelUnion.class);

    byte[] stickerBytes = new byte[] {1, 2, 3};
    StickerAsset asset =
        new StickerAsset("cry.png", stickerBytes, true, "default-stickers/cry.png");

    when(event.getAuthor()).thenReturn(user);
    when(user.isBot()).thenReturn(false);
    when(user.getName()).thenReturn("Alice");
    when(user.getEffectiveAvatarUrl()).thenReturn("http://avatar.url");
    when(event.getMessage()).thenReturn(message);
    when(message.getContentRaw()).thenReturn("😢");
    when(event.isFromGuild()).thenReturn(true);
    when(event.getGuild()).thenReturn(guild);
    when(guild.getId()).thenReturn("1");
    when(mappingService.resolveSticker("1", "cry")).thenReturn(Optional.of(asset));
    when(event.getChannel()).thenReturn(channel);
    when(channel.getId()).thenReturn("ch-2");
    when(webhookSender.sendAsAuthor(
            eq(channel), eq(stickerBytes), eq("cry.png"), eq("Alice"), eq("http://avatar.url")))
        .thenReturn(true);

    listener.onMessageReceived(event);

    verify(mappingService).resolveSticker("1", "cry");
    verify(webhookSender)
        .sendAsAuthor(
            eq(channel), eq(stickerBytes), eq("cry.png"), eq("Alice"), eq("http://avatar.url"));
    verify(sender, never()).send(any(), any(byte[].class), anyString());
    verify(message).delete();
  }

  @Test
  void keepsMixedContentWithNewEmojiOutsideReplacementScope() {
    EmojiDetector detector = new EmojiDetector();
    StickerMappingService mappingService = mock(StickerMappingService.class);
    StickerSenderService sender = mock(StickerSenderService.class);
    WebhookStickerSender webhookSender = mockWebhookSender();
    EmojiMessageListener listener =
        new EmojiMessageListener(detector, mappingService, sender, webhookSender);

    MessageReceivedEvent event = mock(MessageReceivedEvent.class);
    Message message = mock(Message.class);
    User user = mock(User.class);
    Guild guild = mock(Guild.class);
    MessageChannelUnion channel = mock(MessageChannelUnion.class);

    when(event.getAuthor()).thenReturn(user);
    when(user.isBot()).thenReturn(false);
    when(event.isFromGuild()).thenReturn(true);
    when(event.getGuild()).thenReturn(guild);
    when(guild.getId()).thenReturn("1");
    when(event.getChannel()).thenReturn(channel);
    when(channel.getId()).thenReturn("ch-1");
    when(user.getId()).thenReturn("user-1");
    when(event.getMessage()).thenReturn(message);
    when(message.getContentRaw()).thenReturn("hi 😢");

    listener.onMessageReceived(event);

    verifyNoInteractions(mappingService, sender, webhookSender);
  }
}
