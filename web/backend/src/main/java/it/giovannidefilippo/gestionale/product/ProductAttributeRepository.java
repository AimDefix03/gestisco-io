package it.giovannidefilippo.gestionale.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long> {
    Optional<ProductAttribute> findByCodeCanonical(String codeCanonical);

    boolean existsByCodeCanonical(String codeCanonical);

    @Query(value = "select count(*) from product_attribute_values where attribute_id = :attributeId", nativeQuery = true)
    long countValues(@Param("attributeId") Long attributeId);

    @Query(value = "select distinct lower(trim(attribute_value)) from product_attribute_values where attribute_id = :attributeId", nativeQuery = true)
    List<String> findCanonicalValuesInUse(@Param("attributeId") Long attributeId);

    @Query(value = "select distinct p.category_id from product_attribute_values v join products p on p.id = v.product_id where v.attribute_id = :attributeId", nativeQuery = true)
    List<Long> findCategoryIdsInUse(@Param("attributeId") Long attributeId);
}
