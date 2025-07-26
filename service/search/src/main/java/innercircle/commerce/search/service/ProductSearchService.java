package innercircle.commerce.search.service;

import innercircle.commerce.search.domain.Product;
import innercircle.commerce.search.dto.ProductSearchRequest;
import innercircle.commerce.search.dto.ProductSearchResponse;
import innercircle.commerce.search.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchService {
    
    private final ProductSearchRepository productSearchRepository;
    
    public ProductSearchResponse searchProducts(ProductSearchRequest request) {
        log.info("상품 검색 요청: keyword={}, categoryIds={}, brandIds={}, minPrice={}, maxPrice={}", 
                request.getKeyword(), request.getCategoryIds(), request.getBrandIds(), 
                request.getMinPrice(), request.getMaxPrice());
        
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        
        try {
            // 일단 간단하게 findAll()로 테스트
            Page<Product> productPage = productSearchRepository.findAll(pageable);
            
            List<ProductSearchResponse.ProductDto> productDtos = productPage.getContent().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            return ProductSearchResponse.builder()
                    .totalElements(productPage.getTotalElements())
                    .totalPages(productPage.getTotalPages())
                    .currentPage(productPage.getNumber())
                    .pageSize(productPage.getSize())
                    .products(productDtos)
                    .build();
        } catch (Exception e) {
            log.error("검색 중 오류 발생", e);
            throw e;
        }
    }
    
    public List<String> getAutocompleteSuggestions(String keyword, int size) {
        log.info("자동완성 요청: keyword={}, size={}", keyword, size);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        
        return productSearchRepository.getAutocompleteSuggestions(keyword, size);
    }
    
    public void indexProduct(Product product) {
        log.info("상품 인덱싱: productId={}", product.getId());
        productSearchRepository.save(product);
    }
    
    public void deleteProduct(String productId) {
        log.info("상품 삭제: productId={}", productId);
        productSearchRepository.deleteById(productId);
    }
    
    private ProductSearchResponse.ProductDto convertToDto(Product product) {
        return ProductSearchResponse.ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .status(product.getStatus())
                .brandId(product.getBrandId())
                .brandName(product.getBrandName())
                .categories(convertCategoryDtos(product.getCategories()))
                .price(convertPriceDto(product.getPrice()))
                .options(convertOptionDtos(product.getOptions()))
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
    
    private List<ProductSearchResponse.CategoryDto> convertCategoryDtos(List<Product.Category> categories) {
        if (categories == null) {
            return List.of();
        }
        
        return categories.stream()
                .map(category -> ProductSearchResponse.CategoryDto.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .code(category.getCode())
                        .depth(category.getDepth())
                        .parentId(category.getParentId())
                        .build())
                .collect(Collectors.toList());
    }
    
    private ProductSearchResponse.PriceDto convertPriceDto(Product.Price price) {
        if (price == null) {
            return null;
        }
        
        return ProductSearchResponse.PriceDto.builder()
                .originalPrice(price.getOriginalPrice())
                .discountRate(price.getDiscountRate())
                .isDiscount(price.getIsDiscount())
                .discountStartDate(price.getDiscountStartDate())
                .discountEndDate(price.getDiscountEndDate())
                .finalPrice(price.getFinalPrice())
                .build();
    }
    
    private List<ProductSearchResponse.OptionDto> convertOptionDtos(List<Product.Option> options) {
        if (options == null) {
            return List.of();
        }
        
        return options.stream()
                .map(option -> ProductSearchResponse.OptionDto.builder()
                        .id(option.getId())
                        .groupName(option.getGroupName())
                        .optionName(option.getOptionName())
                        .additionalPrice(option.getAdditionalPrice())
                        .stock(option.getStock())
                        .build())
                .collect(Collectors.toList());
    }
}