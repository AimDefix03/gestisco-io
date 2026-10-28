import { randomUUID } from 'node:crypto';
import { expect, Page, test } from '@playwright/test';

const adminUsername = process.env.E2E_USERNAME ?? process.env.GESTIONALE_BOOTSTRAP_SUPER_ADMIN_USERNAME;
const adminPassword = process.env.E2E_PASSWORD ?? process.env.GESTIONALE_BOOTSTRAP_SUPER_ADMIN_PASSWORD;
const runId = randomUUID().slice(0, 8).toUpperCase();
const unitCode = `ROT_${runId}`;
const unitLabel = `Rotolo ${runId}`;
const productCode = `UNI-${runId}`;

test.beforeAll(() => {
  if (!adminUsername || !adminPassword) {
    throw new Error('Imposta E2E_USERNAME ed E2E_PASSWORD oppure le credenziali bootstrap del super admin.');
  }
});

test('un unita di misura definita da UI si assegna al prodotto e, disattivata, non si offre ai nuovi', async ({ page }) => {
  const runtimeErrors = collectRuntimeErrors(page);
  await page.goto('/catalogo');
  await page.getByLabel('Username').fill(adminUsername!);
  await page.getByLabel('Password').fill(adminPassword!);
  await page.getByRole('button', { name: 'Accedi' }).click();
  await expect(page.locator('.workspace-header h1')).toHaveText('Catalogo prodotti');

  const manager = page.locator('section[aria-labelledby="unit-manager-title"]');
  await manager.getByLabel('Codice unita').fill(unitCode);
  await manager.getByLabel('Nome unita').fill(unitLabel);
  await manager.getByLabel('Simbolo').fill('rot');
  await manager.getByRole('button', { name: 'Crea unita' }).click();
  await expect(manager.getByRole('status')).toHaveText(`Creata l'unita ${unitLabel}.`);

  const form = page.locator('form.product-form');
  await form.getByLabel('Codice', { exact: true }).fill(productCode);
  await form.getByLabel('Nome').fill(`Tessuto ${productCode}`);
  await form.getByLabel('Categoria').selectOption('HARDWARE');
  await form.getByLabel('Unita di misura').selectOption({ label: `${unitLabel} (rot)` });
  await form.getByLabel('Brand').fill('E2E Unita');
  await form.getByLabel('Tipo prodotto').fill('Tessuto');
  await form.getByLabel('Prezzo').fill('12.50');
  await form.getByLabel('Sconto %').fill('0');
  await form.getByLabel('Descrizione').fill('Prodotto venduto a rotolo.');
  await form.getByRole('button', { name: 'Crea prodotto' }).click();
  await expect(page.getByRole('status').filter({ hasText: `Prodotto ${productCode} creato.` })).toBeVisible();
  await expect(manager.getByText('1 prodotto')).toBeVisible();

  await manager.getByRole('button', { name: `Modifica unita ${unitLabel}` }).click();
  await manager.getByLabel('Attiva').uncheck();
  await manager.getByRole('button', { name: 'Salva unita' }).click();
  await expect(manager.getByRole('status')).toHaveText(`Aggiornata l'unita ${unitLabel}.`);
  await expect(form.getByLabel('Unita di misura').locator('option', { hasText: unitLabel })).toHaveCount(0);

  await page.getByRole('row').filter({ hasText: productCode }).getByRole('button', { name: 'Modifica' }).click();
  await expect(form.getByLabel('Unita di misura')).toHaveValue(unitCode);
  expect(runtimeErrors).toEqual([]);
});

function collectRuntimeErrors(page: Page) {
  const errors: string[] = [];
  page.on('pageerror', (error) => errors.push(error.message));
  page.on('console', (message) => {
    if (message.type() === 'error') errors.push(message.text());
  });
  return errors;
}
