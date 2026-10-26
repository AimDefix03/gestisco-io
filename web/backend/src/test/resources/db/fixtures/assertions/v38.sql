do $$
declare
    company_rate numeric(5, 4);
    default_rates bigint;
    product_mismatch bigint;
    item_mismatch bigint;
    line_mismatch bigint;
    summary_mismatch bigint;
begin
    select default_vat_rate into company_rate from company_settings where id = 1;

    select count(*) into default_rates from vat_rates where default_rate;
    if default_rates <> 1 then
        raise exception 'V38 must flag exactly one default VAT rate';
    end if;

    select count(*) into product_mismatch
    from products p join vat_rates v on v.id = p.vat_rate_id
    where not v.default_rate or v.rate <> company_rate;
    if product_mismatch <> 0 then
        raise exception 'V38 must assign the historical company VAT rate to every product';
    end if;

    select count(*) into item_mismatch from order_items where vat_rate is null or vat_rate <> company_rate;
    if item_mismatch <> 0 then
        raise exception 'V38 must snapshot the historical company VAT rate on every order line';
    end if;

    select count(*) into line_mismatch
    from fiscal_document_lines l join fiscal_documents d on d.id = l.document_id
    where l.vat_rate is distinct from d.vat_rate;
    if line_mismatch <> 0 then
        raise exception 'V38 must copy the document VAT rate to historical document lines';
    end if;

    select count(*) into summary_mismatch
    from fiscal_documents d
    where (select count(*) from fiscal_document_vat_summaries s where s.document_id = d.id) <> 1
       or exists (
           select 1 from fiscal_document_vat_summaries s
           where s.document_id = d.id
             and (s.vat_rate <> d.vat_rate or s.taxable_amount <> d.taxable_amount or s.vat_amount <> d.vat_amount)
       );
    if summary_mismatch <> 0 then
        raise exception 'V38 must keep one VAT summary per historical document with its original amounts';
    end if;
end $$;
