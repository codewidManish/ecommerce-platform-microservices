package com.ecommerce.productservice.repository;

import com.ecommerce.productservice.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    Page<Product> findByCategoryIdAndStatus(Long categoryId, Product.ProductStatus status, Pageable pageable);

    Page<Product> findByStatus(Product.ProductStatus status, Pageable pageable);

    @Query("""
        SELECT p FROM Product p
        JOIN FETCH p.category c
        WHERE p.status = 'ACTIVE'
          AND (:minPrice IS NULL OR p.price >= :minPrice)
          AND (:maxPrice IS NULL OR p.price <= :maxPrice)
          AND (:brand IS NULL OR LOWER(p.brand) = LOWER(:brand))
          AND (:categoryId IS NULL OR c.id = :categoryId)
        """)
    Page<Product> findWithFilters(
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("brand") String brand,
        @Param("categoryId") Long categoryId,
        Pageable pageable
    );

    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.status = 'ACTIVE' ORDER BY p.brand")
    List<String> findAllBrands();

    @Query("SELECT p FROM Product p WHERE p.averageRating >= :minRating AND p.status = 'ACTIVE' ORDER BY p.averageRating DESC")
    Page<Product> findTopRated(@Param("minRating") BigDecimal minRating, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.vendorId = :vendorId AND p.status = :status")
    Page<Product> findByVendorIdAndStatus(@Param("vendorId") Long vendorId, @Param("status") Product.ProductStatus status, Pageable pageable);
}
