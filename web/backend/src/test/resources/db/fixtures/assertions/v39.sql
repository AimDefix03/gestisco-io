do $$
declare
    invented_attributes bigint;
    invented_values bigint;
begin
    select count(*) into invented_attributes from product_attributes;
    if invented_attributes <> 0 then
        raise exception 'V39 must not invent product attributes for historical data';
    end if;

    select count(*) into invented_values from product_attribute_values;
    if invented_values <> 0 then
        raise exception 'V39 must not assign attribute values to historical products';
    end if;
end $$;
