package com.bigmoji.discord;

import static org.mockito.Mockito.*;

import com.bigmoji.domain.entity.StickerMapping;
import com.bigmoji.emoji.EmojiDetector;
import com.bigmoji.sticker.StickerMappingService;
import com.bigmoji.storage.MinioStorageService;
import java.util.Optional;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.junit.jupiter.api.Test;

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
    MinioStorageService storage = mock(MinioStorageService.class);
    EmojiMessageListener listener =
        new EmojiMessageListener(detector, mappingService, sender, webhookSender, storage);

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
    verifyNoInteractions(mappingService, sender, webhookSender, storage);
  }

  @Test
  void ignoresBotMessage() {
    EmojiDetector detector = mock(EmojiDetector.class);
    StickerMappingService mappingService = mock(StickerMappingService.class);
    StickerSenderService sender = mock(StickerSenderService.class);
    WebhookStickerSender webhookSender = mockWebhookSender();
    MinioStorageService storage = mock(MinioStorageService.class);
    EmojiMessageListener listener =
        new EmojiMessageListener(detector, mappingService, sender, webhookSender, storage);

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

    verifyNoInteractions(detector, mappingService, sender, webhookSender, storage);
  }

  @Test
  void sendsReplacementWhenMappingExists() throws Exception {
    EmojiDetector detector = mock(EmojiDetector.class);
    StickerMappingService mappingService = mock(StickerMappingService.class);
    StickerSenderService sender = mock(StickerSenderService.class);
    WebhookStickerSender webhookSender = mockWebhookSender();
    MinioStorageService storage = mock(MinioStorageService.class);
    EmojiMessageListener listener =
        new EmojiMessageListener(detector, mappingService, sender, webhookSender, storage);

    MessageReceivedEvent event = mock(MessageReceivedEvent.class);
    Message message = mock(Message.class);
    User user = mock(User.class);
    Guild guild = mock(Guild.class);
    MessageChannelUnion channel = mock(MessageChannelUnion.class);
    StickerMapping mapping = new StickerMapping();
    mapping.setMinioBucketName("b");
    mapping.setMinioObjectKey("o.png");
    mapping.setDefault(true);

    byte[] stickerBytes = new byte[] {1, 2, 3};

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
    when(mappingService.pickRandomMapping("1", "smile")).thenReturn(Optional.of(mapping));
    when(storage.downloadFile("b", "o.png")).thenReturn(stickerBytes);
    when(event.getChannel()).thenReturn(channel);
    when(channel.getId()).thenReturn("ch-2");
    when(webhookSender.sendAsAuthor(
            eq(channel), eq(stickerBytes), eq("sticker.png"), eq("Alice"), eq("http://avatar.url")))
        .thenReturn(true);

    listener.onMessageReceived(event);

    verify(storage).downloadFile("b", "o.png");
    verify(webhookSender)
        .sendAsAuthor(
            eq(channel), eq(stickerBytes), eq("sticker.png"), eq("Alice"), eq("http://avatar.url"));
    verify(sender, never()).send(any(), any(byte[].class), anyString());
    verify(message).delete();
    verify(mappingService).pickRandomMapping("1", "smile");
  }

  @Test
  void fallsBackToDirectSendWhenWebhookFails() throws Exception {
    EmojiDetector detector = mock(EmojiDetector.class);
    StickerMappingService mappingService = mock(StickerMappingService.class);
    StickerSenderService sender = mock(StickerSenderService.class);
    WebhookStickerSender webhookSender = mockWebhookSender();
    MinioStorageService storage = mock(MinioStorageService.class);
    EmojiMessageListener listener =
        new EmojiMessageListener(detector, mappingService, sender, webhookSender, storage);

    MessageReceivedEvent event = mock(MessageReceivedEvent.class);
    Message message = mock(Message.class);
    User user = mock(User.class);
    Guild guild = mock(Guild.class);
    MessageChannelUnion channel = mock(MessageChannelUnion.class);
    StickerMapping mapping = new StickerMapping();
    mapping.setMinioBucketName("b");
    mapping.setMinioObjectKey("o.png");
    mapping.setDefault(true);

    byte[] stickerBytes = new byte[] {1, 2, 3};

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
    when(mappingService.pickRandomMapping("1", "smile")).thenReturn(Optional.of(mapping));
    when(storage.downloadFile("b", "o.png")).thenReturn(stickerBytes);
    when(event.getChannel()).thenReturn(channel);
    when(channel.getId()).thenReturn("ch-2");
    when(webhookSender.sendAsAuthor(any(), any(), anyString(), anyString(), anyString()))
        .thenReturn(false);

    listener.onMessageReceived(event);

    verify(storage).downloadFile("b", "o.png");
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
    MinioStorageService storage = mock(MinioStorageService.class);
    EmojiMessageListener listener =
        new EmojiMessageListener(detector, mappingService, sender, webhookSender, storage);

    MessageReceivedEvent event = mock(MessageReceivedEvent.class);
    Message message = mock(Message.class);
    User user = mock(User.class);
    Member member = mock(Member.class);
    Guild guild = mock(Guild.class);
    MessageChannelUnion channel = mock(MessageChannelUnion.class);
    StickerMapping mapping = new StickerMapping();
    mapping.setMinioBucketName("b");
    mapping.setMinioObjectKey("o.png");
    mapping.setDefault(true);

    byte[] stickerBytes = new byte[] {1, 2, 3};

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
    when(mappingService.pickRandomMapping("1", "smile")).thenReturn(Optional.of(mapping));
    when(storage.downloadFile("b", "o.png")).thenReturn(stickerBytes);
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
    MinioStorageService storage = mock(MinioStorageService.class);
    EmojiMessageListener listener =
        new EmojiMessageListener(detector, mappingService, sender, webhookSender, storage);

    MessageReceivedEvent event = mock(MessageReceivedEvent.class);
    Message message = mock(Message.class);
    User user = mock(User.class);
    Guild guild = mock(Guild.class);
    MessageChannelUnion channel = mock(MessageChannelUnion.class);
    StickerMapping mapping = new StickerMapping();
    mapping.setMinioBucketName("b");
    mapping.setMinioObjectKey("o.png");
    mapping.setDefault(true);

    byte[] stickerBytes = new byte[] {1, 2, 3};

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
    when(mappingService.pickRandomMapping("1", "smile")).thenReturn(Optional.of(mapping));
    when(storage.downloadFile("b", "o.png")).thenReturn(stickerBytes);
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
  void skipsReplacementWhenMappingMissing() {
    EmojiDetector detector = mock(EmojiDetector.class);
    StickerMappingService mappingService = mock(StickerMappingService.class);
    StickerSenderService sender = mock(StickerSenderService.class);
    WebhookStickerSender webhookSender = mockWebhookSender();
    MinioStorageService storage = mock(MinioStorageService.class);
    EmojiMessageListener listener =
        new EmojiMessageListener(detector, mappingService, sender, webhookSender, storage);

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
    when(mappingService.pickRandomMapping("guild-1", "party")).thenReturn(Optional.empty());

    listener.onMessageReceived(event);

    verify(mappingService).pickRandomMapping("guild-1", "party");
    verifyNoInteractions(sender, webhookSender, storage);
  }
}
