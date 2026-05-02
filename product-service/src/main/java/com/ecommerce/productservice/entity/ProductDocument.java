package com.ecommerce.productservice.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Document(indexName = "products")
@Setting(settingPath = "elasticsearch/settings.json")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ProductDocument {

    @Id
    private String id;

    @Field(type = FieldType.Long, name = "product_id")
    private Long productId;

    @Field(type = FieldType.Text, analyzer = "standard",
           searchAnalyzer = "standard")
    private String name;

    @Field(type = FieldType.Text, analyzer = "english")
    private String description;

    @Field(type = FieldType.Keyword)
    private String sku;

    @Field(type = FieldType.Double)
    private BigDecimal price;

    @Field(type = FieldType.Keyword)
    private String categoryName;

    @Field(type = FieldType.Long)
    private Long categoryId;

    @Field(type = FieldType.Keyword)
    private String brand;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @Field(type = FieldType.Double, name = "average_rating")
    private BigDecimal averageRating;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Text)
    private String imageUrl;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createdAt;
}
