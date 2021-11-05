package com.udemyproject.mobileapp.webservices.service;

import com.udemyproject.mobileapp.webservices.shared.dto.AddressDto;

import java.util.List;

public interface AddressService {
    List<AddressDto> getAddresses(String udersId);
    AddressDto getAddress(String addressId);
}
