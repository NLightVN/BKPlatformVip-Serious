package com.example.backend.controller;

import com.example.backend.dto.request.ProductCreationRequest;
import com.example.backend.dto.request.ProductImageUploadRequest;
import com.example.backend.dto.response.ApiResponse;
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

    @GetMapping("/shop/{shopId}")
    ApiResponse<List<ProductResponse>> getAllProductsByShopId(@PathVariable String shopId) {
        return ApiResponse.<List<ProductResponse>>builder()
                .result(productService.getAllProducts(shopId))
                .build();
    }


    @GetMapping("/{productId}")
    ApiResponse<ProductResponse> getProductById(@PathVariable String productId) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.getProductById(productId))
                .build();
    }

    @PutMapping("/{productId}")
    ApiResponse<ProductResponse> updateProduct(@PathVariable String productId,
                                               @RequestBody ProductCreationRequest request) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.updateProduct(request, productId))
                .build();
    }

    @DeleteMapping("/{productId}")
    ApiResponse<String> deleteProduct(@PathVariable String productId) {
        productService.deleteProduct(productId);
        return ApiResponse.<String>builder()
                .result("Product deleted")
                .build();
    }

    @GetMapping("/category/{categoryName}")
    ApiResponse<List<ProductResponse>> getProductsByCategory(@PathVariable String categoryName) {
        return ApiResponse.<List<ProductResponse>>builder()
                .result(productService.getProductsByCategory(categoryName))
                .build();
    }

    @GetMapping("/brand/{brand}")
    ApiResponse<List<ProductResponse>> getProductsByBrand(@PathVariable String brand) {
        return ApiResponse.<List<ProductResponse>>builder()
                .result(productService.getProductsByBrand(brand))
                .build();
    }

    @GetMapping("/search/{keyword}")
    ApiResponse<List<ProductResponse>> searchProduct(@PathVariable String keyword) {
        return ApiResponse.<List<ProductResponse>>builder()
                .result(productService.searchProduct(keyword))
                .build();
    }

    @PostMapping("/{productId}/images/formdata")
    public ApiResponse<List<String>> uploadImagesFormData(
            @PathVariable String productId,
            @RequestPart("files") List<MultipartFile> files,
            @RequestParam("imageType") List<String> imageTypes,
            @RequestParam("description") List<String> descriptions
    ) {
        List<String> urls = new ArrayList<>();
        log.info(imageTypes.toString());
        log.info(descriptions.toString());
        for (int i = 0; i < files.size(); i++) {
            String url = productService.uploadImages(
                    productId,
                    files.get(i),
                    imageTypes.get(i),
                    descriptions.get(i)
            );
            urls.add(url);
        }

        return ApiResponse.<List<String>>builder().result(urls).build();
    }

    @PostMapping("/{productId}/images/update")
    public ApiResponse<List<String>> updateImages(
            @PathVariable String productId,
            @RequestPart("files") List<MultipartFile> files,
            @RequestParam("imageType") List<String> imageTypes,
            @RequestParam("description") List<String> descriptions
    ) {

        List<ProductImageUploadRequest> requests = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            ProductImageUploadRequest req = new ProductImageUploadRequest();
            req.setFile(files.get(i));
            req.setImageType(imageTypes.get(i));
            req.setDescription(descriptions.get(i));
            requests.add(req);
        }

        List<String> urls = productService.updateProductImages(productId, requests);

        return ApiResponse.<List<String>>builder()
                .result(urls)
                .build();
    }

}
