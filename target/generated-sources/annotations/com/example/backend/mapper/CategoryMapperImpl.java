package com.example.backend.mapper;

import com.example.backend.dto.request.CategoryCreationRequest;
import com.example.backend.dto.response.CategoryResponse;
import com.example.backend.entity.Category;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-03T15:55:47+0700",
    comments = "version: 1.6.2, compiler: javac, environment: Java 21.0.9 (Microsoft)"
)
@Component
public class CategoryMapperImpl implements CategoryMapper {

    @Override
    public Category toCategory(CategoryCreationRequest request) {
        if ( request == null ) {
            return null;
        }

        Category.CategoryBuilder category = Category.builder();

        category.name( request.getName() );

        return category.build();
    }

    @Override
    public CategoryResponse toCategoryResponse(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryResponse.CategoryResponseBuilder categoryResponse = CategoryResponse.builder();

        categoryResponse.categoryId( category.getCategoryId() );
        categoryResponse.name( category.getName() );

        return categoryResponse.build();
    }
}
