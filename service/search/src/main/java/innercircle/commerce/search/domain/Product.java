package innercircle.commerce.search.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "products")
@Setting(settingPath = "elasticsearch/settings/product-settings.json")
public class Product {
    
    @Id
    private String id;
    
    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String name;
    
    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String description;
    
    @Field(type = FieldType.Keyword)
    private String status;
    
    @Field(type = FieldType.Long)
    private Long brandId;
    
    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String brandName;
    
    @Field(type = FieldType.Nested)
    private List<Category> categories;
    
    @Field(type = FieldType.Object)
    private Price price;
    
    @Field(type = FieldType.Nested)
    private List<Option> options;
    
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Category {
        @Field(type = FieldType.Long)
        private Long id;
        
        @Field(type = FieldType.Text, analyzer = "nori_analyzer")
        private String name;
        
        @Field(type = FieldType.Keyword)
        private String code;
        
        @Field(type = FieldType.Integer)
        private Integer depth;
        
        @Field(type = FieldType.Long)
        private Long parentId;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Price {
        @Field(type = FieldType.Double)
        private BigDecimal originalPrice;
        
        @Field(type = FieldType.Double)
        private BigDecimal discountRate;
        
        @Field(type = FieldType.Boolean)
        private Boolean isDiscount;
        
        @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss")
        private LocalDateTime discountStartDate;
        
        @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss")
        private LocalDateTime discountEndDate;
        
        @Field(type = FieldType.Double)
        private BigDecimal finalPrice;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Option {
        @Field(type = FieldType.Long)
        private Long id;
        
        @Field(type = FieldType.Text)
        private String groupName;
        
        @Field(type = FieldType.Text)
        private String optionName;
        
        @Field(type = FieldType.Double)
        private BigDecimal additionalPrice;
        
        @Field(type = FieldType.Integer)
        private Integer stock;
    }
}