package com.udemyproject.mobileapp.webservices.service.impl;

import com.udemyproject.mobileapp.webservices.io.entity.AddressEntity;
import com.udemyproject.mobileapp.webservices.io.entity.UserEntity;
import com.udemyproject.mobileapp.webservices.io.repositories.AddressRepository;
import com.udemyproject.mobileapp.webservices.io.repositories.UserRepository;
import com.udemyproject.mobileapp.webservices.service.AddressService;
import com.udemyproject.mobileapp.webservices.shared.dto.AddressDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AddressServiceimpl implements AddressService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    AddressRepository addressRepository;

    @Override
    public List<AddressDto> getAddresses(String userId) {
        List<AddressDto> returnValue = new ArrayList<>();
        ModelMapper modelMapper = new ModelMapper();

        UserEntity userEntity = userRepository.findByUserId(userId);
        if(userEntity == null) return returnValue;

        Iterable<AddressEntity> addresses = addressRepository.findAllByUserDetails(userEntity);

        for (AddressEntity addressEntity:addresses){
            returnValue.add(modelMapper.map(addressEntity,AddressDto.class));
        }
        return returnValue;
    }

    @Override
    public AddressDto getAddress(String addressId) {
        AddressDto returnValue = null;

        AddressEntity addressEntity=addressRepository.findByAddressId(addressId);

        if(addressEntity != null){
            returnValue = new ModelMapper().map(addressEntity,AddressDto.class);
        }
        return returnValue;
    }
}
