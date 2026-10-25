package it.giovannidefilippo.gestionale.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    Optional<ProductCategory> findByCodeCanonical(String codeCanonical);

    boolean existsByCodeCanonical(String codeCanonical);

    boolean existsByParentKeyAndNameCanonicalAndIdNot(long parentKey, String nameCanonical, Long id);

    boolean existsByParentKeyAndNameCanonical(long parentKey, String nameCanonical);

    List<ProductCategory> findByParentId(Long parentId);
}
