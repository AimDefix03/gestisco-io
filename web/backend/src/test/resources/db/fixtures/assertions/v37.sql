do $$
declare
    invented_barcodes bigint;
    non_piece bigint;
begin
    select count(*) into invented_barcodes from products where barcode is not null or barcode_canonical is not null;
    if invented_barcodes <> 0 then
        raise exception 'V37 must not invent barcodes for historical products';
    end if;

    select count(*) into non_piece from products where unit_of_measure <> 'PIECE';
    if non_piece <> 0 then
        raise exception 'V37 must default historical products to PIECE';
    end if;
end $$;
