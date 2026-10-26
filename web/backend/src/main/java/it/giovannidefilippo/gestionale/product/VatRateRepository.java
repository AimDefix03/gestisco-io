package it.giovannidefilippo.gestionale.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface VatRateRepository extends JpaRepository<VatRate, Long> {
    Optional<VatRate> findByCodeIgnoreCase(String code);

    List<VatRate> findByDefaultRateTrue();

    List<VatRate> findAllByOrderBySortOrderAscCodeAsc();
}
