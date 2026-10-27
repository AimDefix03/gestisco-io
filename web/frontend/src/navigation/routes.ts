import { View } from '../types/ui';

export type WorkspaceRoute = {
  view: View;
  detail?: string;
};

export type LocatedRoute = WorkspaceRoute & {
  path: string;
  notFound: boolean;
};

const VIEW_PATHS = {
  dashboard: '/',
  catalog: '/catalogo',
  sales: '/vendita',
  partners: '/anagrafiche',
  inventory: '/magazzino',
  orders: '/ordini',
  purchases: '/acquisti',
  documents: '/documenti',
  reports: '/report',
  accounts: '/amministrazione/account',
  company: '/amministrazione/azienda',
  monitoring: '/amministrazione/monitoraggio',
  audit: '/amministrazione/audit'
} satisfies Record<View, string>;

type DetailRule = {
  prefix: string;
  valid: (detail: string) => boolean;
};

const DETAIL_RULES: Partial<Record<View, DetailRule>> = {
  orders: { prefix: VIEW_PATHS.orders, valid: (detail) => detail.length > 0 },
  purchases: { prefix: VIEW_PATHS.purchases, valid: (detail) => detail.length > 0 },
  inventory: { prefix: `${VIEW_PATHS.inventory}/inventari`, valid: (detail) => /^[1-9]\d{0,17}$/.test(detail) }
};

export function pathForRoute(route: WorkspaceRoute): string {
  const rule = DETAIL_RULES[route.view];
  if (route.detail && rule) return `${rule.prefix}/${encodeURIComponent(route.detail)}`;
  return VIEW_PATHS[route.view];
}

export function parsePath(pathname: string): WorkspaceRoute | null {
  const path = normalize(pathname);
  for (const view of Object.keys(VIEW_PATHS) as View[]) {
    if (VIEW_PATHS[view] === path) return { view };
  }
  for (const [view, rule] of Object.entries(DETAIL_RULES) as [View, DetailRule][]) {
    if (!path.startsWith(`${rule.prefix}/`)) continue;
    const segment = path.slice(rule.prefix.length + 1);
    if (segment.includes('/')) continue;
    const detail = decodeSegment(segment);
    if (detail !== null && rule.valid(detail)) return { view, detail };
  }
  return null;
}

export function locate(pathname: string): LocatedRoute {
  const route = parsePath(pathname);
  return route
    ? { ...route, path: pathForRoute(route), notFound: false }
    : { view: 'dashboard', path: pathname, notFound: true };
}

export function supportsDetail(view: View): boolean {
  return Boolean(DETAIL_RULES[view]);
}

function normalize(pathname: string): string {
  const trimmed = pathname.replace(/\/+$/, '');
  return trimmed === '' ? '/' : trimmed;
}

function decodeSegment(segment: string): string | null {
  try {
    return decodeURIComponent(segment);
  } catch {
    return null;
  }
}
