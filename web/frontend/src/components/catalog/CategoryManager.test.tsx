import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { ProductCategoryOption } from '../../api';
import { StaticProductCategories } from '../../features/catalog/ProductCategoriesContext';
import CategoryManager from './CategoryManager';

const apiMocks = vi.hoisted(() => ({
  createProductCategory: vi.fn(),
  updateProductCategory: vi.fn()
}));

vi.mock('../../api', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../../api')>()),
  ...apiMocks
}));

const categories: ProductCategoryOption[] = [
  { code: 'ABB', name: 'Abbigliamento', path: 'Abbigliamento', parentCode: null, depth: 1, active: true },
  { code: 'PANT', name: 'Pantaloni', path: 'Abbigliamento / Pantaloni', parentCode: 'ABB', depth: 2, active: true },
  { code: 'JEANS', name: 'Jeans', path: 'Abbigliamento / Pantaloni / Jeans', parentCode: 'PANT', depth: 3, active: true },
  { code: 'SCARPE', name: 'Scarpe', path: 'Scarpe', parentCode: null, depth: 1, active: false }
];

function renderManager() {
  return render(<StaticProductCategories categories={categories}><CategoryManager /></StaticProductCategories>);
}

describe('CategoryManager', () => {
  beforeEach(() => {
    apiMocks.createProductCategory.mockReset();
    apiMocks.updateProductCategory.mockReset();
  });

  it('crea una sottocategoria e conferma con il percorso', async () => {
    const user = userEvent.setup();
    apiMocks.createProductCategory.mockResolvedValue({ ...categories[1], code: 'GONNE', name: 'Gonne', path: 'Abbigliamento / Gonne' });
    renderManager();

    await user.type(screen.getByLabelText('Codice categoria'), 'GONNE');
    await user.type(screen.getByLabelText('Nome categoria'), 'Gonne');
    await user.selectOptions(screen.getByLabelText('Categoria superiore'), 'ABB');
    await user.click(screen.getByRole('button', { name: 'Crea categoria' }));

    await waitFor(() => expect(apiMocks.createProductCategory).toHaveBeenCalledWith({ code: 'GONNE', name: 'Gonne', parentCode: 'ABB', active: true }));
    expect(await screen.findByRole('status')).toHaveTextContent('Creata la categoria Abbigliamento / Gonne.');
    expect(screen.getByLabelText('Codice categoria')).toHaveValue('');
  });

  it('non propone come superiore se stessa, i discendenti, il terzo livello o categorie disattivate', async () => {
    const user = userEvent.setup();
    renderManager();

    const offeredParents = () => within(screen.getByLabelText('Categoria superiore')).getAllByRole('option').map((option) => option.getAttribute('value'));
    expect(offeredParents()).toEqual(['', 'ABB', 'PANT']);

    await user.click(screen.getByRole('button', { name: 'Modifica categoria Abbigliamento' }));
    expect(offeredParents()).toEqual(['']);
    expect(screen.getByLabelText('Codice categoria')).toHaveAttribute('readonly');
    expect(screen.getByLabelText('Attiva')).toBeChecked();
  });

  it('mostra l\'errore del server e conserva i dati inseriti', async () => {
    const user = userEvent.setup();
    apiMocks.updateProductCategory.mockRejectedValue(new Error('Disattivare prima le sottocategorie attive.'));
    renderManager();

    await user.click(screen.getByRole('button', { name: 'Modifica categoria Abbigliamento' }));
    await user.click(screen.getByLabelText('Attiva'));
    await user.click(screen.getByRole('button', { name: 'Salva categoria' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Disattivare prima le sottocategorie attive.');
    expect(apiMocks.updateProductCategory).toHaveBeenCalledWith('ABB', { code: 'ABB', name: 'Abbigliamento', parentCode: null, active: false });
    expect(screen.getByLabelText('Nome categoria')).toHaveValue('Abbigliamento');
  });
});
