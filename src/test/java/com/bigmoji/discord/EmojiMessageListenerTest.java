package com.bigmoji.discord;

import static org.mockito.Mockito.*;

import com.bigmoji.domain.entity.StickerMapping;
import com.bigmoji.emoji.EmojiDetector;
import com.bigmoji.sticker.StickerMappingService;
import com.bigmoji.storage.MinioStorageService;
import java.util.Optional;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.junit.jupiter.api.Test;

class EmojiMessageListenerTest {
  @Test
  void ignoresInvalidMessage() {
    EmojiDetector detector = mock(EmojiDetector.class);
    StickerMappingService mappingService = mock(StickerMappingService.class);
    StickerSenderService sender = mock(StickerSenderService.class);
    MinioStorageService storage = mock(MinioStorageService.class);
    EmojiMessageListener listener = new EmojiMessageListener(detector, mappingService, sender, storage);

    MessageReceivedEvent event = mock(MessageReceivedEvent.class);
    Message message = mock(Message.class);
    User user = mock(User.class);

    when(event.getAuthor()).thenReturn(user);
    when(user.isBot()).thenReturn(false);
    when(event.getMessage()).thenReturn(message);
    when(message.getContentRaw()).thenReturn("hello");
    when(detector.isSingleEmojiMessage("hello")).thenReturn(false);

    listener.onMessageReceived(event);
    verifyNoInteractions(mappingService);
  }

  @Test
  void sendsReplacementWhenMappingExists() throws Exception {
    EmojiDetector detector = mock(EmojiDetector.class);
    StickerMappingService mappingService = mock(StickerMappingService.class);
    StickerSenderService sender = mock(StickerSenderService.class);
    MinioStorageService storage = mock(MinioStorageService.class);
    EmojiMessageListener listener = new EmojiMessageListener(detector, mappingService, sender, storage);

    MessageReceivedEvent event = mock(MessageReceivedEvent.class);
    Message message = mock(Message.class);
    User user = mock(User.class);
    Guild guild = mock(Guild.class);
    MessageChannel channel = mock(MessageChannel.class);
    StickerMapping mapping = new StickerMapping();
    mapping.setMinioBucketName("b");
    mapping.setMinioObjectKey("o");

    when(event.getAuthor()).thenReturn(user);
    when(user.isBot()).thenReturn(false);
    when(event.getMessage()).thenReturn(message);
    when(message.getContentRaw()).thenReturn("😊");
    when(detector.isSingleEmojiMessage("😊")).thenReturn(true);
    when(detector.normalize("😊")).thenReturn("😊");
    when(event.getGuild()).thenReturn(guild);
    when(guild.getId()).thenReturn("1");
    when(mappingService.pickRandomMapping("1", "😊")).thenReturn(Optional.of(mapping));
    when(storage.presignedGetUrl("b", "o")).thenReturn("http://example");
    when(event.getChannel()).thenReturn(channel);

    listener.onMessageReceived(event);
    verify(sender).send(channel, "http://example");
  }
}
