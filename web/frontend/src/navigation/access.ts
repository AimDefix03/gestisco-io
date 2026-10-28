import { UserPermission } from '../api';
import { View } from '../types/ui';

const VIEW_PERMISSIONS: Partial<Record<View, UserPermission>> = {
  sales: 'CREATE_ORDERS',
  partners: 'VIEW_PARTNERS',
  inventory: 'MANAGE_INVENTORY',
  purchases: 'VIEW_PURCHASE_ORDERS',
  documents: 'MANAGE_DOCUMENTS',
  reports: 'VIEW_REPORTS',
  accounts: 'MANAGE_ACCOUNTS',
  company: 'MANAGE_COMPANY_SETTINGS',
  monitoring: 'VIEW_AUDIT',
  audit: 'VIEW_AUDIT'
};

export function canOpenView(view: View, permissions: readonly UserPermission[]): boolean {
  const required = VIEW_PERMISSIONS[view];
  return !required || permissions.includes(required);
}
