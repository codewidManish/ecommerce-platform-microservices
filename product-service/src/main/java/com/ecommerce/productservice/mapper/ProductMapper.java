package com.ecommerce.productservice.mapper;

import com.ecommerce.productservice.dto.*;
import com.ecommerce.productservice.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "category",     ignore = true)
    @Mapping(target = "discountPercent", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "totalReviews",  ignore = true)
    @Mapping(target = "createdAt",     ignore = true)
    @Mapping(target = "updatedAt",     ignore = true)
    Product toEntity(CreateProductRequest request);

    @Mapping(target = "categoryId",   source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    ProductResponse toResponse(Product product);

    @Mapping(target = "categoryId",   source = "categoryId")
    @Mapping(target = "categoryName", source = "categoryName")
    ProductResponse documentToResponse(ProductDocument document);
}
