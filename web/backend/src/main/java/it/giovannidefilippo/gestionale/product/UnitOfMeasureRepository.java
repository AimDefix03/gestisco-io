package it.giovannidefilippo.gestionale.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, Long> {
    Optional<UnitOfMeasure> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByLabelIgnoreCase(String label);

    boolean existsByLabelIgnoreCaseAndIdNot(String label, Long id);

    @Query("select p.unitOfMeasure.code, count(p) from Product p group by p.unitOfMeasure.code")
    List<Object[]> countProductsByCode();
}
