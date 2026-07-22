package com.projeto.ticket_wave.application.mapper;

import com.projeto.ticket_wave.application.dto.CategoryDTOs.CategoryResponse;
import com.projeto.ticket_wave.application.dto.CategoryDTOs.CreateCategoryRequest;
import com.projeto.ticket_wave.application.dto.CategoryDTOs.UpdateCategoryRequest;
import com.projeto.ticket_wave.domain.entity.Category;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;


@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CategoryMapper {

    Category toEntity(CreateCategoryRequest dto);

    CategoryResponse toResponse(Category category);

    List<CategoryResponse> toResponseList(List<Category> categories);

    @BeanMapping(
            nullValuePropertyMappingStrategy =
                    NullValuePropertyMappingStrategy.IGNORE
    )
    void updateEntity(
            UpdateCategoryRequest dto,
            @MappingTarget Category category
    );

}