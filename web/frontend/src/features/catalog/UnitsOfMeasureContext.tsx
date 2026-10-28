import { createContext, ReactNode, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { fetchUnitsOfMeasure, UnitOfMeasureOption } from '../../api';

type UnitsOfMeasureValue = {
  units: UnitOfMeasureOption[];
  loading: boolean;
  error: unknown;
  reload: () => Promise<void>;
};

const UnitsOfMeasureContext = createContext<UnitsOfMeasureValue>({
  units: [],
  loading: false,
  error: null,
  reload: async () => undefined
});

type ProviderProps = {
  enabled: boolean;
  includeInactive: boolean;
  children: ReactNode;
};

/** Carica una volta per sessione le unita di misura configurate e le rende disponibili al form prodotto. */
export function UnitsOfMeasureProvider({ enabled, includeInactive, children }: ProviderProps) {
  const [units, setUnits] = useState<UnitOfMeasureOption[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<unknown>(null);
  const requestSequence = useRef(0);

  const reload = useCallback(async () => {
    if (!enabled) {
      setUnits([]);
      return;
    }
    const sequence = ++requestSequence.current;
    setLoading(true);
    try {
      const loaded = await fetchUnitsOfMeasure(includeInactive);
      if (sequence === requestSequence.current) {
        setUnits(loaded);
        setError(null);
      }
    } catch (exception) {
      if (sequence === requestSequence.current) setError(exception);
    } finally {
      if (sequence === requestSequence.current) setLoading(false);
    }
  }, [enabled, includeInactive]);

  useEffect(() => {
    void reload();
  }, [reload]);

  const value = useMemo(() => ({ units, loading, error, reload }), [units, loading, error, reload]);
  return <UnitsOfMeasureContext.Provider value={value}>{children}</UnitsOfMeasureContext.Provider>;
}

const noReload = async () => undefined;

/** Fornisce un elenco fisso, per test e anteprime. */
export function StaticUnitsOfMeasure({ units, reload = noReload, children }: { units: UnitOfMeasureOption[]; reload?: () => Promise<void>; children: ReactNode }) {
  const value = useMemo(() => ({ units, loading: false, error: null, reload }), [units, reload]);
  return <UnitsOfMeasureContext.Provider value={value}>{children}</UnitsOfMeasureContext.Provider>;
}

export function useUnitsOfMeasure(): UnitsOfMeasureValue {
  return useContext(UnitsOfMeasureContext);
}
