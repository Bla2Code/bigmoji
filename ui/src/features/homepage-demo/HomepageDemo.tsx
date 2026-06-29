import { UploadCloud, WandSparkles } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { demoSteps, sampleSticker } from "./demoData";
import "./HomepageDemo.css";

export function HomepageDemo() {
  const prefersReducedMotion = useMemo(
    () =>
      typeof window !== "undefined" &&
      window.matchMedia("(prefers-reduced-motion: reduce)").matches,
    [],
  );
  const [activeStep, setActiveStep] = useState<(typeof demoSteps)[number]["id"]>(
    "upload",
  );

  useEffect(() => {
    if (prefersReducedMotion) return undefined;

    const timer = window.setInterval(() => {
      setActiveStep((current) => {
        const index = demoSteps.findIndex((step) => step.id === current);
        return demoSteps[(index + 1) % demoSteps.length]!.id;
      });
    }, 4200);

    return () => window.clearInterval(timer);
  }, [prefersReducedMotion]);

  const active = demoSteps.find((step) => step.id === activeStep) ?? demoSteps[0]!;

  return (
    <section className="homepage-demo" aria-label="Upload emoji replacement demo">
      <div className="demo-canvas" data-active-step={activeStep}>
        <div className="upload-tile" aria-label="Sticker upload preview">
          <UploadCloud aria-hidden="true" size={20} />
          <img alt={`${sampleSticker.name} sticker`} src={sampleSticker.imageSrc} />
          <span>{sampleSticker.name}.png</span>
        </div>
        <div className="discord-window" aria-label="Discord replacement preview">
          <div className="discord-titlebar">
            <span />
            <span />
            <span />
          </div>
          <div className="discord-message">
            <span className="avatar">A</span>
            <div>
              <strong>Admin</strong>
              <p className="emoji-message">{sampleSticker.emojiName}</p>
            </div>
          </div>
          <div className="discord-message replacement">
            <span className="avatar bot">B</span>
            <div>
              <strong>Bigmoji</strong>
              <img alt="Large replacement sticker" src={sampleSticker.imageSrc} />
            </div>
          </div>
        </div>
        <div className="demo-badge">
          <WandSparkles aria-hidden="true" size={16} />
          Emoji-only messages become stickers
        </div>
      </div>
      <div className="demo-steps" role="tablist" aria-label="Demo steps">
        {demoSteps.map((step) => (
          <button
            aria-selected={step.id === activeStep}
            className={step.id === activeStep ? "active" : ""}
            key={step.id}
            onClick={() => setActiveStep(step.id)}
            role="tab"
            type="button"
          >
            <span>{step.label}</span>
          </button>
        ))}
      </div>
      <div className="demo-copy" aria-live="polite">
        <h2>{active.title}</h2>
        <p>{active.text}</p>
      </div>
      {prefersReducedMotion ? (
        <p className="visually-hidden">Demo animation is reduced.</p>
      ) : null}
    </section>
  );
}
