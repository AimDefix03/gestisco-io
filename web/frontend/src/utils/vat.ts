import { FiscalDocument } from '../api';

export function vatRateLabel(rate: number, nature: string | null): string {
  if (rate === 0) return nature ? `Esente (${nature})` : 'Esente';
  return `${(rate * 100).toLocaleString('it-IT', { maximumFractionDigits: 2 })}%`;
}

/** Aliquota unica del documento oppure l'elenco del riepilogo IVA per i documenti con aliquote miste. */
export function documentVatLabel(document: Pick<FiscalDocument, 'vatRate' | 'vatSummaries'>): string {
  const summaries = document.vatSummaries ?? [];
  if (summaries.length > 1) return `IVA mista ${summaries.map((summary) => vatRateLabel(summary.vatRate, summary.vatNature)).join(' · ')}`;
  if (summaries.length === 1) return `IVA ${vatRateLabel(summaries[0].vatRate, summaries[0].vatNature)}`;
  return document.vatRate === null ? 'IVA non indicata' : `IVA ${vatRateLabel(document.vatRate, null)}`;
}
