import { renderHook } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import useDetailRouteSync from './useDetailRouteSync';

type Props = { active: boolean; routeDetail?: string; selected?: string };

function setup(initialProps: Props) {
  const onRouteDetail = vi.fn();
  const onSelectionChange = vi.fn();
  const hook = renderHook((props: Props) => useDetailRouteSync({ ...props, routeDetail: props.routeDetail, selected: props.selected, onRouteDetail, onSelectionChange }), { initialProps });
  return { ...hook, onRouteDetail, onSelectionChange };
}

describe('useDetailRouteSync', () => {
  it('applica il dettaglio dell indirizzo iniziale alla vista attiva', () => {
    const { onRouteDetail, onSelectionChange } = setup({ active: true, routeDetail: 'ORD-1' });

    expect(onRouteDetail).toHaveBeenCalledWith('ORD-1');
    expect(onSelectionChange).not.toHaveBeenCalled();
  });

  it('non tocca la selezione delle viste non attive', () => {
    const { rerender, onRouteDetail, onSelectionChange } = setup({ active: false, routeDetail: 'PO-1' });
    rerender({ active: false, routeDetail: 'PO-1', selected: 'PO-2' });

    expect(onRouteDetail).not.toHaveBeenCalled();
    expect(onSelectionChange).not.toHaveBeenCalled();
  });

  it('trasforma in indirizzo la selezione fatta nella vista senza rimandarla indietro', () => {
    const { rerender, onRouteDetail, onSelectionChange } = setup({ active: true });
    rerender({ active: true, selected: 'ORD-2' });

    expect(onSelectionChange).toHaveBeenCalledWith('ORD-2');
    rerender({ active: true, routeDetail: 'ORD-2', selected: 'ORD-2' });
    expect(onRouteDetail).not.toHaveBeenCalled();
    expect(onSelectionChange).toHaveBeenCalledTimes(1);
  });

  it('fa prevalere l indirizzo quando cambia con il tasto indietro', () => {
    const { rerender, onRouteDetail, onSelectionChange } = setup({ active: true, routeDetail: 'ORD-2', selected: 'ORD-2' });
    rerender({ active: true, routeDetail: undefined, selected: 'ORD-2' });

    expect(onRouteDetail).toHaveBeenCalledWith(undefined);
    rerender({ active: true, routeDetail: undefined, selected: undefined });
    expect(onSelectionChange).not.toHaveBeenCalled();
  });

  it('riallinea la selezione rimasta quando la vista torna attiva', () => {
    const { rerender, onRouteDetail } = setup({ active: false, selected: 'INV-OLD' });
    rerender({ active: true, routeDetail: '4', selected: 'INV-OLD' });

    expect(onRouteDetail).toHaveBeenCalledWith('4');
  });
});
