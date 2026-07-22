package com.projeto.ticket_wave.application.mapper;

import com.projeto.ticket_wave.application.dto.AddressDTOs.AddressResponse;
import com.projeto.ticket_wave.domain.entity.Address;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AddressMapper {
    AddressResponse toResponse(Address address);
    List<AddressResponse> toResponseList(List<Address> addresses);
}
