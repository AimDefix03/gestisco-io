import { act, renderHook } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import useWorkspaceNavigation from './useWorkspaceNavigation';

beforeEach(() => window.history.replaceState(null, '', '/'));

function goBackTo(path: string) {
  act(() => {
    window.history.replaceState(null, '', path);
    window.dispatchEvent(new PopStateEvent('popstate'));
  });
}

describe('useWorkspaceNavigation', () => {
  it('apre la sezione dell indirizzo iniziale accanto alla dashboard', () => {
    window.history.replaceState(null, '', '/ordini/ORD-7');
    const { result } = renderHook(() => useWorkspaceNavigation());

    expect(result.current.route).toMatchObject({ view: 'orders', detail: 'ORD-7', notFound: false });
    expect(result.current.openTabs).toEqual(['dashboard', 'orders']);
  });

  it('crea una voce di cronologia per ogni scheda aperta e ricorda il dettaglio della scheda', () => {
    const { result } = renderHook(() => useWorkspaceNavigation());
    const initialLength = window.history.length;

    act(() => result.current.openTab('orders'));
    act(() => result.current.navigate({ view: 'orders', detail: 'ORD-9' }));
    act(() => result.current.openTab('catalog'));

    expect(window.location.pathname).toBe('/catalogo');
    expect(window.history.length).toBe(initialLength + 3);
    expect(result.current.openTabs).toEqual(['dashboard', 'orders', 'catalog']);

    act(() => result.current.setActiveView('orders'));
    expect(window.location.pathname).toBe('/ordini/ORD-9');
    expect(result.current.route.detail).toBe('ORD-9');
  });

  it('segue il tasto indietro e riapre la scheda chiusa', () => {
    const { result } = renderHook(() => useWorkspaceNavigation());
    act(() => result.current.openTab('reports'));
    act(() => result.current.closeTab('reports'));
    expect(result.current.openTabs).toEqual(['dashboard']);

    goBackTo('/report');

    expect(result.current.activeView).toBe('reports');
    expect(result.current.openTabs).toEqual(['dashboard', 'reports']);
  });

  it('trattiene il tasto indietro quando la scheda ha una bozza e prosegue solo su conferma', () => {
    const onLeaveBlocked = vi.fn();
    const { result } = renderHook(() => useWorkspaceNavigation({ shouldConfirmLeave: (from) => from === 'inventory', onLeaveBlocked }));
    act(() => result.current.openTab('catalog'));
    act(() => result.current.openTab('inventory'));

    goBackTo('/catalogo');

    expect(window.location.pathname).toBe('/magazzino');
    expect(result.current.activeView).toBe('inventory');
    expect(onLeaveBlocked).toHaveBeenCalledWith(expect.objectContaining({ view: 'catalog', path: '/catalogo' }), expect.any(Function));

    act(() => onLeaveBlocked.mock.calls[0][1]());
    expect(window.location.pathname).toBe('/catalogo');
    expect(result.current.activeView).toBe('catalog');
  });

  it('non trattiene i cambi di dettaglio all interno della stessa scheda', () => {
    const shouldConfirmLeave = vi.fn(() => true);
    const { result } = renderHook(() => useWorkspaceNavigation({ shouldConfirmLeave }));
    act(() => result.current.navigate({ view: 'orders', detail: 'ORD-1' }));

    goBackTo('/ordini');

    expect(shouldConfirmLeave).not.toHaveBeenCalled();
    expect(result.current.route).toMatchObject({ view: 'orders', path: '/ordini' });
    expect(result.current.route.detail).toBeUndefined();
  });

  it('mostra gli indirizzi sconosciuti come pagina non trovata', () => {
    window.history.replaceState(null, '', '/pagina-inesistente');
    const { result } = renderHook(() => useWorkspaceNavigation());

    expect(result.current.route).toMatchObject({ view: 'dashboard', path: '/pagina-inesistente', notFound: true });
    act(() => result.current.setActiveView('dashboard'));
    expect(window.location.pathname).toBe('/');
    expect(result.current.route.notFound).toBe(false);
  });

  it('riporta alla radice all uscita e riparte dall indirizzo corrente al nuovo accesso', () => {
    const { result } = renderHook(() => useWorkspaceNavigation());
    act(() => result.current.navigate({ view: 'orders', detail: 'ORD-3' }));
    act(() => result.current.openTab('catalog'));

    act(() => result.current.resetNavigation());
    expect(window.location.pathname).toBe('/');
    expect(result.current.openTabs).toEqual(['dashboard']);

    window.history.replaceState(null, '', '/acquisti/PO-5');
    act(() => result.current.restoreFromLocation());
    expect(result.current.route).toMatchObject({ view: 'purchases', detail: 'PO-5' });
    expect(result.current.openTabs).toEqual(['dashboard', 'purchases']);

    act(() => result.current.setActiveView('orders'));
    expect(window.location.pathname).toBe('/ordini');
  });
});
