import { FormEvent, useEffect, useRef, useState } from 'react';
import { createUnitOfMeasure, UnitOfMeasureOption, updateUnitOfMeasure } from '../../api';
import { useUnitsOfMeasure } from '../../features/catalog/UnitsOfMeasureContext';
import { errorMessage } from '../../utils/errors';

type FormState = {
  code: string;
  label: string;
  symbol: string;
  active: boolean;
};

const emptyForm: FormState = { code: '', label: '', symbol: '', active: true };

type Props = {
  disabled?: boolean;
  /** Cambia quando cambia il catalogo, per tenere aggiornato il numero di prodotti per unita. */
  catalogVersion?: unknown;
};

export default function UnitManager({ disabled = false, catalogVersion }: Props) {
  const { units, loading, error, reload } = useUnitsOfMeasure();
  const firstCatalogVersion = useRef(true);
  const [editing, setEditing] = useState<UnitOfMeasureOption | null>(null);
  const [form, setForm] = useState<FormState>(emptyForm);
  const [saving, setSaving] = useState(false);
  const [feedback, setFeedback] = useState<{ tone: 'success' | 'error'; message: string } | null>(null);

  useEffect(() => {
    if (firstCatalogVersion.current) {
      firstCatalogVersion.current = false;
      return;
    }
    void reload();
  }, [catalogVersion, reload]);

  function startEdit(unit: UnitOfMeasureOption) {
    setEditing(unit);
    setForm({ code: unit.code, label: unit.label, symbol: unit.symbol, active: unit.active });
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
    const payload = { code: form.code.trim(), label: form.label.trim(), symbol: form.symbol.trim(), active: form.active };
    try {
      const saved = editing ? await updateUnitOfMeasure(editing.code, payload) : await createUnitOfMeasure(payload);
      await reload();
      setFeedback({ tone: 'success', message: `${editing ? 'Aggiornata' : 'Creata'} l'unita ${saved.label}.` });
      reset();
    } catch (exception) {
      setFeedback({ tone: 'error', message: errorMessage(exception) });
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="panel category-manager" aria-labelledby="unit-manager-title">
      <div className="section-heading compact">
        <span>Catalogo</span>
        <h2 id="unit-manager-title">Unita di misura</h2>
        <p>Come si conta e si vende un prodotto. Il pezzo e l'unita base e resta sempre attivo.</p>
      </div>

      {loading && units.length === 0 && <p role="status">Caricamento unita di misura...</p>}
      {Boolean(error) && <p role="alert" className="danger-text">{errorMessage(error)}</p>}
      {!loading && !error && units.length === 0 && <p>Nessuna unita di misura configurata.</p>}

      {units.length > 0 && (
        <ul className="category-tree">
          {units.map((unit) => (
            <li key={unit.code} className={unit.active ? undefined : 'inactive'}>
              <span>{unit.label} ({unit.symbol}){unit.active ? '' : ' (disattivata)'}</span>
              <small>{unit.base ? 'Unita base · ' : ''}{unit.productCount === 1 ? '1 prodotto' : `${unit.productCount} prodotti`}</small>
              <button className="link-button" type="button" disabled={disabled || saving} onClick={() => startEdit(unit)} aria-label={`Modifica unita ${unit.label}`}>Modifica</button>
            </li>
          ))}
        </ul>
      )}

      <form className="form-grid" onSubmit={handleSubmit}>
        <label>
          Codice unita
          <input value={form.code} readOnly={Boolean(editing)} pattern="[A-Za-z0-9][A-Za-z0-9_]*" maxLength={16} onChange={(event) => setForm((current) => ({ ...current, code: event.target.value }))} required />
        </label>
        <label>
          Nome unita
          <input value={form.label} maxLength={60} onChange={(event) => setForm((current) => ({ ...current, label: event.target.value }))} required />
        </label>
        <label>
          Simbolo
          <input value={form.symbol} maxLength={12} onChange={(event) => setForm((current) => ({ ...current, symbol: event.target.value }))} required />
        </label>
        {editing && !editing.base && (
          <label className="checkbox-line">
            <input type="checkbox" checked={form.active} onChange={(event) => setForm((current) => ({ ...current, active: event.target.checked }))} />
            Attiva
          </label>
        )}
        <div className="form-actions wide">
          {feedback && <span role={feedback.tone === 'error' ? 'alert' : 'status'} className={feedback.tone === 'error' ? 'danger-text' : 'draft-status'}>{feedback.message}</span>}
          {editing && <button className="button secondary" type="button" onClick={reset} disabled={saving}>Annulla</button>}
          <button className="button primary" type="submit" disabled={disabled || saving}>{saving ? 'Salvataggio...' : editing ? 'Salva unita' : 'Crea unita'}</button>
        </div>
      </form>
    </section>
  );
}
