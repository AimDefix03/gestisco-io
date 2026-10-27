import { act, render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { request } from '../api';
import { SessionProvider, useSession } from './SessionProvider';

const account = {
  id: 1,
  username: 'superadmin',
  role: 'SUPER_ADMIN' as const,
  roleLabel: 'Super admin',
  permissions: [],
  enabled: true,
  disabledAt: null,
  disabledBy: null,
  disabledReason: null
};

afterEach(() => vi.unstubAllGlobals());

describe('SessionProvider', () => {
  it('ripristina la sessione del cookie dopo una ricarica senza chiedere il login', async () => {
    const expiresAt = new Date(Date.now() + 3600000).toISOString();
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      headers: new Headers(),
      json: vi.fn().mockResolvedValue({ user: account, expiresAt })
    } as unknown as Response);
    vi.stubGlobal('fetch', fetchMock);
    render(<SessionProvider><Harness /></SessionProvider>);

    expect(screen.getByText('Ripristino in corso')).toBeInTheDocument();
    expect(await screen.findByText('Utente superadmin')).toBeInTheDocument();
    expect(fetchMock.mock.calls[0][0]).toBe('/api/accounts/session');
    expect(screen.queryByText('Ripristino in corso')).not.toBeInTheDocument();
  });

  it('mostra l accesso quando il cookie manca o e scaduto, senza aprire il rinnovo', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      status: 204,
      headers: new Headers(),
      json: vi.fn().mockRejectedValue(new SyntaxError('Unexpected end of JSON input'))
    } as unknown as Response));
    render(<SessionProvider><Harness /></SessionProvider>);

    await waitFor(() => expect(screen.queryByText('Ripristino in corso')).not.toBeInTheDocument());
    expect(screen.queryByText(/^Utente /)).not.toBeInTheDocument();
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
  });

  it('apre una sola gestione centralizzata della sessione sui 401 autenticati', async () => {
    const user = userEvent.setup();
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: false,
      status: 401,
      headers: new Headers({ 'X-Request-Id': 'req-session' }),
      json: vi.fn().mockResolvedValue({ message: 'Sessione revocata.', requestId: 'req-session' })
    } as unknown as Response));
    render(<SessionProvider><Harness /></SessionProvider>);

    await user.click(screen.getByRole('button', { name: 'Avvia sessione' }));
    await user.click(screen.getByRole('button', { name: 'Richiesta protetta' }));

    await waitFor(() => expect(screen.getByRole('alert')).toHaveTextContent('Sessione revocata. Codice richiesta: req-session'));
  });

  it('non tratta un 401 pubblico come scadenza di una sessione', async () => {
    const user = userEvent.setup();
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: false,
      status: 401,
      headers: new Headers(),
      json: vi.fn().mockResolvedValue({ message: 'Credenziali non valide.' })
    } as unknown as Response));
    render(<SessionProvider><Harness /></SessionProvider>);

    await user.click(screen.getByRole('button', { name: 'Richiesta protetta' }));

    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
  });

  it('segnala prima della scadenza senza attendere il tick periodico', async () => {
    render(<SessionProvider><Harness expiresAt={new Date(Date.now() + 120000).toISOString()} /></SessionProvider>);

    act(() => screen.getByRole('button', { name: 'Avvia sessione' }).click());

    await waitFor(() => expect(screen.getByRole('alert')).toHaveTextContent('sta per scadere'));
  });
});

function Harness({ expiresAt = new Date(Date.now() + 3600000).toISOString() }: { expiresAt?: string }) {
  const session = useSession();
  return (
    <div>
      <button onClick={() => session.startSession({ user: account, expiresAt })}>Avvia sessione</button>
      <button onClick={() => void request('/api/protected').catch(() => undefined)}>Richiesta protetta</button>
      {session.renewalMessage && <div role="alert">{session.renewalMessage}</div>}
      {session.restoring && <p>Ripristino in corso</p>}
      {session.currentUser && <p>Utente {session.currentUser.username}</p>}
    </div>
  );
}
