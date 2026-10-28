package it.giovannidefilippo.gestionale.product;

import java.util.List;

public interface InventoryReportingUsage {
    default List<InventoryProductReportSource> findForReport(
            String query,
            String category,
            String stock,
            Boolean discontinued,
            int maxRows
    ) {
        return findForReport(query, category, stock, discontinued, null, null, maxRows);
    }

    List<InventoryProductReportSource> findForReport(
            String query,
            String category,
            String stock,
            Boolean discontinued,
            String attribute,
            String attributeValue,
            int maxRows
    );

    default List<InventoryReportAttribute> reportAttributes() {
        return List.of();
    }
}
