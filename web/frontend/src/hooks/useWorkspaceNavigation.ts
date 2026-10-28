import { useEffect, useRef, useState } from 'react';
import { LocatedRoute, locate, pathForRoute, WorkspaceRoute } from '../navigation/routes';
import { View } from '../types/ui';

type Options = {
  shouldConfirmLeave?: (from: View, to: View) => boolean;
  onLeaveBlocked?: (target: LocatedRoute, proceed: () => void) => void;
};

type NavigateOptions = {
  replace?: boolean;
};

export default function useWorkspaceNavigation({ shouldConfirmLeave, onLeaveBlocked }: Options = {}) {
  const [route, setRoute] = useState<LocatedRoute>(currentLocation);
  const [openTabs, setOpenTabs] = useState<View[]>(() => tabsFor(route.view));
  const [openMenu, setOpenMenu] = useState<string | null>(null);
  const routeRef = useRef(route);
  const lastRoutes = useRef<Partial<Record<View, WorkspaceRoute>>>(route.notFound ? {} : { [route.view]: { view: route.view, detail: route.detail } });
  const guardRef = useRef({ shouldConfirmLeave, onLeaveBlocked });
  guardRef.current = { shouldConfirmLeave, onLeaveBlocked };

  useEffect(() => {
    const handlePopState = () => {
      const target = currentLocation();
      const current = routeRef.current;
      const { shouldConfirmLeave: confirmLeave, onLeaveBlocked: leaveBlocked } = guardRef.current;
      if (target.view !== current.view && confirmLeave?.(current.view, target.view)) {
        window.history.pushState(null, '', current.path);
        leaveBlocked?.(target, () => {
          window.history.pushState(null, '', target.path);
          apply(target);
        });
        return;
      }
      apply(target);
    };
    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, []);

  function apply(target: LocatedRoute) {
    routeRef.current = target;
    setRoute(target);
    setOpenTabs((current) => current.includes(target.view) ? current : [...current, target.view]);
    if (!target.notFound) lastRoutes.current[target.view] = { view: target.view, detail: target.detail };
  }

  function navigate(target: WorkspaceRoute, { replace = false }: NavigateOptions = {}) {
    const path = pathForRoute(target);
    if (path !== window.location.pathname) {
      if (replace) window.history.replaceState(null, '', path);
      else window.history.pushState(null, '', path);
    }
    apply({ view: target.view, detail: target.detail, path, notFound: false });
  }

  function setActiveView(target: View) {
    navigate(lastRoutes.current[target] ?? { view: target });
  }

  function openTab(target: View) {
    setActiveView(target);
    setOpenMenu(null);
  }

  function closeTab(target: View) {
    if (target === 'dashboard') return;
    const targetIndex = openTabs.indexOf(target);
    const nextTabs = openTabs.filter((tab) => tab !== target);
    const remaining = nextTabs.length ? nextTabs : ['dashboard' as View];
    delete lastRoutes.current[target];
    if (routeRef.current.view === target) {
      const nextView = remaining[targetIndex - 1] ?? remaining[targetIndex] ?? 'dashboard';
      navigate(lastRoutes.current[nextView] ?? { view: nextView });
    }
    setOpenTabs(remaining);
  }

  function resetNavigation() {
    lastRoutes.current = {};
    if (window.location.pathname !== '/') window.history.replaceState(null, '', '/');
    const home = locate('/');
    routeRef.current = home;
    setRoute(home);
    setOpenTabs(tabsFor('dashboard'));
    setOpenMenu(null);
  }

  function restoreFromLocation() {
    const target = currentLocation();
    lastRoutes.current = target.notFound ? {} : { [target.view]: { view: target.view, detail: target.detail } };
    routeRef.current = target;
    setRoute(target);
    setOpenTabs(tabsFor(target.view));
    setOpenMenu(null);
  }

  return {
    route,
    activeView: route.view,
    openTabs,
    openMenu,
    navigate,
    setActiveView,
    setOpenMenu,
    openTab,
    closeTab,
    resetNavigation,
    restoreFromLocation
  };
}

function currentLocation(): LocatedRoute {
  return locate(window.location.pathname);
}

function tabsFor(view: View): View[] {
  return view === 'dashboard' ? ['dashboard'] : ['dashboard', view];
}
