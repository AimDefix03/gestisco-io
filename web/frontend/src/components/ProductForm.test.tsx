import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import ProductForm from './ProductForm';
import { renderWithDrafts } from '../test/renderWithDrafts';
import type { ProductCategoryOption, VatRateOption } from '../api';
import { StaticProductCategories } from '../features/catalog/ProductCategoriesContext';
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

function renderForm(ui: Parameters<typeof renderWithDrafts>[0]) {
  return renderWithDrafts(
    <StaticProductCategories categories={categories}>
      <StaticVatRates vatRates={vatRates}>{ui}</StaticVatRates>
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
      vatRateCode: null
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
});
