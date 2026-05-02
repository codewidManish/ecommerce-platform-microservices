package com.ecommerce.productservice.mapper;

import com.ecommerce.productservice.dto.CreateProductRequest;
import com.ecommerce.productservice.dto.ProductResponse;
import com.ecommerce.productservice.entity.Category;
import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.entity.ProductDocument;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-02T20:54:52+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public Product toEntity(CreateProductRequest request) {
        if ( request == null ) {
            return null;
        }

        Product.ProductBuilder product = Product.builder();

        List<String> list = request.getAdditionalImages();
        if ( list != null ) {
            product.additionalImages( new ArrayList<String>( list ) );
        }
        product.brand( request.getBrand() );
        product.description( request.getDescription() );
        product.imageUrl( request.getImageUrl() );
        product.name( request.getName() );
        product.originalPrice( request.getOriginalPrice() );
        product.price( request.getPrice() );
        product.sku( request.getSku() );
        List<String> list1 = request.getTags();
        if ( list1 != null ) {
            product.tags( new ArrayList<String>( list1 ) );
        }
        product.vendorId( request.getVendorId() );
        product.weightGrams( request.getWeightGrams() );

        return product.build();
    }

    @Override
    public ProductResponse toResponse(Product product) {
        if ( product == null ) {
            return null;
        }

        ProductResponse.ProductResponseBuilder productResponse = ProductResponse.builder();

        productResponse.categoryId( productCategoryId( product ) );
        productResponse.categoryName( productCategoryName( product ) );
        List<String> list = product.getAdditionalImages();
        if ( list != null ) {
            productResponse.additionalImages( new ArrayList<String>( list ) );
        }
        productResponse.averageRating( product.getAverageRating() );
        productResponse.brand( product.getBrand() );
        productResponse.createdAt( product.getCreatedAt() );
        productResponse.description( product.getDescription() );
        productResponse.discountPercent( product.getDiscountPercent() );
        productResponse.id( product.getId() );
        productResponse.imageUrl( product.getImageUrl() );
        productResponse.name( product.getName() );
        productResponse.originalPrice( product.getOriginalPrice() );
        productResponse.price( product.getPrice() );
        productResponse.sku( product.getSku() );
        if ( product.getStatus() != null ) {
            productResponse.status( product.getStatus().name() );
        }
        List<String> list1 = product.getTags();
        if ( list1 != null ) {
            productResponse.tags( new ArrayList<String>( list1 ) );
        }
        productResponse.totalReviews( product.getTotalReviews() );
        productResponse.updatedAt( product.getUpdatedAt() );

        return productResponse.build();
    }

    @Override
    public ProductResponse documentToResponse(ProductDocument document) {
        if ( document == null ) {
            return null;
        }

        ProductResponse.ProductResponseBuilder productResponse = ProductResponse.builder();

        productResponse.categoryId( document.getCategoryId() );
        productResponse.categoryName( document.getCategoryName() );
        productResponse.averageRating( document.getAverageRating() );
        productResponse.brand( document.getBrand() );
        productResponse.createdAt( document.getCreatedAt() );
        productResponse.description( document.getDescription() );
        if ( document.getId() != null ) {
            productResponse.id( Long.parseLong( document.getId() ) );
        }
        productResponse.imageUrl( document.getImageUrl() );
        productResponse.name( document.getName() );
        productResponse.price( document.getPrice() );
        productResponse.sku( document.getSku() );
        productResponse.status( document.getStatus() );
        List<String> list = document.getTags();
        if ( list != null ) {
            productResponse.tags( new ArrayList<String>( list ) );
        }

        return productResponse.build();
    }

    private Long productCategoryId(Product product) {
        if ( product == null ) {
            return null;
        }
        Category category = product.getCategory();
        if ( category == null ) {
            return null;
        }
        Long id = category.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String productCategoryName(Product product) {
        if ( product == null ) {
            return null;
        }
        Category category = product.getCategory();
        if ( category == null ) {
            return null;
        }
        String name = category.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
