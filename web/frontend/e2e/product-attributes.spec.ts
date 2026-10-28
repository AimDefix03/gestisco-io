import { randomUUID } from 'node:crypto';
import { readFile } from 'node:fs/promises';
import { expect, Page, test } from '@playwright/test';

const adminUsername = process.env.E2E_USERNAME ?? process.env.GESTIONALE_BOOTSTRAP_SUPER_ADMIN_USERNAME;
const adminPassword = process.env.E2E_PASSWORD ?? process.env.GESTIONALE_BOOTSTRAP_SUPER_ADMIN_PASSWORD;
const runId = randomUUID().slice(0, 8).toUpperCase();
const attributeLabel = `Materiale ${runId}`;
const cottonProduct = `ATT-C-${runId}`;
const linenProduct = `ATT-L-${runId}`;

test.beforeAll(() => {
  if (!adminUsername || !adminPassword) {
    throw new Error('Imposta E2E_USERNAME ed E2E_PASSWORD oppure le credenziali bootstrap del super admin.');
  }
});

test('un attributo definito da UI si usa nel prodotto, nel filtro del catalogo e nell export', async ({ page }) => {
  const runtimeErrors = collectRuntimeErrors(page);
  await page.goto('/catalogo');
  await page.getByLabel('Username').fill(adminUsername!);
  await page.getByLabel('Password').fill(adminPassword!);
  await page.getByRole('button', { name: 'Accedi' }).click();
  await expect(page.locator('.workspace-header h1')).toHaveText('Catalogo prodotti');

  const manager = page.locator('section').filter({ has: page.getByRole('heading', { name: 'Attributi', exact: true }) });
  await manager.getByLabel('Codice attributo').fill(`MAT-${runId}`);
  await manager.getByLabel('Etichetta').fill(attributeLabel);
  await manager.getByLabel('Valori ammessi (uno per riga)').fill('Cotone\nLino');
  await manager.getByRole('button', { name: 'Crea attributo' }).click();
  await expect(manager.getByRole('status')).toHaveText(`Creato l'attributo ${attributeLabel}.`);

  await createProduct(page, cottonProduct, 'Cotone');
  await createProduct(page, linenProduct, 'Lino');

  const filters = page.locator('section.catalog-panel');
  await filters.getByRole('combobox', { name: 'Attributo', exact: true }).selectOption({ label: attributeLabel });
  await filters.getByRole('combobox', { name: 'Valore attributo' }).selectOption('Cotone');
  await expect(page.getByRole('row').filter({ hasText: cottonProduct })).toBeVisible();
  await expect(page.getByRole('row').filter({ hasText: linenProduct })).toHaveCount(0);

  await page.goto('/report');
  await expect(page.locator('.workspace-header h1')).toHaveText('Report');
  await page.getByLabel('Tipo di report').getByRole('button', { name: 'Magazzino', exact: true }).click();
  const reportFilters = page.locator('.inventory-report-filters');
  await reportFilters.getByRole('combobox', { name: 'Attributo', exact: true }).selectOption({ label: attributeLabel });
  await reportFilters.getByRole('combobox', { name: 'Valore attributo' }).selectOption('Lino');
  await expect(page.getByRole('row').filter({ hasText: linenProduct })).toContainText(`${attributeLabel}: Lino`);
  await expect(page.getByRole('row').filter({ hasText: cottonProduct })).toHaveCount(0);
  const downloadPromise = page.waitForEvent('download');
  await page.getByRole('button', { name: 'CSV' }).click();
  const csv = await readFile((await (await downloadPromise).path())!, 'utf8');
  expect(csv).toContain(attributeLabel);
  expect(csv).toContain(linenProduct);
  expect(csv).not.toContain(cottonProduct);
  expect(runtimeErrors).toEqual([]);
});

async function createProduct(page: Page, code: string, material: string) {
  const form = page.locator('form.product-form');
  await form.getByLabel('Codice', { exact: true }).fill(code);
  await form.getByLabel('Nome').fill(`Camicia ${code}`);
  await form.getByLabel('Categoria').selectOption('HARDWARE');
  await form.getByLabel(attributeLabel).selectOption(material);
  await form.getByLabel('Brand').fill('E2E Attributi');
  await form.getByLabel('Tipo prodotto').fill('Camicia');
  await form.getByLabel('Prezzo').fill('39.90');
  await form.getByLabel('Sconto %').fill('0');
  await form.getByLabel('Descrizione').fill('Prodotto con attributo materiale.');
  await form.getByRole('button', { name: 'Crea prodotto' }).click();
  await expect(page.getByRole('status').filter({ hasText: `Prodotto ${code} creato.` })).toBeVisible();
}

function collectRuntimeErrors(page: Page) {
  const errors: string[] = [];
  page.on('pageerror', (error) => errors.push(error.message));
  page.on('console', (message) => {
    if (message.type() === 'error') errors.push(message.text());
  });
  return errors;
}
