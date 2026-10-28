import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { ProductAttributeOption, ProductCategoryOption } from '../../api';
import { StaticProductAttributes } from '../../features/catalog/ProductAttributesContext';
import { StaticProductCategories } from '../../features/catalog/ProductCategoriesContext';
import AttributeFilterFields from './AttributeFilterFields';
import AttributeManager from './AttributeManager';

const apiMocks = vi.hoisted(() => ({
  createProductAttribute: vi.fn(),
  updateProductAttribute: vi.fn()
}));

vi.mock('../../api', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../../api')>()),
  ...apiMocks
}));

const categories: ProductCategoryOption[] = [
  { code: 'ABB', name: 'Abbigliamento', path: 'Abbigliamento', parentCode: null, depth: 1, active: true }
];

const attributes: ProductAttributeOption[] = [
  { code: 'MATERIALE', label: 'Materiale', type: 'LIST', categoryCode: 'ABB', categoryPath: 'Abbigliamento', options: ['Cotone', 'Lino'], active: true },
  { code: 'VESTIBILITA', label: 'Vestibilita', type: 'TEXT', categoryCode: null, categoryPath: null, options: [], active: true },
  { code: 'STAGIONE', label: 'Stagione', type: 'TEXT', categoryCode: null, categoryPath: null, options: [], active: false }
];

function renderWithAttributes(ui: Parameters<typeof render>[0]) {
  return render(
    <StaticProductCategories categories={categories}>
      <StaticProductAttributes attributes={attributes}>{ui}</StaticProductAttributes>
    </StaticProductCategories>
  );
}

afterEach(() => vi.useRealTimers());

describe('AttributeManager', () => {
  beforeEach(() => {
    apiMocks.createProductAttribute.mockReset();
    apiMocks.updateProductAttribute.mockReset();
  });

  it('crea un attributo a elenco con un valore per riga e ambito di categoria', async () => {
    const user = userEvent.setup();
    apiMocks.createProductAttribute.mockResolvedValue({ ...attributes[0], code: 'COLORE', label: 'Colore' });
    renderWithAttributes(<AttributeManager />);

    expect(screen.getByText('Elenco di valori · Abbigliamento')).toBeInTheDocument();
    expect(screen.getByText('Stagione (disattivato)')).toBeInTheDocument();
    await user.type(screen.getByLabelText('Codice attributo'), 'COLORE');
    await user.type(screen.getByLabelText('Etichetta'), 'Colore');
    await user.selectOptions(screen.getByLabelText('Vale per'), 'ABB');
    await user.type(screen.getByLabelText('Valori ammessi (uno per riga)'), 'Nero{enter} {enter}Bianco ');
    await user.click(screen.getByRole('button', { name: 'Crea attributo' }));

    await waitFor(() => expect(apiMocks.createProductAttribute).toHaveBeenCalledWith({
      code: 'COLORE', label: 'Colore', type: 'LIST', categoryCode: 'ABB', options: ['Nero', 'Bianco'], active: true
    }));
    expect(await screen.findByRole('status')).toHaveTextContent("Creato l'attributo Colore.");
  });

  it('in modifica blocca codice e tipo e mostra gli errori del server', async () => {
    const user = userEvent.setup();
    apiMocks.updateProductAttribute.mockRejectedValue(new Error('Valori ancora usati da prodotti: lino.'));
    renderWithAttributes(<AttributeManager />);

    await user.click(screen.getByRole('button', { name: 'Modifica attributo Materiale' }));
    expect(screen.getByLabelText('Codice attributo')).toHaveAttribute('readonly');
    expect(screen.getByLabelText('Tipo')).toBeDisabled();
    expect(screen.getByLabelText('Valori ammessi (uno per riga)')).toHaveValue('Cotone\nLino');
    await user.clear(screen.getByLabelText('Valori ammessi (uno per riga)'));
    await user.type(screen.getByLabelText('Valori ammessi (uno per riga)'), 'Cotone');
    await user.click(screen.getByRole('button', { name: 'Salva attributo' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Valori ancora usati da prodotti: lino.');
    expect(apiMocks.updateProductAttribute).toHaveBeenCalledWith('MATERIALE', expect.objectContaining({ options: ['Cotone'] }));
  });
});

describe('AttributeFilterFields', () => {
  it('filtra un elenco per voce esatta e nasconde gli attributi disattivati', async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();
    const { rerender } = renderWithAttributes(<AttributeFilterFields onChange={onChange} />);

    expect(screen.queryByRole('option', { name: 'Stagione' })).not.toBeInTheDocument();
    await user.selectOptions(screen.getByLabelText('Attributo'), 'MATERIALE');
    expect(onChange).toHaveBeenLastCalledWith({ attribute: 'MATERIALE', attributeValue: undefined });

    rerender(
      <StaticProductCategories categories={categories}>
        <StaticProductAttributes attributes={attributes}><AttributeFilterFields attribute="MATERIALE" onChange={onChange} /></StaticProductAttributes>
      </StaticProductCategories>
    );
    await user.selectOptions(screen.getByLabelText('Valore attributo'), 'Lino');
    expect(onChange).toHaveBeenLastCalledWith({ attribute: 'MATERIALE', attributeValue: 'Lino' });
  });

  it('applica il testo libero solo dopo una pausa di digitazione', () => {
    vi.useFakeTimers();
    const onChange = vi.fn();
    renderWithAttributes(<AttributeFilterFields attribute="VESTIBILITA" onChange={onChange} />);

    fireEvent.change(screen.getByLabelText('Valore attributo'), { target: { value: 'slim' } });
    expect(onChange).not.toHaveBeenCalled();
    act(() => vi.advanceTimersByTime(300));
    expect(onChange).toHaveBeenCalledWith({ attribute: 'VESTIBILITA', attributeValue: 'slim' });
  });
});
