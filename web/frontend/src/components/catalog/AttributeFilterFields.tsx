import { useEffect, useState } from 'react';
import { useProductAttributes } from '../../features/catalog/ProductAttributesContext';
import useDebouncedValue from '../../hooks/useDebouncedValue';

type AttributeFilter = {
  attribute?: string;
  attributeValue?: string;
};

type Props = AttributeFilter & {
  onChange: (filter: AttributeFilter) => void;
};

/** Filtro per attributo personalizzato: voce esatta per gli elenchi, testo contenuto per gli attributi liberi. */
export default function AttributeFilterFields({ attribute, attributeValue, onChange }: Props) {
  const { attributes } = useProductAttributes();
  const active = attributes.filter((candidate) => candidate.active);
  const selected = active.find((candidate) => candidate.code === attribute);
  const [text, setText] = useState(attributeValue ?? '');
  const debouncedText = useDebouncedValue(text, 300);

  useEffect(() => setText(attributeValue ?? ''), [attribute]);

  useEffect(() => {
    if (selected?.type === 'TEXT' && (debouncedText || undefined) !== attributeValue) {
      onChange({ attribute, attributeValue: debouncedText || undefined });
    }
  }, [debouncedText]);

  if (active.length === 0) return null;

  return (
    <>
      <label>
        Attributo
        <select value={selected ? attribute : ''} onChange={(event) => onChange({ attribute: event.target.value || undefined, attributeValue: undefined })}>
          <option value="">Nessuno</option>
          {active.map((candidate) => <option key={candidate.code} value={candidate.code}>{candidate.label}</option>)}
        </select>
      </label>
      {selected && (
        <label>
          Valore attributo
          {selected.type === 'LIST' ? (
            <select value={attributeValue ?? ''} onChange={(event) => onChange({ attribute, attributeValue: event.target.value || undefined })}>
              <option value="">Tutti</option>
              {selected.options.map((option) => <option key={option} value={option}>{option}</option>)}
            </select>
          ) : (
            <input value={text} placeholder="Contiene" onChange={(event) => setText(event.target.value)} />
          )}
        </label>
      )}
    </>
  );
}
