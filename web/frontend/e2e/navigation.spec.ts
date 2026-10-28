import { randomUUID } from 'node:crypto';
import { APIResponse, expect, Page, Response, test } from '@playwright/test';

type Order = {
  code: string;
};

const adminUsername = process.env.E2E_USERNAME ?? process.env.GESTIONALE_BOOTSTRAP_SUPER_ADMIN_USERNAME;
const adminPassword = process.env.E2E_PASSWORD ?? process.env.GESTIONALE_BOOTSTRAP_SUPER_ADMIN_PASSWORD;
const runId = `${Date.now()}-${randomUUID().slice(0, 8)}`;
const productCode = `NAV-${runId}`;

test.describe.configure({ mode: 'serial' });

test.beforeAll(() => {
  if (!adminUsername || !adminPassword) {
    throw new Error('Imposta E2E_USERNAME ed E2E_PASSWORD oppure le credenziali bootstrap del super admin.');
  }
});

test('ogni sezione ha un indirizzo che regge tasto indietro, link diretti e ricarica', async ({ page }) => {
  const runtimeErrors = collectRuntimeErrors(page);

  await page.goto('/ordini');
  await expect(page).toHaveURL(/\/ordini$/);
  await login(page, 'Ordini cliente');
  await expect(page).toHaveURL(/\/ordini$/);
  const scriptCookies = await page.evaluate(() => document.cookie);
  expect(scriptCookies).not.toContain('__Host-gestionale_session');
  expect(scriptCookies).toContain('__Host-gestionale_xsrf=');
  const order = await createWalkInOrder(page);

  await openMenuEntry(page, 'Workspace', 'Catalogo prodotti');
  await expect(page).toHaveURL(/\/catalogo$/);
  await openMenuEntry(page, 'Amministrazione', 'Audit log');
  await expect(page).toHaveURL(/\/amministrazione\/audit$/);

  await openMenuEntry(page, 'Operazioni', 'Ordini');
  await expect(page).toHaveURL(/\/ordini$/);
  const searchResponse = waitForSearchResponse(page, '/api/orders', order.code);
  await page.getByLabel('Cerca', { exact: true }).fill(order.code);
  await readJson(await searchResponse);
  await page.getByRole('row').filter({ hasText: order.code }).getByRole('button', { name: 'Operazioni' }).click();
  await expect(page).toHaveURL(new RegExp(`/ordini/${order.code}$`));
  await expectOrderDetail(page, order.code);

  await page.goBack();
  await expect(page).toHaveURL(/\/ordini$/);
  await expect(page.locator('.order-operations').getByRole('heading', { name: 'Seleziona un ordine' })).toBeVisible();
  await page.goBack();
  await expect(page).toHaveURL(/\/amministrazione\/audit$/);
  await expect(page.locator('.workspace-header h1')).toHaveText('Audit log');
  await page.goForward();
  await page.goForward();
  await expect(page).toHaveURL(new RegExp(`/ordini/${order.code}$`));
  await expectOrderDetail(page, order.code);

  await page.reload();
  await expect(page.locator('.workspace-header h1')).toHaveText('Ordini cliente');
  await expect(page).toHaveURL(new RegExp(`/ordini/${order.code}$`));
  await expectOrderDetail(page, order.code);

  await page.goto('/amministrazione/audit');
  await expect(page.locator('.workspace-header h1')).toHaveText('Audit log');

  await page.goto('/pagina-inesistente');
  await expect(page.getByRole('heading', { name: 'Pagina non trovata' })).toBeVisible();
  await page.getByRole('button', { name: 'Vai alla dashboard' }).click();
  await expect(page).toHaveURL(/\/$/);

  await page.waitForLoadState('networkidle');
  await page.getByRole('button', { name: 'Logout' }).click();
  await expect(page.getByRole('heading', { name: 'Accedi al workspace' })).toBeVisible();
  await expect(page).toHaveURL(/\/$/);
  await page.goto(`/ordini/${order.code}`);
  await expect(page.getByRole('heading', { name: 'Accedi al workspace' })).toBeVisible();
  expect(runtimeErrors).toEqual([]);
});

test('il tasto indietro chiede conferma quando la scheda ha una bozza', async ({ page }) => {
  const runtimeErrors = collectRuntimeErrors(page);

  await page.goto('/');
  await login(page, 'Dashboard operativa');
  await openMenuEntry(page, 'Workspace', 'Catalogo prodotti');
  await openMenuEntry(page, 'Operazioni', 'Magazzino');
  await expect(page).toHaveURL(/\/magazzino$/);
  await page.getByLabel('Causale').fill('Bozza da conservare');

  await page.goBack();
  const dialog = page.getByRole('dialog', { name: 'Passare a Catalogo prodotti?' });
  await expect(dialog).toBeVisible();
  await dialog.getByRole('button', { name: 'Resta qui' }).click();
  await expect(page).toHaveURL(/\/magazzino$/);
  await expect(page.getByLabel('Causale')).toHaveValue('Bozza da conservare');

  await page.goBack();
  await page.getByRole('dialog', { name: 'Passare a Catalogo prodotti?' }).getByRole('button', { name: 'Cambia scheda' }).click();
  await expect(page).toHaveURL(/\/catalogo$/);
  await expect(page.locator('.workspace-header h1')).toHaveText('Catalogo prodotti');

  await page.getByRole('navigation', { name: 'Schede aperte' }).getByRole('button', { name: /^Magazzino/ }).click();
  await expect(page).toHaveURL(/\/magazzino$/);
  await expect(page.getByLabel('Causale')).toHaveValue('Bozza da conservare');
  expect(runtimeErrors).toEqual([]);
});

async function login(page: Page, expectedHeading: string) {
  await expect(page.getByRole('heading', { name: 'Accedi al workspace' })).toBeVisible();
  await page.getByLabel('Username').fill(adminUsername!);
  await page.getByLabel('Password').fill(adminPassword!);
  const responsePromise = page.waitForResponse((response) => response.request().method() === 'POST' && new URL(response.url()).pathname === '/api/accounts/login');
  await page.getByRole('button', { name: 'Accedi' }).click();
  await readJson(await responsePromise);
  await expect(page.locator('.workspace-header h1')).toHaveText(expectedHeading);
}

async function createWalkInOrder(page: Page): Promise<Order> {
  await readJson(await page.request.post('/api/products', {
    headers: await commandHeaders(page),
    data: {
      code: productCode,
      name: `Prodotto navigazione ${runId}`,
      description: 'Prodotto effimero per il test degli indirizzi per pagina.',
      category: 'HARDWARE',
      brand: 'E2E',
      productType: 'Navigazione',
      usageContext: 'P1.5',
      price: 12.5,
      discount: 0,
      barcode: null,
      unitOfMeasure: 'PIECE',
      vatRateCode: null
    }
  }));
  await readJson(await page.request.post('/api/inventory/initial-balance', {
    headers: { ...await commandHeaders(page), 'Idempotency-Key': `e2e-${randomUUID()}` },
    data: { productCode, quantity: 3, reason: 'Saldo iniziale test navigazione' }
  }));
  return readJson<Order>(await page.request.post('/api/orders', {
    headers: { ...await commandHeaders(page), 'Idempotency-Key': `e2e-${randomUUID()}` },
    data: { customerType: 'WALK_IN', walkInCustomerName: `Cliente navigazione ${runId}`, paymentMethod: 'CASH', items: [{ productCode, quantity: 1 }] }
  }));
}

async function expectOrderDetail(page: Page, orderCode: string) {
  await expect(page.locator('.order-operations').getByRole('heading', { name: orderCode })).toBeVisible();
}

async function openMenuEntry(page: Page, menu: string, entry: string) {
  const mainMenu = page.getByRole('region', { name: 'Menu principale' });
  await mainMenu.getByRole('button', { name: new RegExp(`^${menu}`) }).click();
  await mainMenu.getByRole('button', { name: new RegExp(`^${entry}`) }).click();
}

function waitForSearchResponse(page: Page, pathname: string, q: string): Promise<Response> {
  return page.waitForResponse((response) => {
    const url = new URL(response.url());
    return response.request().method() === 'GET' && url.pathname === pathname && url.searchParams.get('q') === q;
  });
}

async function readJson<T = unknown>(response: Response | APIResponse): Promise<T> {
  const body = await response.text();
  expect(response.ok(), `${response.url()} -> ${response.status()} ${body}`).toBe(true);
  return body ? JSON.parse(body) as T : undefined as T;
}

async function commandHeaders(page: Page) {
  const csrf = (await page.context().cookies()).find((cookie) => cookie.name === '__Host-gestionale_xsrf');
  expect(csrf, 'cookie CSRF assente nel contesto del browser').toBeDefined();
  return {
    'Content-Type': 'application/json',
    'X-XSRF-TOKEN': csrf!.value,
    'X-Request-Id': `e2e-${randomUUID()}`
  };
}

function collectRuntimeErrors(page: Page) {
  const errors: string[] = [];
  page.on('pageerror', (error) => errors.push(error.message));
  page.on('console', (message) => {
    if (message.type() === 'error') errors.push(message.text());
  });
  return errors;
}
