package com.example.backend.mapper;

import com.example.backend.dto.request.CategoryCreationRequest;
import com.example.backend.dto.request.ProductCreationRequest;
import com.example.backend.dto.response.CategoryResponse;
import com.example.backend.entity.Category;
import com.example.backend.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toCategory(CategoryCreationRequest request);
    CategoryResponse toCategoryResponse(Category category);


}
