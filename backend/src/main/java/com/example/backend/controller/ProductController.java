package com.example.backend.controller;

import com.example.backend.dto.request.ProductCreationRequest;
import com.example.backend.dto.request.ProductImageUploadRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.ProductImageResponse;
import com.example.backend.dto.response.ProductResponse;
import com.example.backend.service.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductController {
    ProductService productService;

    @PostMapping
    ApiResponse addProduct(@RequestBody ProductCreationRequest request) {
        return ApiResponse.builder()
                .result(productService.createProduct(request))
                .build();
    }
    //OK
    @GetMapping
    public ApiResponse<List<ProductResponse>> getAllProducts() {
        return ApiResponse.<List<ProductResponse>>builder()
                .result(productService.getAllProductsGlobal())
                .build();
    }
    @GetMapping("/shop/{shopId}")
    ApiResponse<List<ProductResponse>> getAllProductsByShopId(@PathVariable String shopId) {
        return ApiResponse.<List<ProductResponse>>builder()
                .result(productService.getAllProducts(shopId))
                .build();
    }
    //OK


    @GetMapping("/{productId}")
    ApiResponse<ProductResponse> getProductById(@PathVariable String productId) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.getProductById(productId))
                .build();
    }
    //OK

    @PutMapping("/{productId}")
    ApiResponse<ProductResponse> updateProduct(@PathVariable String productId,
                                               @RequestBody ProductCreationRequest request) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.updateProduct(request, productId))
                .build();
    }
    //OK

    @DeleteMapping("/{productId}")
    ApiResponse<String> deleteProduct(@PathVariable String productId) {
        productService.deleteProduct(productId);
        return ApiResponse.<String>builder()
                .result("Product deleted")
                .build();
    }
    //OK

    @GetMapping("/category/{categoryName}")
    ApiResponse<List<ProductResponse>> getProductsByCategory(@PathVariable String categoryName) {
        return ApiResponse.<List<ProductResponse>>builder()
                .result(productService.getProductsByCategory(categoryName))
                .build();
    }
    //OK

    @GetMapping("/brand/{brand}")
    ApiResponse<List<ProductResponse>> getProductsByBrand(@PathVariable String brand) {
        return ApiResponse.<List<ProductResponse>>builder()
                .result(productService.getProductsByBrand(brand))
                .build();
    }
    //OK

    @GetMapping("/search/{keyword}")
    ApiResponse<List<ProductResponse>> searchProduct(@PathVariable String keyword) {
        return ApiResponse.<List<ProductResponse>>builder()
                .result(productService.searchProduct(keyword))
                .build();
    }
    //OK

    @PostMapping("/{productId}/images/upload")
    public ApiResponse<List<ProductImageResponse>> uploadImages(
            @PathVariable String productId,
            @RequestPart("files") List<MultipartFile> files
    ) {
        List<ProductImageResponse> responses = productService.uploadImages(productId, files);
        return ApiResponse.<List<ProductImageResponse>>builder().result(responses).build();
    }


    @PostMapping("/{productId}/images/update")
    public ApiResponse<List<ProductImageResponse>> updateImages(
            @PathVariable String productId,
            @RequestPart("files") List<MultipartFile> files
    ) {
        List<ProductImageUploadRequest> requests = new ArrayList<>();
        for (MultipartFile file : files) {
            ProductImageUploadRequest req = new ProductImageUploadRequest();
            req.setFile(file);
            requests.add(req);
        }

        List<ProductImageResponse> responses = productService.updateProductImages(productId, requests);
        return ApiResponse.<List<ProductImageResponse>>builder().result(responses).build();
    }


}
