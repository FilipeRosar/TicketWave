package com.projeto.ticket_wave.application.mapper;

import com.projeto.ticket_wave.application.dto.VenueDTOs.CreateVenueRequest;
import com.projeto.ticket_wave.application.dto.VenueDTOs.UpdateVenueRequest;
import com.projeto.ticket_wave.application.dto.VenueDTOs.VenueResponse;
import com.projeto.ticket_wave.domain.entity.Venue;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {AddressMapper.class}
)
public interface VenueMapper {
    Venue toEntity(CreateVenueRequest dto);

    VenueResponse toResponse(Venue venue);

    List<VenueResponse> toResponseList(List<Venue> venues);

    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    void updateEntity(UpdateVenueRequest dto,
                      @MappingTarget Venue venue);
}
