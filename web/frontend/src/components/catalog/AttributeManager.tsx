import { FormEvent, useState } from 'react';
import { createProductAttribute, ProductAttributeOption, ProductAttributeType, updateProductAttribute } from '../../api';
import { useProductAttributes } from '../../features/catalog/ProductAttributesContext';
import { categoryOptionLabel, useProductCategories } from '../../features/catalog/ProductCategoriesContext';
import { errorMessage } from '../../utils/errors';

type FormState = {
  code: string;
  label: string;
  type: ProductAttributeType;
  categoryCode: string;
  options: string;
  active: boolean;
};

const emptyForm: FormState = { code: '', label: '', type: 'LIST', categoryCode: '', options: '', active: true };

const TYPE_LABELS: Record<ProductAttributeType, string> = { LIST: 'Elenco di valori', TEXT: 'Testo libero' };

export default function AttributeManager({ disabled = false }: { disabled?: boolean }) {
  const { attributes, loading, error, reload } = useProductAttributes();
  const { categories } = useProductCategories();
  const [editing, setEditing] = useState<ProductAttributeOption | null>(null);
  const [form, setForm] = useState<FormState>(emptyForm);
  const [saving, setSaving] = useState(false);
  const [feedback, setFeedback] = useState<{ tone: 'success' | 'error'; message: string } | null>(null);

  function startEdit(attribute: ProductAttributeOption) {
    setEditing(attribute);
    setForm({
      code: attribute.code,
      label: attribute.label,
      type: attribute.type,
      categoryCode: attribute.categoryCode ?? '',
      options: attribute.options.join('\n'),
      active: attribute.active
    });
    setFeedback(null);
  }

  function reset() {
    setEditing(null);
    setForm(emptyForm);
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSaving(true);
    setFeedback(null);
    const payload = {
      code: form.code.trim(),
      label: form.label.trim(),
      type: form.type,
      categoryCode: form.categoryCode || null,
      options: form.type === 'LIST' ? form.options.split('\n').map((option) => option.trim()).filter(Boolean) : [],
      active: form.active
    };
    try {
      const saved = editing ? await updateProductAttribute(editing.code, payload) : await createProductAttribute(payload);
      await reload();
      setFeedback({ tone: 'success', message: `${editing ? 'Aggiornato' : 'Creato'} l'attributo ${saved.label}.` });
      reset();
    } catch (exception) {
      setFeedback({ tone: 'error', message: errorMessage(exception) });
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="panel category-manager" aria-labelledby="attribute-manager-title">
      <div className="section-heading compact">
        <span>Catalogo</span>
        <h2 id="attribute-manager-title">Attributi</h2>
        <p>Informazioni descrittive come materiale o vestibilita, usabili in filtri ed export. Non generano varianti.</p>
      </div>

      {loading && attributes.length === 0 && <p role="status">Caricamento attributi...</p>}
      {Boolean(error) && <p role="alert" className="danger-text">{errorMessage(error)}</p>}
      {!loading && !error && attributes.length === 0 && <p>Nessun attributo configurato.</p>}

      {attributes.length > 0 && (
        <ul className="category-tree">
          {attributes.map((attribute) => (
            <li key={attribute.code} className={attribute.active ? undefined : 'inactive'}>
              <span>{attribute.label}{attribute.active ? '' : ' (disattivato)'}</span>
              <small>{TYPE_LABELS[attribute.type]} · {attribute.categoryPath ?? 'Tutti i prodotti'}</small>
              <button className="link-button" type="button" disabled={disabled || saving} onClick={() => startEdit(attribute)} aria-label={`Modifica attributo ${attribute.label}`}>Modifica</button>
            </li>
          ))}
        </ul>
      )}

      <form className="form-grid" onSubmit={handleSubmit}>
        <label>
          Codice attributo
          <input value={form.code} readOnly={Boolean(editing)} pattern="[A-Za-z0-9][A-Za-z0-9_\-]*" maxLength={64} onChange={(event) => setForm((current) => ({ ...current, code: event.target.value }))} required />
        </label>
        <label>
          Etichetta
          <input value={form.label} maxLength={120} onChange={(event) => setForm((current) => ({ ...current, label: event.target.value }))} required />
        </label>
        <label>
          Tipo
          <select value={form.type} disabled={Boolean(editing)} onChange={(event) => setForm((current) => ({ ...current, type: event.target.value as ProductAttributeType }))}>
            {(Object.keys(TYPE_LABELS) as ProductAttributeType[]).map((type) => <option key={type} value={type}>{TYPE_LABELS[type]}</option>)}
          </select>
        </label>
        <label>
          Vale per
          <select value={form.categoryCode} onChange={(event) => setForm((current) => ({ ...current, categoryCode: event.target.value }))}>
            <option value="">Tutti i prodotti</option>
            {categories
              .filter((category) => category.active || category.code === form.categoryCode)
              .map((category) => <option key={category.code} value={category.code}>{categoryOptionLabel(category)}</option>)}
          </select>
        </label>
        {form.type === 'LIST' && (
          <label className="wide">
            Valori ammessi (uno per riga)
            <textarea value={form.options} onChange={(event) => setForm((current) => ({ ...current, options: event.target.value }))} required />
          </label>
        )}
        {editing && (
          <label className="checkbox-line">
            <input type="checkbox" checked={form.active} onChange={(event) => setForm((current) => ({ ...current, active: event.target.checked }))} />
            Attivo
          </label>
        )}
        <div className="form-actions wide">
          {feedback && <span role={feedback.tone === 'error' ? 'alert' : 'status'} className={feedback.tone === 'error' ? 'danger-text' : 'draft-status'}>{feedback.message}</span>}
          {editing && <button className="button secondary" type="button" onClick={reset} disabled={saving}>Annulla</button>}
          <button className="button primary" type="submit" disabled={disabled || saving}>{saving ? 'Salvataggio...' : editing ? 'Salva attributo' : 'Crea attributo'}</button>
        </div>
      </form>
    </section>
  );
}
