import { createContext, ReactNode, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { fetchProductCategories, ProductCategoryOption } from '../../api';

type ProductCategoriesValue = {
  categories: ProductCategoryOption[];
  loading: boolean;
  error: unknown;
  reload: () => Promise<void>;
};

const ProductCategoriesContext = createContext<ProductCategoriesValue>({
  categories: [],
  loading: false,
  error: null,
  reload: async () => undefined
});

type ProviderProps = {
  enabled: boolean;
  includeInactive: boolean;
  children: ReactNode;
};

/** Carica una volta per sessione le categorie configurate e le rende disponibili a filtri e form. */
export function ProductCategoriesProvider({ enabled, includeInactive, children }: ProviderProps) {
  const [categories, setCategories] = useState<ProductCategoryOption[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<unknown>(null);
  const requestSequence = useRef(0);

  const reload = useCallback(async () => {
    if (!enabled) {
      setCategories([]);
      return;
    }
    const sequence = ++requestSequence.current;
    setLoading(true);
    try {
      const loaded = await fetchProductCategories(includeInactive);
      if (sequence === requestSequence.current) {
        setCategories(loaded);
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

  const value = useMemo(() => ({ categories, loading, error, reload }), [categories, loading, error, reload]);
  return <ProductCategoriesContext.Provider value={value}>{children}</ProductCategoriesContext.Provider>;
}

/** Fornisce un elenco fisso, per test e anteprime. */
export function StaticProductCategories({ categories, children }: { categories: ProductCategoryOption[]; children: ReactNode }) {
  const value = useMemo(() => ({ categories, loading: false, error: null, reload: async () => undefined }), [categories]);
  return <ProductCategoriesContext.Provider value={value}>{children}</ProductCategoriesContext.Provider>;
}

export function useProductCategories(): ProductCategoriesValue {
  return useContext(ProductCategoriesContext);
}

/** Etichetta indentata per menu a tendina gerarchici. */
export function categoryOptionLabel(category: ProductCategoryOption): string {
  return `${'  '.repeat(Math.max(0, category.depth - 1))}${category.name}${category.active ? '' : ' (disattivata)'}`;
}
