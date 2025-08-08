package innercircle.commerce.search.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setting(settingPath = "/elasticsearch/product-settings.json")
@Document(indexName = "products")
public class Product {
    
    @Id
    private String id;

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "products_name_analyzer"),
        otherFields = {
            @InnerField(suffix = "auto_complete", type = FieldType.Search_As_You_Type, analyzer = "nori")
        }
    )
    private String name;

    @Field(type = FieldType.Keyword)
    private String code;

    @Field(type = FieldType.Integer)
    private Integer price;
    
    @Field(type = FieldType.Keyword)
    private String productStatus;

    @Field(type = FieldType.Keyword)
    private String saleType;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "products_category_analyzer"),
            otherFields = {
                    @InnerField(suffix = "raw", type = FieldType.Keyword)
            }
    )
    private List<String> categories;

    @Field(type = FieldType.Text, analyzer = "products_detail_content_analyzer")
    private String detailContent;

    @Field(type = FieldType.Integer)
    private Integer stock;

    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date)
    private LocalDateTime updatedAt;

}