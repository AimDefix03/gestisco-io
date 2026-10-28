import { FormEvent, useMemo } from 'react';
import { Product, ProductCategory, ProductPayload, UnitOfMeasure } from '../api';
import { vatRateLabel } from '../utils/vat';
import { useVatRates } from '../features/catalog/VatRatesContext';
import { categoryOptionLabel, useProductCategories } from '../features/catalog/ProductCategoriesContext';
import { attributeAppliesTo, useProductAttributes } from '../features/catalog/ProductAttributesContext';
import { useUnitsOfMeasure } from '../features/catalog/UnitsOfMeasureContext';
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
  vatRateCode: '',
  attributes: {} as Record<string, string>
};

export default function ProductForm({ editingProduct, busy, onSubmit, onCancel }: Props) {
  const { categories } = useProductCategories();
  const { attributes } = useProductAttributes();
  const { units } = useUnitsOfMeasure();
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
      vatRateCode: editingProduct.vatRateCode,
      attributes: Object.fromEntries(editingProduct.attributes.filter((attribute) => attribute.active).map((attribute) => [attribute.code, attribute.value]))
    }) : initialForm, [editingProduct]);
  const { value: form, setValue: setForm, dirty, clear } = useDraftState({
    key: `catalog:${editingProduct?.code ?? 'new'}`,
    view: 'catalog',
    label: editingProduct ? `Modifica prodotto ${editingProduct.code}` : 'Nuovo prodotto',
    initialValue
  });
  const attributeValues = form.attributes ?? {};
  const applicableAttributes = form.category
    ? attributes.filter((attribute) => attribute.active && attributeAppliesTo(attribute, form.category, categories))
    : [];

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
      vatRateCode: form.vatRateCode || null,
      // Solo gli attributi validi per la categoria scelta: quelli omessi vengono tolti dal prodotto.
      attributes: Object.fromEntries(applicableAttributes.map((attribute) => [attribute.code, (attributeValues[attribute.code] ?? '').trim()]))
    });
    if (saved) clear();
  }

  function updateField<Key extends keyof typeof form>(key: Key, value: (typeof form)[Key]) {
    setForm((current) => ({ ...current, [key]: value }));
  }

  function updateAttribute(code: string, value: string) {
    setForm((current) => ({ ...current, attributes: { ...(current.attributes ?? {}), [code]: value } }));
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
            {units
              .filter((unit) => unit.active || unit.code === form.unitOfMeasure)
              .map((unit) => <option key={unit.code} value={unit.code}>{unit.label} ({unit.symbol})</option>)}
            {!units.some((unit) => unit.code === form.unitOfMeasure) && (
              <option value={form.unitOfMeasure}>{editingProduct?.unitOfMeasure === form.unitOfMeasure ? editingProduct.unitLabel : form.unitOfMeasure}</option>
            )}
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
        {applicableAttributes.map((attribute) => (
          <label key={attribute.code}>
            {attribute.label}
            {attribute.type === 'LIST' ? (
              <select value={attributeValues[attribute.code] ?? ''} onChange={(event) => updateAttribute(attribute.code, event.target.value)}>
                <option value="">Non indicato</option>
                {attribute.options.map((option) => <option key={option} value={option}>{option}</option>)}
                {attributeValues[attribute.code] && !attribute.options.includes(attributeValues[attribute.code]) && (
                  <option value={attributeValues[attribute.code]}>{attributeValues[attribute.code]}</option>
                )}
              </select>
            ) : (
              <input value={attributeValues[attribute.code] ?? ''} maxLength={200} onChange={(event) => updateAttribute(attribute.code, event.target.value)} />
            )}
          </label>
        ))}
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
