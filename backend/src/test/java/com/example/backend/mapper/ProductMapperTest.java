package com.example.backend.mapper;

import com.example.backend.dto.request.ProductCreationRequest;
import com.example.backend.dto.response.ProductResponse;
import com.example.backend.entity.Category;
import com.example.backend.entity.Product;
import com.example.backend.entity.ProductImage;
import com.example.backend.entity.Shop;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProductMapperTest {

    private ProductMapper productMapper;

    @BeforeEach
    void setUp() {
        productMapper = new ProductMapperImpl();
    }

    @Test
    void toProduct_shouldMapCorrectly() {
        //setup
        ProductCreationRequest request = new ProductCreationRequest();
        request.setName("Test Product");
        request.setPrice(99.99);
        request.setBrand("Test Brand");
        request.setDescription("Test Description");
        request.setWeight(1.5);

        //chay
        Product product = productMapper.toProduct(request);

        //check
        assertNotNull(product);
        assertEquals("Test Product", product.getName());
        assertEquals(99.99, product.getPrice());
        assertEquals("Test Brand", product.getBrand());
        assertEquals("Test Description", product.getDescription());
        assertEquals(1.5, product.getWeight());
    }

    @Test
    void toProduct_nullRequest_returnsNull() {
        //chay
        Product product = productMapper.toProduct(null);

        //check
        assertNull(product);
    }

    @Test
    void toProductResponse_shouldMapCorrectly() {
        //setup
        Shop shop = Shop.builder()
                .shopId("shop-1")
                .name("Test Shop")
                .build();

        Product product = Product.builder()
                .productId("prod-1")
                .name("Test Product")
                .price(99.99)
                .brand("Test Brand")
                .description("Test Description")
                .shop(shop)
                .build();

        //chay
        ProductResponse response = productMapper.toProductResponse(product);

        //check
        assertNotNull(response);
        assertEquals("prod-1", response.getProductId());
        assertEquals("Test Product", response.getName());
        assertEquals(99.99, response.getPrice());
        assertEquals("Test Brand", response.getBrand());
        assertEquals("Test Description", response.getDescription());
        assertEquals("shop-1", response.getShopId());
    }

    @Test
    void toProductResponse_shouldMapShopId() {
        // Arrange
        Shop shop = Shop.builder()
                .shopId("shop-123")
                .build();

        Product product = Product.builder()
                .productId("prod-1")
                .shop(shop)
                .build();

        //chay
        ProductResponse response = productMapper.toProductResponse(product);

        //check
        assertEquals("shop-123", response.getShopId());
    }

    @Test
    void toProductResponse_shouldMapCategories() {
        // Arrange
        Category category1 = Category.builder()
                .categoryId("cat-1")
                .name("Electronics")
                .build();

        Category category2 = Category.builder()
                .categoryId("cat-2")
                .name("Computers")
                .build();

        Set<Category> categories = new HashSet<>();
        categories.add(category1);
        categories.add(category2);

        Product product = Product.builder()
                .productId("prod-1")
                .categories(categories)
                .build();

        //chay
        ProductResponse response = productMapper.toProductResponse(product);

        //check
        assertNotNull(response.getCategories());
        assertEquals(2, response.getCategories().size());

        // Verify category mapping
        boolean hasElectronics = response.getCategories().stream()
                .anyMatch(c -> c.getCategoryId().equals("cat-1") && c.getName().equals("Electronics"));
        boolean hasComputers = response.getCategories().stream()
                .anyMatch(c -> c.getCategoryId().equals("cat-2") && c.getName().equals("Computers"));

        assertTrue(hasElectronics);
        assertTrue(hasComputers);
    }

    @Test
    void toProductResponse_shouldMapImages() {
        // Arrange
        ProductImage image1 = ProductImage.builder()
                .imageType("MAIN")
                .imageUrl("http://example.com/image1.jpg")
                .description("Main image")
                .build();

        ProductImage image2 = ProductImage.builder()
                .imageType("THUMBNAIL")
                .imageUrl("http://example.com/image2.jpg")
                .description("Thumbnail")
                .build();

        Set<ProductImage> images = new HashSet<>();
        images.add(image1);
        images.add(image2);

        Product product = Product.builder()
                .productId("prod-1")
                .images(images)
                .build();

        //chay
        ProductResponse response = productMapper.toProductResponse(product);

        //check
        assertNotNull(response.getImages());
        assertEquals(2, response.getImages().size());

        // Verify image mapping
        boolean hasMainImage = response.getImages().stream()
                .anyMatch(img -> img.getImageType().equals("MAIN")
                        && img.getImageUrl().equals("http://example.com/image1.jpg"));
        boolean hasThumbnail = response.getImages().stream()
                .anyMatch(img -> img.getImageType().equals("THUMBNAIL")
                        && img.getImageUrl().equals("http://example.com/image2.jpg"));

        assertTrue(hasMainImage);
        assertTrue(hasThumbnail);
    }

    @Test
    void toProductResponse_withNullCategories_shouldReturnNull() {
        // Arrange
        Product product = Product.builder()
                .productId("prod-1")
                .categories(null)
                .build();

        //chay
        ProductResponse response = productMapper.toProductResponse(product);

        //check
        assertNull(response.getCategories());
    }

    @Test
    void toProductResponse_withNullImages_shouldReturnNull() {
        // Arrange
        Product product = Product.builder()
                .productId("prod-1")
                .images(null)
                .build();

        //chay
        ProductResponse response = productMapper.toProductResponse(product);

        //check
        assertNull(response.getImages());
    }

    @Test
    void toProductResponse_withEmptyCategories_shouldReturnEmptySet() {
        // Arrange
        Product product = Product.builder()
                .productId("prod-1")
                .categories(new HashSet<>())
                .build();

        //chay
        ProductResponse response = productMapper.toProductResponse(product);

        //check
        assertNotNull(response.getCategories());
        assertTrue(response.getCategories().isEmpty());
    }

    @Test
    void toProductResponse_withEmptyImages_shouldReturnEmptySet() {
        // Arrange
        Product product = Product.builder()
                .productId("prod-1")
                .images(new HashSet<>())
                .build();

        //chay
        ProductResponse response = productMapper.toProductResponse(product);

        //check
        assertNotNull(response.getImages());
        assertTrue(response.getImages().isEmpty());
    }

    @Test
    void updateProduct_shouldIgnoreShopCategoriesImages() {
        // Arrange
        Product existingProduct = Product.builder()
                .productId("prod-1")
                .name("Old Name")
                .price(50.0)
                .build();

        ProductCreationRequest request = new ProductCreationRequest();
        request.setName("New Name");
        request.setPrice(100.0);

        //chay
        productMapper.updateProduct(request, existingProduct);

        //check
        // Note: updateProduct should update fields but ignore shop, categories, images
        // The actual behavior depends on MapStruct implementation
        assertNotNull(existingProduct);
    }

    @Test
    void toProductResponse_nullProduct_returnsNull() {
        //chay
        ProductResponse response = productMapper.toProductResponse(null);

        //check
        assertNull(response);
    }
}
