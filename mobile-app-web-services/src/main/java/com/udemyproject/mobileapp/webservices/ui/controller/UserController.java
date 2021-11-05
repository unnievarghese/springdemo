package com.udemyproject.mobileapp.webservices.ui.controller;

import com.udemyproject.mobileapp.webservices.service.AddressService;
import com.udemyproject.mobileapp.webservices.service.UserService;
import com.udemyproject.mobileapp.webservices.shared.dto.AddressDto;
import com.udemyproject.mobileapp.webservices.shared.dto.UserDto;
import com.udemyproject.mobileapp.webservices.ui.model.request.PasswordResetModel;
import com.udemyproject.mobileapp.webservices.ui.model.request.PasswordResetRequestModel;
import com.udemyproject.mobileapp.webservices.ui.model.request.UserDetailsRequestModel;
import com.udemyproject.mobileapp.webservices.ui.model.response.*;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "users")
public class UserController {

    @Autowired
    UserService userService;
    @Autowired
    AddressService addressesService;
    @Autowired
    AddressService addressService;

    @GetMapping(path = "/{id}")
    public UserRest getUser(@PathVariable String id){
//        UserRest returnValue = new UserRest();
        UserDto userDto = userService.getUserByUserId(id);
        ModelMapper modelMapper = new ModelMapper();
        UserRest returnValue = modelMapper.map(userDto,UserRest.class);
//        BeanUtils.copyProperties(userDto,returnValue);

        return returnValue;
    }

    @PostMapping
    public UserRest createUser(@RequestBody UserDetailsRequestModel userDetails) throws Exception {

//        UserRest returnValue = new UserRest();

        if(userDetails.getFirstName().isEmpty()) throw new NullPointerException("The Object is Null");

//        UserDto userDto = new UserDto();
//        BeanUtils.copyProperties(userDetails,userDto);
        ModelMapper modelMapper = new ModelMapper();
        UserDto userDto = modelMapper.map(userDetails,UserDto.class);

        UserDto createUser = userService.createUser(userDto);
        UserRest returnValue= modelMapper.map(createUser,UserRest.class);

//        BeanUtils.copyProperties(createUser,returnValue);

        return returnValue;
    }

    @PutMapping(path = "/{id}")
    public UserRest updateUser(@PathVariable String id,@RequestBody UserDetailsRequestModel userDetails){

        UserRest returnValue = new UserRest();

        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(userDetails,userDto);

        UserDto updateUser = userService.updateUser(id,userDto);
        BeanUtils.copyProperties(updateUser,returnValue);

        return returnValue;
    }

    @DeleteMapping(path = "/{id}")
    public OperationStatusModel deleteUser(@PathVariable String id){

        OperationStatusModel returnValue = new OperationStatusModel();
        returnValue.setOperationName(RequestOperationName.DELETE.name());

        userService.deleteUser(id);

        returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
        return returnValue;
    }
    @GetMapping
    public List<UserRest> getUsers(@RequestParam(value = "page",defaultValue = "0") int page,
                                   @RequestParam(value = "limit",defaultValue = "2") int limit){
        List<UserRest> returnValue = new ArrayList<>();
        List<UserDto> users = userService.getUsers(page,limit);

        for (UserDto userDto : users){
            UserRest userModel = new UserRest();
            BeanUtils.copyProperties(userDto,userModel);
            returnValue.add(userModel);
        }
        return returnValue;
    }
    @GetMapping(path = "/{id}/addresses")
    public List<AddressesRest> getUserAddresses(@PathVariable String id){

        List<AddressesRest> returnValue = new ArrayList<>();

        List<AddressDto> addressesDto = addressesService.getAddresses(id);

        if(addressesDto != null && !addressesDto.isEmpty()) {
            Type listType = new TypeToken<List<AddressesRest>>() {}.getType();
            returnValue = new ModelMapper().map(addressesDto, listType);
        }
        return returnValue;
    }
    @GetMapping(path = "/{id}/addresses/{addressId}")
    public AddressesRest getUserAddress(@PathVariable String id ,
                                        @PathVariable String addressId){

        AddressDto addressDto = addressService.getAddress(addressId);

        ModelMapper modelMapper = new ModelMapper();
        AddressesRest returnValue = modelMapper.map(addressDto,AddressesRest.class);

//        http://localhost:8080/users
        Link userLink = WebMvcLinkBuilder.linkTo(UserController.class).slash(id).withRel("user");
//        http://localhost:8080/users/<userId>/addresses
        Link userAddressLink = WebMvcLinkBuilder.linkTo(UserController.class).slash(id)
                        .slash("addresses")
                        .withRel("addresses");
//        http://localhost:8080/users/<userId>/addresses/address
        Link selfLink = WebMvcLinkBuilder.linkTo(UserController.class).slash(id)
                .slash("addresses")
                .slash(addressId)
                .withSelfRel();

        returnValue.add(userLink);
        returnValue.add(userAddressLink);
        returnValue.add(selfLink);

        return returnValue;
    }
//    http://localhost:8080/mobile-app-webservices/users/email-verification?token=sdfsdf
    @GetMapping(path= "/email-verification")
    public OperationStatusModel verifyEmailToken(@RequestParam(value = "token") String token){
        OperationStatusModel returnValue = new OperationStatusModel();
        returnValue.setOperationName(RequestOperationName.VERIFY_EMAIL.name());

        boolean isVerified = userService.verifyEmailToken(token);

        if(isVerified){
            returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
        }
        else{
            returnValue.setOperationResult(RequestOperationStatus.ERROR.name());
        }
        return returnValue;
    }
    @PostMapping(path="/password-reset-request")

    public OperationStatusModel requestReset(@RequestBody PasswordResetRequestModel passwordResetRequestModel){
        OperationStatusModel returnValue = new OperationStatusModel();

        Boolean OperationResult = userService.requestPasswordReset(passwordResetRequestModel.getEmail());
        returnValue.setOperationName(RequestOperationName.REQUEST_PASSWORD_RESET.name());
        returnValue.setOperationResult(RequestOperationStatus.ERROR.name());

        if(OperationResult){
            returnValue.setOperationResult(RequestOperationStatus.EMAIL_SENT_SUCCESSFULLY.name());
        }
        return returnValue;
    }

    @PostMapping(path="/password-reset")
    public OperationStatusModel setNewPassword(@RequestBody PasswordResetModel passwordResetModel,
                                               @RequestParam(value = "token") String token){

        OperationStatusModel returnValue = new OperationStatusModel();
        returnValue.setOperationName(RequestOperationName.PASSWORD_RESET.name());
        Boolean isVerified = userService.verifyPasswordResetToken(token,passwordResetModel.getNewPassword(),
                                                                        passwordResetModel.getRepeatPassword());

        if(isVerified){
            returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
        }
        else{
            returnValue.setOperationResult(RequestOperationStatus.ERROR.name());
        }
        return returnValue;
    }
}

