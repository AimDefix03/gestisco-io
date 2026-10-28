do $$
declare
    seeded_units bigint;
    orphan_units bigint;
    changed_units bigint;
begin
    select count(*) into seeded_units from units_of_measure
    where active and code in ('PIECE', 'PAIR', 'PACK', 'KILOGRAM', 'METER', 'LITER');
    if seeded_units <> 6 then
        raise exception 'V40 must seed the six historical units of measure as active';
    end if;

    select count(*) into orphan_units from products p
    where not exists (select 1 from units_of_measure u where u.code = p.unit_of_measure);
    if orphan_units <> 0 then
        raise exception 'V40 must keep every product on an existing unit of measure';
    end if;

    select count(*) into changed_units from products where unit_of_measure <> 'PIECE';
    if changed_units <> 0 then
        raise exception 'V40 must not change the unit of historical products';
    end if;
end $$;
