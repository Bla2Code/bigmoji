import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { customMappings } from "../../test/fixtures";
import { MappingList } from "./MappingList";

describe("MappingList", () => {
  it("groups mappings by emoji and hides storage internals", () => {
    render(<MappingList mappings={customMappings} onDelete={vi.fn()} />);

    expect(screen.getByText("2 custom stickers")).toBeInTheDocument();
    expect(screen.queryByText(/bigmoji-123456789012345678/i)).not.toBeInTheDocument();
    expect(screen.queryByText(/internal\/smile/i)).not.toBeInTheDocument();
  });

  it("requires confirmation before deleting a mapping", async () => {
    const onDelete = vi.fn().mockResolvedValue(undefined);
    render(<MappingList mappings={customMappings.slice(0, 1)} onDelete={onDelete} />);

    await userEvent.click(screen.getByRole("button", { name: /^delete$/i }));

    expect(onDelete).not.toHaveBeenCalled();
    expect(screen.getByRole("dialog", { name: /delete mapping/i })).toBeInTheDocument();

    await userEvent.click(screen.getByRole("button", { name: /delete mapping/i }));

    await waitFor(() => {
      expect(onDelete).toHaveBeenCalledWith(customMappings[0]);
    });
  });
});
