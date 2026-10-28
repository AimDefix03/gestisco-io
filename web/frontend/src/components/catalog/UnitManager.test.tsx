import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { UnitOfMeasureOption } from '../../api';
import { StaticUnitsOfMeasure } from '../../features/catalog/UnitsOfMeasureContext';
import UnitManager from './UnitManager';

const apiMocks = vi.hoisted(() => ({
  createUnitOfMeasure: vi.fn(),
  updateUnitOfMeasure: vi.fn()
}));

vi.mock('../../api', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../../api')>()),
  ...apiMocks
}));

const units: UnitOfMeasureOption[] = [
  { code: 'PIECE', label: 'Pezzo', symbol: 'pz', active: true, base: true, productCount: 12 },
  { code: 'PAIR', label: 'Paio', symbol: 'paia', active: true, base: false, productCount: 1 },
  { code: 'ROLL', label: 'Rotolo', symbol: 'rot', active: false, base: false, productCount: 0 }
];

function renderManager() {
  return render(<StaticUnitsOfMeasure units={units}><UnitManager /></StaticUnitsOfMeasure>);
}

describe('UnitManager', () => {
  beforeEach(() => {
    apiMocks.createUnitOfMeasure.mockReset();
    apiMocks.updateUnitOfMeasure.mockReset();
  });

  it('elenca le unita con uso e stato e ne crea una nuova', async () => {
    const user = userEvent.setup();
    apiMocks.createUnitOfMeasure.mockResolvedValue({ code: 'BOX', label: 'Scatola', symbol: 'sc', active: true, base: false, productCount: 0 });
    renderManager();

    expect(screen.getByText('Unita base · 12 prodotti')).toBeInTheDocument();
    expect(screen.getByText('1 prodotto')).toBeInTheDocument();
    expect(screen.getByText('Rotolo (rot) (disattivata)')).toBeInTheDocument();
    await user.type(screen.getByLabelText('Codice unita'), 'BOX');
    await user.type(screen.getByLabelText('Nome unita'), ' Scatola ');
    await user.type(screen.getByLabelText('Simbolo'), 'sc');
    await user.click(screen.getByRole('button', { name: 'Crea unita' }));

    await waitFor(() => expect(apiMocks.createUnitOfMeasure).toHaveBeenCalledWith({ code: 'BOX', label: 'Scatola', symbol: 'sc', active: true }));
    expect(await screen.findByRole('status')).toHaveTextContent("Creata l'unita Scatola.");
  });

  it('non offre la disattivazione dell unita base e blocca il codice in modifica', async () => {
    const user = userEvent.setup();
    renderManager();

    await user.click(screen.getByRole('button', { name: 'Modifica unita Pezzo' }));
    expect(screen.getByLabelText('Codice unita')).toHaveAttribute('readonly');
    expect(screen.queryByLabelText('Attiva')).not.toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Modifica unita Paio' }));
    expect(screen.getByLabelText('Attiva')).toBeChecked();
  });

  it('riattiva un unita e mostra l errore del server', async () => {
    const user = userEvent.setup();
    apiMocks.updateUnitOfMeasure.mockRejectedValue(new Error("Esiste gia un'unita di misura \"Paio\"."));
    renderManager();

    await user.click(screen.getByRole('button', { name: 'Modifica unita Rotolo' }));
    await user.click(screen.getByLabelText('Attiva'));
    await user.clear(screen.getByLabelText('Nome unita'));
    await user.type(screen.getByLabelText('Nome unita'), 'Paio');
    await user.click(screen.getByRole('button', { name: 'Salva unita' }));

    await waitFor(() => expect(apiMocks.updateUnitOfMeasure).toHaveBeenCalledWith('ROLL', { code: 'ROLL', label: 'Paio', symbol: 'rot', active: true }));
    expect(await screen.findByRole('alert')).toHaveTextContent('Esiste gia');
  });

  it('ricarica i conteggi quando cambia il catalogo, non al primo caricamento', () => {
    const reload = vi.fn().mockResolvedValue(undefined);
    const { rerender } = render(<StaticUnitsOfMeasure units={units} reload={reload}><UnitManager catalogVersion={1} /></StaticUnitsOfMeasure>);
    expect(reload).not.toHaveBeenCalled();

    rerender(<StaticUnitsOfMeasure units={units} reload={reload}><UnitManager catalogVersion={2} /></StaticUnitsOfMeasure>);
    expect(reload).toHaveBeenCalledOnce();
  });
});
