package com.example.backend.service;

import com.example.backend.dto.request.ProductCreationRequest;
import com.example.backend.dto.request.ProductImageUploadRequest;
import com.example.backend.dto.response.ProductResponse;
import com.example.backend.entity.Category;
import com.example.backend.entity.Product;
import com.example.backend.entity.ProductImage;
import com.example.backend.entity.Shop;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.mapper.ProductMapper;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.ProductImageRepository;
import com.example.backend.repository.ProductRepository;
import com.example.backend.repository.ShopRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductService {
    ProductRepository productRepository;
    ShopRepository shopRepository;
    ProductMapper productMapper;
    CategoryRepository categoryRepository;
    ProductImageRepository productImageRepository;
    ProductImageService productImageService;

    public ProductResponse createProduct(ProductCreationRequest request) {
        Shop shop = shopRepository.findById(request.getShopId()).orElseThrow(()->new AppException(ErrorCode.SHOP_NOT_EXIST));

        // kiểm tra xem category ồn tại chưa, nếu chưa thì thêm mới vào bảng category
        Set<Category> categories = request.getCategoryNames().stream()
                .map(rawName -> {
                    String normalized = normalizeCategory(rawName);
                    return categoryRepository.findByName(normalized)
                            .orElseGet(() -> categoryRepository.save(
                                    Category.builder().name(normalized).build()
                            ));
                })
                .collect(Collectors.toSet());

        Product product = productMapper.toProduct(request);
        product.setShop(shop);
        product.setCategories(categories);

        Product saved = productRepository.save(product);


        return productMapper.toProductResponse(saved);
    }

    public List<ProductResponse> getAllProducts(String shopId) {
        Shop shop = shopRepository.findById(shopId).orElseThrow(()->new AppException(ErrorCode.SHOP_NOT_EXIST));
        return productRepository.findAllByShop(shop).stream().map(productMapper::toProductResponse).collect(Collectors.toList());
    }


    public ProductResponse getProductById(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(()->new AppException(ErrorCode.PRODUCT_NOT_EXIST));
        return productMapper.toProductResponse(product);
    }

    public ProductResponse updateProduct(ProductCreationRequest request, String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(()->new AppException(ErrorCode.PRODUCT_NOT_EXIST));
        productMapper.updateProduct(request,product);
        Set<Category> category = request.getCategoryNames().stream()
                .map(name ->{
                    String normalized = normalizeCategory(name);
                    return categoryRepository.findByName(normalized)
                            .orElseGet(() -> categoryRepository.save(Category.builder().name(normalized).build()));
                })
                .collect(Collectors.toSet());
        product.setCategories(category);
        return productMapper.toProductResponse(productRepository.save(product));
    }

    public void deleteProduct(String productId) {
        var product =  productRepository.findById(productId)
                .orElseThrow(()->new AppException(ErrorCode.PRODUCT_NOT_EXIST));
        productRepository.delete(product);
    }

    public List<ProductResponse> getProductsByCategory(String categoryName) {
        String normalized = normalizeCategory(categoryName);
        return productRepository.findAllByCategories_Name(normalized)
                .stream().map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getProductsByBrand(String brand) {
        return productRepository.findAllByBrand(brand)
                .stream().map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> searchProduct(String keyword) {
        String normalized = normalizeCategory(keyword);
        return productRepository.findByNameContainingIgnoreCase(normalized)
                .stream().map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    private String normalizeCategory(String name) {
        return name.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    public String uploadImages(String productId, MultipartFile file, String imageType, String description) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXIST));

        String url = productImageService.uploadImage(file, "product-upload-preset");

        ProductImage img = ProductImage.builder()
                .imageUrl(url)
                .imageType(imageType)
                .description(description)
                .product(product)
                .build();

        product.getImages().add(img);
        productRepository.save(product);
        return url;
    }

    public List<String> updateProductImages(String productId, List<ProductImageUploadRequest> newImages) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXIST));

        // Xóa ảnh cũ trong DB
        productImageRepository.deleteAll(product.getImages());
        product.getImages().clear();

        List<String> urls = new ArrayList<>();

        for (ProductImageUploadRequest img : newImages) {
            String url = productImageService.uploadImage(img.getFile(), "product-upload-preset");

            ProductImage newImg = ProductImage.builder()
                    .product(product)
                    .imageUrl(url)
                    .imageType(img.getImageType())
                    .description(img.getDescription())
                    .build();

            product.getImages().add(newImg);
            urls.add(url);
        }

        productRepository.save(product);
        return urls;
    }

}
