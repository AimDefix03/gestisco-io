import { FormEvent, useMemo } from 'react';
import { Product, ProductCategory, ProductPayload, UnitOfMeasure } from '../api';
import { UNIT_OF_MEASURE_LABELS } from '../utils/units';
import { vatRateLabel } from '../utils/vat';
import { useVatRates } from '../features/catalog/VatRatesContext';
import { categoryOptionLabel, useProductCategories } from '../features/catalog/ProductCategoriesContext';
import useDraftState from '../hooks/useDraftState';

type Props = {
  editingProduct: Product | null;
  busy: boolean;
  onSubmit: (payload: ProductPayload) => Promise<boolean>;
  onCancel: () => void;
};

const initialForm = {
  code: '',
  name: '',
  description: '',
  category: '' as ProductCategory,
  brand: '',
  productType: '',
  usageContext: '',
  price: '0',
  discount: '0',
  barcode: '',
  unitOfMeasure: 'PIECE' as UnitOfMeasure,
  vatRateCode: ''
};

export default function ProductForm({ editingProduct, busy, onSubmit, onCancel }: Props) {
  const { categories } = useProductCategories();
  const vatRates = useVatRates();
  const defaultVatRate = vatRates.find((rate) => rate.defaultRate);
  const initialValue = useMemo(() => editingProduct ? ({
      code: editingProduct.code,
      name: editingProduct.name,
      description: editingProduct.description,
      category: editingProduct.category,
      brand: editingProduct.brand,
      productType: editingProduct.productType,
      usageContext: editingProduct.usageContext,
      price: String(editingProduct.price),
      discount: String(editingProduct.discount),
      barcode: editingProduct.barcode ?? '',
      unitOfMeasure: editingProduct.unitOfMeasure,
      vatRateCode: editingProduct.vatRateCode
    }) : initialForm, [editingProduct]);
  const { value: form, setValue: setForm, dirty, clear } = useDraftState({
    key: `catalog:${editingProduct?.code ?? 'new'}`,
    view: 'catalog',
    label: editingProduct ? `Modifica prodotto ${editingProduct.code}` : 'Nuovo prodotto',
    initialValue
  });

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const saved = await onSubmit({
      code: form.code,
      name: form.name,
      description: form.description,
      category: form.category,
      brand: form.brand,
      productType: form.productType,
      usageContext: form.usageContext,
      price: Number(form.price),
      discount: Number(form.discount),
      barcode: form.barcode.trim() || null,
      unitOfMeasure: form.unitOfMeasure,
      vatRateCode: form.vatRateCode || null
    });
    if (saved) clear();
  }

  function updateField<Key extends keyof typeof form>(key: Key, value: (typeof form)[Key]) {
    setForm((current) => ({ ...current, [key]: value }));
  }

  function cancelEdit() {
    clear();
    onCancel();
  }

  return (
    <form className="product-form" onSubmit={handleSubmit}>
      <div className="section-heading compact">
        <span>Catalogo</span>
        <h2>{editingProduct ? 'Modifica prodotto' : 'Nuovo prodotto'}</h2>
        <p>Gestisci anagrafica e classificazione. La giacenza si registra dal magazzino.</p>
      </div>

      <div className="form-grid">
        <label>
          Codice
          <input value={form.code} readOnly={Boolean(editingProduct && !editingProduct.capabilities.canChangeCode)} onChange={(event) => updateField('code', event.target.value)} required />
        </label>
        <label>
          Nome
          <input value={form.name} onChange={(event) => updateField('name', event.target.value)} required />
        </label>
        <label>
          Categoria
          <select value={form.category} onChange={(event) => updateField('category', event.target.value as ProductCategory)} required>
            <option value="" disabled>Seleziona categoria</option>
            {categories
              .filter((category) => category.active || category.code === form.category)
              .map((category) => <option key={category.code} value={category.code}>{categoryOptionLabel(category)}</option>)}
            {form.category && !categories.some((category) => category.code === form.category) && (
              <option value={form.category}>{editingProduct?.categoryName ?? form.category}</option>
            )}
          </select>
        </label>
        <label>
          Codice a barre
          <input value={form.barcode} inputMode="text" autoComplete="off" maxLength={32} placeholder="EAN o codice interno" onChange={(event) => updateField('barcode', event.target.value)} />
        </label>
        <label>
          Unita di misura
          <select value={form.unitOfMeasure} onChange={(event) => updateField('unitOfMeasure', event.target.value as UnitOfMeasure)}>
            {(Object.keys(UNIT_OF_MEASURE_LABELS) as UnitOfMeasure[]).map((unit) => <option key={unit} value={unit}>{UNIT_OF_MEASURE_LABELS[unit]}</option>)}
          </select>
        </label>
        <label>
          Aliquota IVA
          <select value={form.vatRateCode} onChange={(event) => updateField('vatRateCode', event.target.value)}>
            {!editingProduct && <option value="">Predefinita{defaultVatRate ? ` (${vatRateLabel(defaultVatRate.rate, defaultVatRate.nature)})` : ''}</option>}
            {vatRates
              .filter((rate) => rate.active || rate.code === form.vatRateCode)
              .map((rate) => <option key={rate.code} value={rate.code}>{rate.description} · {vatRateLabel(rate.rate, rate.nature)}</option>)}
            {form.vatRateCode && !vatRates.some((rate) => rate.code === form.vatRateCode) && (
              <option value={form.vatRateCode}>{editingProduct?.vatLabel ?? form.vatRateCode}</option>
            )}
          </select>
        </label>
        <label>
          Brand
          <input value={form.brand} onChange={(event) => updateField('brand', event.target.value)} required />
        </label>
        <label>
          Tipo prodotto
          <input value={form.productType} onChange={(event) => updateField('productType', event.target.value)} required />
        </label>
        <label>
          Utilizzo opzionale
          <input value={form.usageContext} onChange={(event) => updateField('usageContext', event.target.value)} />
        </label>
        <label>
          Prezzo
          <input type="number" min="0" step="0.01" value={form.price} onChange={(event) => updateField('price', event.target.value)} required />
        </label>
        <label>
          Sconto %
          <input type="number" min="0" max="100" step="0.01" value={form.discount} onChange={(event) => updateField('discount', event.target.value)} required />
        </label>
        <label className="wide">
          Descrizione
          <textarea value={form.description} onChange={(event) => updateField('description', event.target.value)} required />
        </label>
      </div>

      <div className="form-actions">
        {dirty && <span className="draft-status" role="status">Bozza salvata per questa sessione</span>}
        {editingProduct && <button className="button secondary" type="button" onClick={cancelEdit}>Annulla</button>}
        <button className="button primary" type="submit" disabled={busy}>{busy ? 'Salvataggio...' : editingProduct ? 'Salva modifiche' : 'Crea prodotto'}</button>
      </div>
    </form>
  );
}
