import { describe, expect, it } from 'vitest';
import { View } from '../types/ui';
import { canOpenView } from './access';
import { locate, parsePath, pathForRoute } from './routes';

const VIEWS: View[] = ['dashboard', 'catalog', 'sales', 'partners', 'inventory', 'orders', 'purchases', 'documents', 'reports', 'accounts', 'company', 'monitoring', 'audit'];

describe('indirizzi del workspace', () => {
  it('assegna un indirizzo distinto a ogni sezione e lo rilegge senza perdite', () => {
    const paths = VIEWS.map((view) => pathForRoute({ view }));

    expect(new Set(paths).size).toBe(VIEWS.length);
    VIEWS.forEach((view, index) => expect(parsePath(paths[index])).toEqual({ view }));
    expect(pathForRoute({ view: 'dashboard' })).toBe('/');
    expect(pathForRoute({ view: 'accounts' })).toBe('/amministrazione/account');
  });

  it('codifica i dettagli di ordini, ordini fornitore e inventari fisici', () => {
    expect(pathForRoute({ view: 'orders', detail: 'ORD-2026-000042' })).toBe('/ordini/ORD-2026-000042');
    expect(pathForRoute({ view: 'purchases', detail: 'PO 7/B' })).toBe('/acquisti/PO%207%2FB');
    expect(pathForRoute({ view: 'inventory', detail: '12' })).toBe('/magazzino/inventari/12');
    expect(parsePath('/acquisti/PO%207%2FB')).toEqual({ view: 'purchases', detail: 'PO 7/B' });
    expect(parsePath('/magazzino/inventari/12')).toEqual({ view: 'inventory', detail: '12' });
    expect(parsePath('/ordini/ORD-1/')).toEqual({ view: 'orders', detail: 'ORD-1' });
  });

  it('ignora il dettaglio per le sezioni che non lo prevedono', () => {
    expect(pathForRoute({ view: 'catalog', detail: 'NB-01' })).toBe('/catalogo');
    expect(parsePath('/catalogo/NB-01')).toBeNull();
  });

  it('rifiuta indirizzi sconosciuti, annidati o malformati', () => {
    ['/inesistente', '/ordini/A/B', '/magazzino/inventari/0', '/magazzino/inventari/abc', '/magazzino/12', '/ordini/%E0%A4%A', '/amministrazione'].forEach((path) => {
      expect(parsePath(path)).toBeNull();
    });
  });

  it('segnala come non trovato un indirizzo sconosciuto conservandone il percorso', () => {
    expect(locate('/inesistente')).toEqual({ view: 'dashboard', path: '/inesistente', notFound: true });
    expect(locate('/ordini/')).toEqual({ view: 'orders', path: '/ordini', notFound: false });
  });
});

describe('accesso alle sezioni', () => {
  it('richiede il permesso della sezione e lascia libere dashboard, catalogo e ordini', () => {
    expect(canOpenView('dashboard', [])).toBe(true);
    expect(canOpenView('catalog', [])).toBe(true);
    expect(canOpenView('orders', [])).toBe(true);
    expect(canOpenView('accounts', ['VIEW_CATALOG'])).toBe(false);
    expect(canOpenView('accounts', ['MANAGE_ACCOUNTS'])).toBe(true);
    expect(canOpenView('audit', ['VIEW_AUDIT'])).toBe(true);
    expect(canOpenView('inventory', ['VIEW_ORDERS'])).toBe(false);
  });
});
