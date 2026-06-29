import type { ReactNode } from "react";
import { Inbox } from "lucide-react";

interface EmptyStateProps {
  action?: ReactNode;
  message: string;
  title: string;
}

export function EmptyState({ action, message, title }: EmptyStateProps) {
  return (
    <section className="empty-state">
      <Inbox aria-hidden="true" size={26} />
      <div>
        <h2>{title}</h2>
        <p>{message}</p>
      </div>
      {action}
    </section>
  );
}
