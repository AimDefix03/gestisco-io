import { act, cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { StrictMode, useState } from 'react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import AccessibleDialog from './AccessibleDialog';

function DialogHarness({ onEscape = vi.fn() }: { onEscape?: () => void }) {
  const [open, setOpen] = useState(false);
  return (
    <>
      <button type="button" onClick={() => setOpen(true)}>Apri dialog</button>
      {open && (
        <AccessibleDialog labelledBy="dialog-title" onEscape={() => { onEscape(); setOpen(false); }}>
          <h2 id="dialog-title">Conferma operazione</h2>
          <button type="button">Prima azione</button>
          <button type="button" onClick={() => setOpen(false)}>Chiudi</button>
        </AccessibleDialog>
      )}
    </>
  );
}

describe('AccessibleDialog', () => {
  afterEach(() => {
    cleanup();
    if (vi.isFakeTimers()) vi.runOnlyPendingTimers();
    vi.useRealTimers();
  });

  it('non sottrae il focus a una nuova destinazione dopo la chiusura', () => {
    vi.useFakeTimers();
    render(<><DialogHarness /><button type="button">Altra scheda</button></>);
    const trigger = screen.getByRole('button', { name: 'Apri dialog' });
    trigger.focus();
    fireEvent.click(trigger);
    fireEvent.click(screen.getByRole('button', { name: 'Chiudi' }));
    const destination = screen.getByRole('button', { name: 'Altra scheda' });
    destination.focus();
    act(() => vi.runOnlyPendingTimers());
    expect(destination).toHaveFocus();
  });

  it('non sottrae il focus a un nuovo dialog aperto nello stesso ciclo', () => {
    vi.useFakeTimers();
    const view = render(<button type="button">Origine</button>);
    screen.getByRole('button', { name: 'Origine' }).focus();
    const dialog = (id: string) => (
      <AccessibleDialog key={id} labelledBy={id}>
        <h2 id={id}>{id}</h2><button type="button">Azione {id}</button>
      </AccessibleDialog>
    );
    view.rerender(<><button type="button">Origine</button>{dialog('primo')}</>);
    view.rerender(<><button type="button">Origine</button>{dialog('secondo')}</>);
    act(() => vi.runOnlyPendingTimers());
    expect(screen.getByRole('button', { name: 'Azione secondo' })).toHaveFocus();
  });

  it('mantiene il focus nel dialog in StrictMode e lo restituisce alla vera origine', () => {
    vi.useFakeTimers();
    render(<StrictMode><DialogHarness /></StrictMode>);
    const trigger = screen.getByRole('button', { name: 'Apri dialog' });
    trigger.focus();
    fireEvent.click(trigger);
    act(() => vi.runOnlyPendingTimers());
    expect(screen.getByRole('button', { name: 'Prima azione' })).toHaveFocus();
    fireEvent.keyDown(document, { key: 'Escape' });
    act(() => vi.runOnlyPendingTimers());
    expect(trigger).toHaveFocus();
  });

  it('preferisce la scheda attiva ai pulsanti precedenti quando manca il controllo di origine', () => {
    vi.useFakeTimers();
    const view = render(<button type="button">Origine rimossa</button>);
    screen.getByRole('button', { name: 'Origine rimossa' }).focus();
    const workspace = <main><button type="button">Aggiorna</button><button type="button" aria-current="page">Magazzino</button></main>;
    view.rerender(<>{workspace}<AccessibleDialog labelledBy="confirm-title"><h2 id="confirm-title">Conferma</h2><button type="button">Apri scheda</button></AccessibleDialog></>);
    view.rerender(workspace);
    act(() => vi.runOnlyPendingTimers());
    expect(screen.getByRole('button', { name: 'Magazzino' })).toHaveFocus();
  });

  it('intrappola il focus, gestisce Escape e ripristina il controllo di origine', async () => {
    const user = userEvent.setup();
    const onEscape = vi.fn();
    const backgroundEscapeHandler = vi.fn();
    document.addEventListener('keydown', backgroundEscapeHandler);
    render(<DialogHarness onEscape={onEscape} />);

    const trigger = screen.getByRole('button', { name: 'Apri dialog' });
    await user.click(trigger);
    const first = screen.getByRole('button', { name: 'Prima azione' });
    const last = screen.getByRole('button', { name: 'Chiudi' });
    expect(first).toHaveFocus();

    last.focus();
    fireEvent.keyDown(document, { key: 'Tab' });
    expect(first).toHaveFocus();
    fireEvent.keyDown(document, { key: 'Tab', shiftKey: true });
    expect(last).toHaveFocus();

    backgroundEscapeHandler.mockClear();
    await user.keyboard('{Escape}');
    expect(onEscape).toHaveBeenCalledOnce();
    expect(backgroundEscapeHandler).not.toHaveBeenCalled();
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    await waitFor(() => expect(trigger).toHaveFocus());
    document.removeEventListener('keydown', backgroundEscapeHandler);
  });

  it('ripristina il focus sulla scheda attiva se il controllo di origine viene rimosso', async () => {
    const user = userEvent.setup();

    function UnmountedTriggerHarness() {
      const [open, setOpen] = useState(false);
      const [openedOnce, setOpenedOnce] = useState(false);
      return (
        <>
          <button aria-current="page" type="button">Catalogo prodotti</button>
          {!openedOnce && <button type="button" onClick={() => { setOpenedOnce(true); setOpen(true); }}>Apri da menu</button>}
          {open && (
            <AccessibleDialog labelledBy="fallback-title" onEscape={() => setOpen(false)}>
              <h2 id="fallback-title">Bozza aperta</h2>
              <button type="button">Resta qui</button>
            </AccessibleDialog>
          )}
        </>
      );
    }

    render(<UnmountedTriggerHarness />);
    await user.click(screen.getByRole('button', { name: 'Apri da menu' }));
    await user.keyboard('{Escape}');
    await waitFor(() => expect(screen.getByRole('button', { name: 'Catalogo prodotti' })).toHaveFocus());
  });
});
