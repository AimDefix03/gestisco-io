import { createContext, ReactNode, useContext, useEffect, useMemo, useState } from 'react';
import { fetchVatRates, VatRateOption } from '../../api';

const VatRatesContext = createContext<VatRateOption[]>([]);

/** Carica una volta per sessione le aliquote IVA per il personale che gestisce il catalogo. */
export function VatRatesProvider({ enabled, children }: { enabled: boolean; children: ReactNode }) {
  const [vatRates, setVatRates] = useState<VatRateOption[]>([]);

  useEffect(() => {
    if (!enabled) {
      setVatRates([]);
      return undefined;
    }
    const controller = new AbortController();
    fetchVatRates(controller.signal).then(setVatRates).catch(() => setVatRates([]));
    return () => controller.abort();
  }, [enabled]);

  return <VatRatesContext.Provider value={vatRates}>{children}</VatRatesContext.Provider>;
}

export function StaticVatRates({ vatRates, children }: { vatRates: VatRateOption[]; children: ReactNode }) {
  const value = useMemo(() => vatRates, [vatRates]);
  return <VatRatesContext.Provider value={value}>{children}</VatRatesContext.Provider>;
}

export function useVatRates(): VatRateOption[] {
  return useContext(VatRatesContext);
}
