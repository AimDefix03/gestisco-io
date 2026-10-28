import { createContext, ReactNode, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { fetchProductAttributes, ProductAttributeOption, ProductCategoryOption } from '../../api';

type ProductAttributesValue = {
  attributes: ProductAttributeOption[];
  loading: boolean;
  error: unknown;
  reload: () => Promise<void>;
};

const ProductAttributesContext = createContext<ProductAttributesValue>({
  attributes: [],
  loading: false,
  error: null,
  reload: async () => undefined
});

type ProviderProps = {
  enabled: boolean;
  includeInactive: boolean;
  children: ReactNode;
};

/** Carica una volta per sessione gli attributi personalizzati e li rende disponibili a form, filtri e report. */
export function ProductAttributesProvider({ enabled, includeInactive, children }: ProviderProps) {
  const [attributes, setAttributes] = useState<ProductAttributeOption[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<unknown>(null);
  const requestSequence = useRef(0);

  const reload = useCallback(async () => {
    if (!enabled) {
      setAttributes([]);
      return;
    }
    const sequence = ++requestSequence.current;
    setLoading(true);
    try {
      const loaded = await fetchProductAttributes(includeInactive);
      if (sequence === requestSequence.current) {
        setAttributes(loaded);
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

  const value = useMemo(() => ({ attributes, loading, error, reload }), [attributes, loading, error, reload]);
  return <ProductAttributesContext.Provider value={value}>{children}</ProductAttributesContext.Provider>;
}

/** Fornisce un elenco fisso, per test e anteprime. */
export function StaticProductAttributes({ attributes, children }: { attributes: ProductAttributeOption[]; children: ReactNode }) {
  const value = useMemo(() => ({ attributes, loading: false, error: null, reload: async () => undefined }), [attributes]);
  return <ProductAttributesContext.Provider value={value}>{children}</ProductAttributesContext.Provider>;
}

export function useProductAttributes(): ProductAttributesValue {
  return useContext(ProductAttributesContext);
}

/** Un attributo senza categoria vale per tutti i prodotti, altrimenti per la sua categoria e le sottocategorie. */
export function attributeAppliesTo(attribute: ProductAttributeOption, categoryCode: string, categories: ProductCategoryOption[]): boolean {
  if (!attribute.categoryCode) return true;
  let current: string | null = categoryCode;
  while (current) {
    if (current === attribute.categoryCode) return true;
    current = categories.find((category) => category.code === current)?.parentCode ?? null;
  }
  return false;
}
