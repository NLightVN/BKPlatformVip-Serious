package com.example.backend.service;

import com.example.backend.dto.request.ProductCreationRequest;
import com.example.backend.dto.request.ProductImageUploadRequest;
import com.example.backend.dto.response.ProductImageResponse;
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
import com.example.backend.util.SecurityUtil;
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
    public List<ProductResponse> getAllProducts() {
        //chi hien product active va shop active
        return productRepository.findAll().stream()
                .filter(p -> "ACTIVE".equals(p.getStatus()) &&
                            p.getShop() != null &&
                            "ACTIVE".equals(p.getShop().getStatus()))
                .map(productMapper::toProductResponse)
                .toList();
    }
    
    //admin: hien ca product ke ca banned, khong hien deleted
    public List<ProductResponse> getAllProductsGlobal() {
        return productRepository.findAll().stream()
                .filter(product -> !"DELETED".equals(product.getStatus()))  
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }
    
    public ProductResponse createProduct(ProductCreationRequest request) {
        String currentUsername = SecurityUtil.getCurrentUsername();
        Shop shop = shopRepository.findById(request.getShopId()).orElseThrow(()->new AppException(ErrorCode.SHOP_NOT_EXIST));

        if (!SecurityUtil.hasRole("ADMIN") && !shop.getOwner().getUsername().equals(currentUsername)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // kiểm tra xem category ồn tại chưa, nếu chưa thì thêm mới vào bảng category
        Set<Category> categories = request.getCategoryNames().stream()
                .map(rawName -> {
                    String normalized = normalize(rawName);
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
        return productRepository.findAllByShop(shop).stream()
                .filter(product -> !"DELETED".equals(product.getStatus()))
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }


    public ProductResponse getProductById(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(()->new AppException(ErrorCode.PRODUCT_NOT_EXIST));
        return productMapper.toProductResponse(product);
    }

    public ProductResponse updateProduct(ProductCreationRequest request, String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(()->new AppException(ErrorCode.PRODUCT_NOT_EXIST));

        String currentUsername = SecurityUtil.getCurrentUsername();
        if (!SecurityUtil.hasRole("ADMIN") &&
                !product.getShop().getOwner().getUsername().equals(currentUsername)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        productMapper.updateProduct(request,product);
        Set<Category> category = request.getCategoryNames().stream()
                .map(name ->{
                    String normalized = normalize(name);
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

        String currentUsername = SecurityUtil.getCurrentUsername();
        if (!SecurityUtil.hasRole("ADMIN") &&
                !product.getShop().getOwner().getUsername().equals(currentUsername)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        //soft delete
        product.setStatus("DELETED");
        productRepository.save(product);
    }

    public List<ProductResponse> getProductsByCategory(String categoryName) {
        String normalized = normalize(categoryName);
        return productRepository.findAllByCategories_Name(normalized)
                .stream()
                .filter(product -> !"DELETED".equals(product.getStatus()))
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getProductsByBrand(String brand) {
        return productRepository.findAllByBrandIgnoreCase(brand)
                .stream()
                .filter(product -> !"DELETED".equals(product.getStatus()))
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getProductsByShop(String shopId) {
        //lay product cua shop, loc theo status
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_EXIST));
        
        List<Product> products = productRepository.findAllByShop(shop);
        
        return products.stream()
                .filter(p -> "ACTIVE".equals(p.getStatus()) && 
                            p.getShop() != null && 
                            "ACTIVE".equals(p.getShop().getStatus()))
                .map(productMapper::toProductResponse)
                .toList();
    }

    public List<ProductResponse> searchProduct(String keyword) {
        String normalized = normalize(keyword);
        return productRepository.findByNameContainingIgnoreCase(normalized)
                .stream()
                .filter(product -> !"DELETED".equals(product.getStatus()))
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    private String normalize(String name) {
        return name.trim().toLowerCase().replaceAll("\\s+", " ");
    }




    public List<ProductImageResponse> uploadImages(String productId, List<MultipartFile> files) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXIST));

        //check quyen
        String currentUsername = SecurityUtil.getCurrentUsername();
        if (!SecurityUtil.hasRole("ADMIN") &&
                !product.getShop().getOwner().getUsername().equals(currentUsername)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        List<ProductImageResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            String url = productImageService.uploadImage(file, "product-upload-preset");

            ProductImage img = ProductImage.builder()
                    .product(product)
                    .imageUrl(url)
                    .build();

            //khoi tao images set neu null
            if (product.getImages() == null) {
                product.setImages(new java.util.HashSet<>());
            }
            product.getImages().add(img);

            responses.add(ProductImageResponse.builder()
                    .imageUrl(url)
                    .build());
        }

        productRepository.save(product);
        return responses;
    }

    public List<ProductImageResponse> updateProductImages(String productId, List<ProductImageUploadRequest> newImages) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXIST));

        //check quyen
        String currentUsername = SecurityUtil.getCurrentUsername();
        if (!SecurityUtil.hasRole("ADMIN") &&
                !product.getShop().getOwner().getUsername().equals(currentUsername)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        //xoa anh cu
        productImageRepository.deleteAll(product.getImages());
        product.getImages().clear();

        List<ProductImageResponse> responses = new ArrayList<>();

        for (ProductImageUploadRequest img : newImages) {
            String url = productImageService.uploadImage(img.getFile(), "product-upload-preset");

            ProductImage newImg = ProductImage.builder()
                    .product(product)
                    .imageUrl(url)
                    .build();

            product.getImages().add(newImg);

            responses.add(ProductImageResponse.builder()
                    .imageUrl(url)
                    .build());
        }

        productRepository.save(product);
        return responses;
    }

    //ban product - admin only
    public ProductResponse banProduct(String productId) {
        SecurityUtil.requireAdmin();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXIST));
        
        product.setStatus("BANNED");
        return productMapper.toProductResponse(productRepository.save(product));
    }

    //unban product - admin only
    public ProductResponse unbanProduct(String productId) {
        SecurityUtil.requireAdmin();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXIST));
        
        product.setStatus("ACTIVE");
        return productMapper.toProductResponse(productRepository.save(product));
    }

}
