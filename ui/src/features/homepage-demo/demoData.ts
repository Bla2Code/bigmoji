export interface HomepageDemoStep {
  id: "upload" | "emoji" | "replace";
  label: string;
  title: string;
  text: string;
}

export const sampleSticker = {
  emojiName: "🔥",
  imageSrc: "/demo-stickers/sticker_01.png",
  name: "Launch fire",
};

export const demoSteps: HomepageDemoStep[] = [
  {
    id: "upload",
    label: "Upload",
    title: "Upload a sticker",
    text: "Add a transparent sticker image for the Discord server.",
  },
  {
    id: "emoji",
    label: "Trigger",
    title: "Choose the emoji",
    text: "Pick the emoji-only message that should expand into the sticker.",
  },
  {
    id: "replace",
    label: "Replace",
    title: "Bigmoji posts it large",
    text: "When the emoji appears by itself, Bigmoji swaps it for the mapped sticker.",
  },
];
