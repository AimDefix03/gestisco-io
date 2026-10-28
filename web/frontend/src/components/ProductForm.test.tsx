import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import ProductForm from './ProductForm';
import { renderWithDrafts } from '../test/renderWithDrafts';
import type { Product, ProductAttributeOption, ProductCategoryOption, UnitOfMeasureOption, VatRateOption } from '../api';
import { StaticProductAttributes } from '../features/catalog/ProductAttributesContext';
import { StaticProductCategories } from '../features/catalog/ProductCategoriesContext';
import { StaticUnitsOfMeasure } from '../features/catalog/UnitsOfMeasureContext';
import { StaticVatRates } from '../features/catalog/VatRatesContext';

const categories: ProductCategoryOption[] = [
  { code: 'HARDWARE', name: 'Hardware', path: 'Hardware', parentCode: null, depth: 1, active: true },
  { code: 'GPU', name: 'Schede video', path: 'Hardware / Schede video', parentCode: 'HARDWARE', depth: 2, active: true },
  { code: 'OLD', name: 'Fuori catalogo', path: 'Fuori catalogo', parentCode: null, depth: 1, active: false }
];

const vatRates: VatRateOption[] = [
  { code: 'IVA22', rate: 0.22, nature: null, description: 'IVA 22% ordinaria', defaultRate: true, active: true },
  { code: 'IVA10', rate: 0.1, nature: null, description: 'IVA 10% ridotta', defaultRate: false, active: true },
  { code: 'ESENTE', rate: 0, nature: 'N4', description: 'Esente', defaultRate: false, active: true },
  { code: 'IVA5', rate: 0.05, nature: null, description: 'IVA 5% ridotta', defaultRate: false, active: false }
];

const units: UnitOfMeasureOption[] = [
  { code: 'PIECE', label: 'Pezzo', symbol: 'pz', active: true, base: true, productCount: 3 },
  { code: 'PAIR', label: 'Paio', symbol: 'paia', active: true, base: false, productCount: 0 },
  { code: 'ROLL', label: 'Rotolo', symbol: 'rot', active: false, base: false, productCount: 1 }
];

const attributes: ProductAttributeOption[] = [
  { code: 'MEMORIA', label: 'Memoria', type: 'LIST', categoryCode: 'GPU', categoryPath: 'Hardware / Schede video', options: ['8 GB', '16 GB'], active: true },
  { code: 'NOTE', label: 'Note tecniche', type: 'TEXT', categoryCode: null, categoryPath: null, options: [], active: true },
  { code: 'VECCHIO', label: 'Attributo dismesso', type: 'TEXT', categoryCode: null, categoryPath: null, options: [], active: false }
];

function renderForm(ui: Parameters<typeof renderWithDrafts>[0], withAttributes: ProductAttributeOption[] = []) {
  return renderWithDrafts(
    <StaticProductCategories categories={categories}>
      <StaticProductAttributes attributes={withAttributes}>
        <StaticUnitsOfMeasure units={units}>
          <StaticVatRates vatRates={vatRates}>{ui}</StaticVatRates>
        </StaticUnitsOfMeasure>
      </StaticProductAttributes>
    </StaticProductCategories>
  );
}

describe('ProductForm', () => {
  it('converte e invia i dati di un nuovo prodotto', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue(true);
    renderForm(<ProductForm editingProduct={null} busy={false} onSubmit={onSubmit} onCancel={vi.fn()} />);

    await user.type(screen.getByLabelText('Codice'), 'GPU-001');
    await user.type(screen.getByLabelText('Nome'), 'Scheda video');
    expect(screen.queryByRole('option', { name: /Fuori catalogo/ })).not.toBeInTheDocument();
    await user.selectOptions(screen.getByLabelText('Categoria'), 'HARDWARE');
    await user.type(screen.getByLabelText('Codice a barre'), ' 8001234567897 ');
    await user.selectOptions(screen.getByLabelText('Unita di misura'), 'PAIR');
    await user.type(screen.getByLabelText('Brand'), 'Example Brand');
    await user.type(screen.getByLabelText('Tipo prodotto'), 'Scheda grafica');
    await user.type(screen.getByLabelText('Utilizzo opzionale'), 'Gaming');
    await user.clear(screen.getByLabelText('Prezzo'));
    await user.type(screen.getByLabelText('Prezzo'), '899.90');
    await user.clear(screen.getByLabelText('Sconto %'));
    await user.type(screen.getByLabelText('Sconto %'), '5');
    await user.type(screen.getByLabelText('Descrizione'), 'Prodotto di test per il catalogo.');
    await user.click(screen.getByRole('button', { name: 'Crea prodotto' }));

    await waitFor(() => expect(onSubmit).toHaveBeenCalledOnce());
    expect(onSubmit).toHaveBeenCalledWith({
      code: 'GPU-001',
      name: 'Scheda video',
      description: 'Prodotto di test per il catalogo.',
      category: 'HARDWARE',
      brand: 'Example Brand',
      productType: 'Scheda grafica',
      usageContext: 'Gaming',
      price: 899.9,
      discount: 5,
      barcode: '8001234567897',
      unitOfMeasure: 'PAIR',
      vatRateCode: null,
      attributes: {}
    });
    expect(screen.getByLabelText('Codice')).toHaveValue('');
  });

  it('mantiene la bozza quando il comando non e confermato', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue(false);
    renderForm(
      <ProductForm
        editingProduct={{
          id: 1,
          code: 'CPU-001',
          name: 'Processore',
          description: 'Descrizione',
          category: 'HARDWARE',
          categoryName: 'Hardware',
          barcode: null,
          unitOfMeasure: 'PIECE',
          unitLabel: 'Pezzo',
          vatRateCode: 'IVA22',
          vatRate: 0.22,
          vatLabel: 'IVA 22% ordinaria',
          attributes: [],
          brand: 'Example Brand',
          productType: 'CPU',
          usageContext: '',
          quantity: 2,
          reservedQuantity: 0,
          availableQuantity: 2,
          lastPurchaseCost: null, averagePurchaseCost: null, costedQuantity: 0, uncostedQuantity: 2, costCoveragePercentage: 0, knownInventoryCost: 0, potentialGrossMarginOnCostedStock: 0,
          price: 399,
          discount: 0,
          discountedPrice: 399,
          discontinued: false,
          capabilities: { canEdit: true, canChangeCode: true, canDelete: true, canDiscontinue: true, canMoveStock: true }
        }}
        busy={false}
        onSubmit={onSubmit}
        onCancel={vi.fn()}
      />
    );

    const name = screen.getByLabelText('Nome');
    await user.clear(name);
    await user.type(name, 'Processore aggiornato');
    await user.click(screen.getByRole('button', { name: 'Salva modifiche' }));

    await waitFor(() => expect(onSubmit).toHaveBeenCalledOnce());
    expect(name).toHaveValue('Processore aggiornato');
  });

  it('mostra i dati esistenti e consente di annullare la modifica', async () => {
    const user = userEvent.setup();
    const onCancel = vi.fn();
    renderForm(
      <ProductForm
        editingProduct={{
          id: 1,
          code: 'CPU-001',
          name: 'Processore',
          description: 'Descrizione',
          category: 'HARDWARE',
          categoryName: 'Hardware',
          barcode: null,
          unitOfMeasure: 'PIECE',
          unitLabel: 'Pezzo',
          vatRateCode: 'IVA22',
          vatRate: 0.22,
          vatLabel: 'IVA 22% ordinaria',
          attributes: [],
          brand: 'Example Brand',
          productType: 'CPU',
          usageContext: '',
          quantity: 2,
          reservedQuantity: 0,
          availableQuantity: 2,
          lastPurchaseCost: null, averagePurchaseCost: null, costedQuantity: 0, uncostedQuantity: 2, costCoveragePercentage: 0, knownInventoryCost: 0, potentialGrossMarginOnCostedStock: 0,
          price: 399,
          discount: 0,
          discountedPrice: 399,
          discontinued: false,
          capabilities: { canEdit: true, canChangeCode: true, canDelete: true, canDiscontinue: true, canMoveStock: true }
        }}
        busy={false}
        onSubmit={vi.fn().mockResolvedValue(true)}
        onCancel={onCancel}
      />
    );

    expect(screen.getByLabelText('Codice')).toHaveValue('CPU-001');
    expect(screen.getByLabelText('Aliquota IVA')).toHaveValue('IVA22');
    expect(screen.queryByRole('option', { name: /Predefinita/ })).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Salva modifiche' })).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Annulla' }));
    expect(onCancel).toHaveBeenCalledOnce();
  });

  it('offre le sottocategorie indentate e richiede una categoria', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue(true);
    renderForm(<ProductForm editingProduct={null} busy={false} onSubmit={onSubmit} onCancel={vi.fn()} />);

    const select = screen.getByLabelText('Categoria');
    expect(select).toBeRequired();
    expect(select).toHaveValue('');
    expect(screen.getByRole('option', { name: /Schede video/ })).toHaveValue('GPU');
    await user.click(screen.getByRole('button', { name: 'Crea prodotto' }));
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('usa l aliquota predefinita salvo scelta esplicita di un aliquota attiva', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue(true);
    renderForm(<ProductForm editingProduct={null} busy={false} onSubmit={onSubmit} onCancel={vi.fn()} />);

    const select = screen.getByLabelText('Aliquota IVA');
    expect(select).toHaveValue('');
    expect(screen.getByRole('option', { name: 'Predefinita (22%)' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Esente · Esente (N4)' })).toHaveValue('ESENTE');
    expect(screen.queryByRole('option', { name: /IVA 5%/ })).not.toBeInTheDocument();

    await user.type(screen.getByLabelText('Codice'), 'TSH-001');
    await user.type(screen.getByLabelText('Nome'), 'T-shirt');
    await user.selectOptions(screen.getByLabelText('Categoria'), 'HARDWARE');
    await user.selectOptions(select, 'IVA10');
    await user.type(screen.getByLabelText('Brand'), 'Example Brand');
    await user.type(screen.getByLabelText('Tipo prodotto'), 'Maglia');
    await user.type(screen.getByLabelText('Descrizione'), 'Prodotto di test con IVA ridotta.');
    await user.click(screen.getByRole('button', { name: 'Crea prodotto' }));

    await waitFor(() => expect(onSubmit).toHaveBeenCalledOnce());
    expect(onSubmit.mock.calls[0][0]).toMatchObject({ code: 'TSH-001', vatRateCode: 'IVA10' });
  });

  it('mostra solo gli attributi attivi validi per la categoria e invia i valori', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue(true);
    renderForm(<ProductForm editingProduct={null} busy={false} onSubmit={onSubmit} onCancel={vi.fn()} />, attributes);

    expect(screen.queryByLabelText('Note tecniche')).not.toBeInTheDocument();
    await user.selectOptions(screen.getByLabelText('Categoria'), 'HARDWARE');
    expect(screen.getByLabelText('Note tecniche')).toBeInTheDocument();
    expect(screen.queryByLabelText('Memoria')).not.toBeInTheDocument();
    expect(screen.queryByLabelText('Attributo dismesso')).not.toBeInTheDocument();

    await user.selectOptions(screen.getByLabelText('Categoria'), 'GPU');
    await user.selectOptions(screen.getByLabelText('Memoria'), '16 GB');
    await user.type(screen.getByLabelText('Note tecniche'), ' Doppia ventola ');
    await user.type(screen.getByLabelText('Codice'), 'GPU-016');
    await user.type(screen.getByLabelText('Nome'), 'Scheda 16 GB');
    await user.type(screen.getByLabelText('Brand'), 'Example Brand');
    await user.type(screen.getByLabelText('Tipo prodotto'), 'Scheda grafica');
    await user.type(screen.getByLabelText('Descrizione'), 'Prodotto con attributi.');
    await user.click(screen.getByRole('button', { name: 'Crea prodotto' }));

    await waitFor(() => expect(onSubmit).toHaveBeenCalledOnce());
    expect(onSubmit.mock.calls[0][0].attributes).toEqual({ MEMORIA: '16 GB', NOTE: 'Doppia ventola' });
  });

  it('toglie gli attributi che non valgono piu per la nuova categoria', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue(true);
    renderForm(<ProductForm editingProduct={null} busy={false} onSubmit={onSubmit} onCancel={vi.fn()} />, attributes);

    await user.selectOptions(screen.getByLabelText('Categoria'), 'GPU');
    await user.selectOptions(screen.getByLabelText('Memoria'), '8 GB');
    await user.selectOptions(screen.getByLabelText('Categoria'), 'HARDWARE');
    await user.type(screen.getByLabelText('Codice'), 'GPU-008');
    await user.type(screen.getByLabelText('Nome'), 'Scheda');
    await user.type(screen.getByLabelText('Brand'), 'Example Brand');
    await user.type(screen.getByLabelText('Tipo prodotto'), 'Scheda grafica');
    await user.type(screen.getByLabelText('Descrizione'), 'Cambio categoria.');
    await user.click(screen.getByRole('button', { name: 'Crea prodotto' }));

    await waitFor(() => expect(onSubmit).toHaveBeenCalledOnce());
    expect(onSubmit.mock.calls[0][0].attributes).toEqual({ NOTE: '' });
  });

  it('offre solo le unita attive ma conserva quella disattivata del prodotto in modifica', async () => {
    const newProduct = renderForm(<ProductForm editingProduct={null} busy={false} onSubmit={vi.fn()} onCancel={vi.fn()} />);
    const unitSelect = screen.getByLabelText('Unita di misura') as HTMLSelectElement;
    expect(Array.from(unitSelect.options).map((option) => option.text)).toEqual(['Pezzo (pz)', 'Paio (paia)']);
    newProduct.unmount();

    const rollProduct: Product = {
      id: 7, code: 'TES-001', name: 'Tessuto', description: 'Tessuto a rotolo', category: 'HARDWARE', categoryName: 'Hardware',
      barcode: null, unitOfMeasure: 'ROLL', unitLabel: 'Rotolo', vatRateCode: 'IVA22', vatRate: 0.22, vatLabel: 'IVA 22% ordinaria',
      attributes: [], brand: 'Brand', productType: 'Tessuto', usageContext: '', quantity: 1, reservedQuantity: 0, availableQuantity: 1,
      lastPurchaseCost: null, averagePurchaseCost: null, costedQuantity: 0, uncostedQuantity: 1, costCoveragePercentage: 0, knownInventoryCost: 0, potentialGrossMarginOnCostedStock: 0,
      price: 10, discount: 0, discountedPrice: 10, discontinued: false,
      capabilities: { canEdit: true, canChangeCode: true, canDelete: true, canDiscontinue: true, canMoveStock: true }
    };
    renderForm(<ProductForm editingProduct={rollProduct} busy={false} onSubmit={vi.fn()} onCancel={vi.fn()} />);

    expect(screen.getByLabelText('Unita di misura')).toHaveValue('ROLL');
    expect(screen.getByRole('option', { name: 'Rotolo (rot)' })).toBeInTheDocument();
  });
});
