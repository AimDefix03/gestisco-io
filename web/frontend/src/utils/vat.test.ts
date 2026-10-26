import { describe, expect, it } from 'vitest';
import { documentVatLabel, vatRateLabel } from './vat';

describe('etichette IVA', () => {
  it('formatta aliquote, decimali ed esenzioni', () => {
    expect(vatRateLabel(0.22, null)).toBe('22%');
    expect(vatRateLabel(0.055, null)).toBe('5,5%');
    expect(vatRateLabel(0, 'N4')).toBe('Esente (N4)');
  });

  it('riassume i documenti con aliquota unica, mista o storica', () => {
    const summary = (vatRate: number, vatNature: string | null) => ({ vatRate, vatNature, taxableAmount: 0, vatAmount: 0, totalAmount: 0 });
    expect(documentVatLabel({ vatRate: 0.1, vatSummaries: [summary(0.1, null)] })).toBe('IVA 10%');
    expect(documentVatLabel({ vatRate: null, vatSummaries: [summary(0.22, null), summary(0, 'N4')] })).toBe('IVA mista 22% · Esente (N4)');
    expect(documentVatLabel({ vatRate: 0.22, vatSummaries: [] })).toBe('IVA 22%');
    expect(documentVatLabel({ vatRate: null, vatSummaries: [] })).toBe('IVA non indicata');
  });
});
