import { useEffect, useRef } from 'react';

type Options = {
  active: boolean;
  routeDetail: string | undefined;
  selected: string | undefined;
  onRouteDetail: (detail: string | undefined) => void;
  onSelectionChange: (detail: string | undefined) => void;
};

/** Allinea dettaglio selezionato e indirizzo della vista attiva: vince l'indirizzo quando cambia, una selezione nella vista crea una nuova voce di cronologia. */
export default function useDetailRouteSync({ active, routeDetail, selected, onRouteDetail, onSelectionChange }: Options) {
  const previousSelection = useRef(selected);

  useEffect(() => {
    if (active && routeDetail !== selected) onRouteDetail(routeDetail);
  }, [active, routeDetail]);

  useEffect(() => {
    const changed = previousSelection.current !== selected;
    previousSelection.current = selected;
    if (changed && active && selected !== routeDetail) onSelectionChange(selected);
  }, [selected]);
}
