import { FormEvent, useMemo, useState } from 'react';
import { createProductCategory, ProductCategoryOption, updateProductCategory } from '../../api';
import { categoryOptionLabel, useProductCategories } from '../../features/catalog/ProductCategoriesContext';
import { errorMessage } from '../../utils/errors';

type FormState = {
  code: string;
  name: string;
  parentCode: string;
  active: boolean;
};

const emptyForm: FormState = { code: '', name: '', parentCode: '', active: true };

export default function CategoryManager({ disabled = false }: { disabled?: boolean }) {
  const { categories, loading, error, reload } = useProductCategories();
  const [editing, setEditing] = useState<ProductCategoryOption | null>(null);
  const [form, setForm] = useState<FormState>(emptyForm);
  const [saving, setSaving] = useState(false);
  const [feedback, setFeedback] = useState<{ tone: 'success' | 'error'; message: string } | null>(null);

  const parentOptions = useMemo(() => categories.filter((category) =>
    category.depth < 3
    && (category.active || category.code === form.parentCode)
    && (!editing || (category.code !== editing.code && !isDescendant(category, editing, categories)))
  ), [categories, editing, form.parentCode]);

  function startEdit(category: ProductCategoryOption) {
    setEditing(category);
    setForm({ code: category.code, name: category.name, parentCode: category.parentCode ?? '', active: category.active });
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
    const payload = { code: form.code.trim(), name: form.name.trim(), parentCode: form.parentCode || null, active: form.active };
    try {
      const saved = editing ? await updateProductCategory(editing.code, payload) : await createProductCategory(payload);
      await reload();
      setFeedback({ tone: 'success', message: `${editing ? 'Aggiornata' : 'Creata'} la categoria ${saved.path}.` });
      reset();
    } catch (exception) {
      setFeedback({ tone: 'error', message: errorMessage(exception) });
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="panel category-manager" aria-labelledby="category-manager-title">
      <div className="section-heading compact">
        <span>Catalogo</span>
        <h2 id="category-manager-title">Categorie</h2>
        <p>Fino a tre livelli. Una categoria in uso si disattiva invece di essere eliminata.</p>
      </div>

      {loading && categories.length === 0 && <p role="status">Caricamento categorie...</p>}
      {Boolean(error) && <p role="alert" className="danger-text">{errorMessage(error)}</p>}
      {!loading && !error && categories.length === 0 && <p>Nessuna categoria configurata.</p>}

      {categories.length > 0 && (
        <ul className="category-tree">
          {categories.map((category) => (
            <li key={category.code} className={category.active ? undefined : 'inactive'}>
              <span>{categoryOptionLabel(category)}</span>
              <small>{category.code}</small>
              <button className="link-button" type="button" disabled={disabled || saving} onClick={() => startEdit(category)} aria-label={`Modifica categoria ${category.path}`}>Modifica</button>
            </li>
          ))}
        </ul>
      )}

      <form className="form-grid" onSubmit={handleSubmit}>
        <label>
          Codice categoria
          <input value={form.code} readOnly={Boolean(editing)} pattern="[A-Za-z0-9][A-Za-z0-9_\-]*" maxLength={64} onChange={(event) => setForm((current) => ({ ...current, code: event.target.value }))} required />
        </label>
        <label>
          Nome categoria
          <input value={form.name} maxLength={120} onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))} required />
        </label>
        <label>
          Categoria superiore
          <select value={form.parentCode} onChange={(event) => setForm((current) => ({ ...current, parentCode: event.target.value }))}>
            <option value="">Nessuna (primo livello)</option>
            {parentOptions.map((category) => <option key={category.code} value={category.code}>{categoryOptionLabel(category)}</option>)}
          </select>
        </label>
        {editing && (
          <label className="checkbox-line">
            <input type="checkbox" checked={form.active} onChange={(event) => setForm((current) => ({ ...current, active: event.target.checked }))} />
            Attiva
          </label>
        )}
        <div className="form-actions wide">
          {feedback && <span role={feedback.tone === 'error' ? 'alert' : 'status'} className={feedback.tone === 'error' ? 'danger-text' : 'draft-status'}>{feedback.message}</span>}
          {editing && <button className="button secondary" type="button" onClick={reset} disabled={saving}>Annulla</button>}
          <button className="button primary" type="submit" disabled={disabled || saving}>{saving ? 'Salvataggio...' : editing ? 'Salva categoria' : 'Crea categoria'}</button>
        </div>
      </form>
    </section>
  );
}

function isDescendant(candidate: ProductCategoryOption, ancestor: ProductCategoryOption, categories: ProductCategoryOption[]): boolean {
  let parentCode = candidate.parentCode;
  while (parentCode) {
    if (parentCode === ancestor.code) return true;
    parentCode = categories.find((category) => category.code === parentCode)?.parentCode ?? null;
  }
  return false;
}
