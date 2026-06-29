package com.bigmoji.discord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.bigmoji.discord.CustomEmojiMetadataProvider.CustomEmojiMetadata;
import java.util.List;
import java.util.Optional;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import org.junit.jupiter.api.Test;

class CustomEmojiMetadataProviderTest {
  @Test
  void resolvesCustomShortcodeByGuildEmojiName() {
    JDA jda = mock(JDA.class);
    Guild guild = mock(Guild.class);
    RichCustomEmoji emoji = mock(RichCustomEmoji.class);
    when(jda.getGuildById("guild-1")).thenReturn(guild);
    when(guild.getEmojisByName("party_blob", true)).thenReturn(List.of(emoji));
    when(emoji.getId()).thenReturn("987654321098765432");
    when(emoji.getName()).thenReturn("party_blob");
    when(emoji.getImageUrl()).thenReturn("https://cdn.discordapp.com/emojis/987.png");
    when(emoji.isAnimated()).thenReturn(false);
    when(emoji.isAvailable()).thenReturn(true);

    Optional<CustomEmojiMetadata> metadata =
        new CustomEmojiMetadataProvider(jda).findFor("guild-1", ":party_blob:");

    assertTrue(metadata.isPresent());
    assertEquals("987654321098765432", metadata.get().id());
    assertEquals(":party_blob:", metadata.get().shortcode());
    assertEquals("https://cdn.discordapp.com/emojis/987.png", metadata.get().imageUrl());
    assertTrue(metadata.get().available());
  }

  @Test
  void resolvesAnimatedCustomMentionById() {
    JDA jda = mock(JDA.class);
    Guild guild = mock(Guild.class);
    RichCustomEmoji emoji = mock(RichCustomEmoji.class);
    when(jda.getGuildById("guild-1")).thenReturn(guild);
    when(guild.getEmojiById("111222333444555666")).thenReturn(emoji);
    when(emoji.getId()).thenReturn("111222333444555666");
    when(emoji.getName()).thenReturn("wave");
    when(emoji.getImageUrl()).thenReturn("https://cdn.discordapp.com/emojis/111.gif");
    when(emoji.isAnimated()).thenReturn(true);
    when(emoji.isAvailable()).thenReturn(true);

    Optional<CustomEmojiMetadata> metadata =
        new CustomEmojiMetadataProvider(jda).findFor("guild-1", "<a:wave:111222333444555666>");

    assertTrue(metadata.isPresent());
    assertEquals(":wave:", metadata.get().shortcode());
    assertTrue(metadata.get().animated());
    assertTrue(metadata.get().available());
  }

  @Test
  void returnsUnavailableMetadataWhenGuildIsMissing() {
    JDA jda = mock(JDA.class);
    when(jda.getGuildById("guild-1")).thenReturn(null);

    Optional<CustomEmojiMetadata> metadata =
        new CustomEmojiMetadataProvider(jda).findFor("guild-1", "<:missing:222333444555666777>");

    assertTrue(metadata.isPresent());
    assertEquals("222333444555666777", metadata.get().id());
    assertEquals(":missing:", metadata.get().shortcode());
    assertFalse(metadata.get().available());
  }

  @Test
  void returnsEmptyForUnicodeEmojiFallback() {
    Optional<CustomEmojiMetadata> metadata =
        new CustomEmojiMetadataProvider(mock(JDA.class)).findFor("guild-1", "😊");

    assertTrue(metadata.isEmpty());
  }
}
