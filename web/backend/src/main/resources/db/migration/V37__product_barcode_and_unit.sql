-- P1.3 (ADR 0008): codice a barre facoltativo e univoco, unita di misura per prodotto.
-- Con le varianti (P2) il codice a barre passera alla singola variante.

alter table products add column barcode varchar(32);
alter table products add column barcode_canonical varchar(32);
alter table products add column unit_of_measure varchar(16) default 'PIECE' not null;

alter table products add constraint uk_products_barcode unique (barcode_canonical);
alter table products add constraint chk_products_barcode check (
    (barcode is null and barcode_canonical is null)
    or (char_length(trim(barcode)) > 0 and barcode_canonical = lower(trim(barcode)))
);
alter table products add constraint chk_products_unit_of_measure check (
    unit_of_measure in ('PIECE', 'PAIR', 'PACK', 'KILOGRAM', 'METER', 'LITER')
);
