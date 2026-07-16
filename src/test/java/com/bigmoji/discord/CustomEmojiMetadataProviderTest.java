package com.bigmoji.discord;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
  void buildsCdnPreviewFromCustomMentionWhenGuildIsMissing() {
    JDA jda = mock(JDA.class);
    when(jda.getGuildById("guild-1")).thenReturn(null);

    Optional<CustomEmojiMetadata> metadata =
        new CustomEmojiMetadataProvider(jda).findFor("guild-1", "<:missing:222333444555666777>");

    assertTrue(metadata.isPresent());
    assertEquals("222333444555666777", metadata.get().id());
    assertEquals(":missing:", metadata.get().shortcode());
    assertEquals(
        "https://cdn.discordapp.com/emojis/222333444555666777.png?size=96&quality=lossless",
        metadata.get().imageUrl());
    assertTrue(metadata.get().available());
  }

  @Test
  void preservesCustomEmojiCaseAndAnimationInCdnFallback() {
    Optional<CustomEmojiMetadata> metadata =
        new CustomEmojiMetadataProvider(null)
            .findFor("guild-1", "<a:Leva_Warrior_1:222333444555666777>");

    assertTrue(metadata.isPresent());
    assertEquals(":Leva_Warrior_1:", metadata.get().shortcode());
    assertEquals(
        "https://cdn.discordapp.com/emojis/222333444555666777.gif?size=96&quality=lossless",
        metadata.get().imageUrl());
    assertTrue(metadata.get().animated());
  }

  @Test
  void returnsEmptyForUnicodeEmojiFallback() {
    Optional<CustomEmojiMetadata> metadata =
        new CustomEmojiMetadataProvider(mock(JDA.class)).findFor("guild-1", "😊");

    assertTrue(metadata.isEmpty());
  }

  @Test
  void resolvesStandardDiscordShortcodeToUnicode() {
    JDA jda = mock(JDA.class);
    Guild guild = mock(Guild.class);
    when(jda.getGuildById("guild-1")).thenReturn(guild);
    when(guild.getEmojisByName("innocent", true)).thenReturn(List.of());

    Optional<CustomEmojiMetadata> metadata =
        new CustomEmojiMetadataProvider(jda).findFor("guild-1", ":innocent:");

    assertTrue(metadata.isPresent());
    assertEquals(":innocent:", metadata.get().shortcode());
    assertEquals("😇", metadata.get().unicodeEmoji());
    assertTrue(metadata.get().available());
  }

  @Test
  void exposesCustomEmojiCdnPreviewEvenWhenBotCannotUseIt() {
    JDA jda = mock(JDA.class);
    Guild guild = mock(Guild.class);
    RichCustomEmoji emoji = mock(RichCustomEmoji.class);
    when(jda.getGuildById("guild-1")).thenReturn(guild);
    when(guild.getEmojisByName("restricted", true)).thenReturn(List.of(emoji));
    when(emoji.getId()).thenReturn("987654321098765432");
    when(emoji.getName()).thenReturn("restricted");
    when(emoji.getImageUrl()).thenReturn("https://cdn.discordapp.com/emojis/987.png");
    when(emoji.isAvailable()).thenReturn(false);

    Optional<CustomEmojiMetadata> metadata =
        new CustomEmojiMetadataProvider(jda).findFor("guild-1", ":restricted:");

    assertTrue(metadata.isPresent());
    assertTrue(metadata.get().available());
    assertEquals("https://cdn.discordapp.com/emojis/987.png", metadata.get().imageUrl());
  }

  @Test
  void resolvesUniqueCustomEmojiFromAnotherCachedGuild() {
    JDA jda = mock(JDA.class);
    RichCustomEmoji emoji = mock(RichCustomEmoji.class);
    when(jda.getGuildById("guild-without-bot")).thenReturn(null);
    when(jda.getEmojisByName("Leva_Warrior_1", true)).thenReturn(List.of(emoji));
    when(emoji.getId()).thenReturn("987654321098765432");
    when(emoji.getName()).thenReturn("Leva_Warrior_1");
    when(emoji.getImageUrl()).thenReturn("https://cdn.discordapp.com/emojis/987.png");

    Optional<CustomEmojiMetadata> metadata =
        new CustomEmojiMetadataProvider(jda).findFor("guild-without-bot", ":Leva_Warrior_1:");

    assertTrue(metadata.isPresent());
    assertEquals(":Leva_Warrior_1:", metadata.get().shortcode());
    assertEquals("https://cdn.discordapp.com/emojis/987.png", metadata.get().imageUrl());
    assertTrue(metadata.get().available());
  }

  @Test
  void doesNotGuessBetweenDuplicateCustomEmojiNames() {
    JDA jda = mock(JDA.class);
    when(jda.getGuildById("guild-without-bot")).thenReturn(null);
    when(jda.getEmojisByName("duplicate_name", true))
        .thenReturn(List.of(mock(RichCustomEmoji.class), mock(RichCustomEmoji.class)));

    Optional<CustomEmojiMetadata> metadata =
        new CustomEmojiMetadataProvider(jda).findFor("guild-without-bot", ":duplicate_name:");

    assertTrue(metadata.isPresent());
    assertEquals(":duplicate_name:", metadata.get().shortcode());
    assertTrue(metadata.get().imageUrl() == null);
  }
}
