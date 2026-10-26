do $$
declare
    uncategorized bigint;
    seeded bigint;
    legacy_column bigint;
begin
    select count(*) into uncategorized from products where category_id is null;
    if uncategorized <> 0 then
        raise exception 'V36 must assign a category to every historical product';
    end if;

    select count(*) into seeded from product_categories where code_canonical in ('hardware', 'software') and parent_id is null and active;
    if seeded <> 2 then
        raise exception 'V36 must keep HARDWARE and SOFTWARE as active root categories';
    end if;

    select count(*) into legacy_column from information_schema.columns
    where table_schema = 'public' and table_name = 'products' and column_name = 'category';
    if legacy_column <> 0 then
        raise exception 'V36 must replace products.category with category_id';
    end if;
end $$;
