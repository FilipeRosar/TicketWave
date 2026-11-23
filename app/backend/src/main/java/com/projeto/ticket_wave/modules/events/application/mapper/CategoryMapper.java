package com.projeto.ticket_wave.modules.events.application.mapper;

import com.projeto.ticket_wave.modules.events.application.dto.CategoryResponse;
import com.projeto.ticket_wave.modules.events.application.dto.CreateCategoryRequest;
import com.projeto.ticket_wave.modules.events.application.dto.UpdateCategoryRequest;
import com.projeto.ticket_wave.modules.events.domain.entity.Category;
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