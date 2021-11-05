package com.udemyproject.mobileapp.webservices.service.impl;

import com.udemyproject.mobileapp.webservices.exceptions.UserServiceException;
import com.udemyproject.mobileapp.webservices.io.entity.PasswordResetTokenEntity;
import com.udemyproject.mobileapp.webservices.io.repositories.PasswordResetTokenRepository;
import com.udemyproject.mobileapp.webservices.io.repositories.UserRepository;
import com.udemyproject.mobileapp.webservices.io.entity.UserEntity;
import com.udemyproject.mobileapp.webservices.service.UserService;
import com.udemyproject.mobileapp.webservices.service.EmailService;
import com.udemyproject.mobileapp.webservices.shared.dto.AddressDto;
import com.udemyproject.mobileapp.webservices.shared.dto.UserDto;
import com.udemyproject.mobileapp.webservices.shared.dto.Utils;
import com.udemyproject.mobileapp.webservices.ui.model.response.ErrorMessages;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    EmailService emailService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    Utils utils;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    public UserServiceImpl() {
    }

    @Override
    public UserDto createUser(UserDto user){

        if(userRepository.findByEmail(user.getEmail()) != null) throw new RuntimeException("Record Already Exixts");

        for(int i=0;i<user.getAddress().size();i++){
            AddressDto address = user.getAddress().get(i);
            address.setUserDetails(user);
            address.setAddressId(utils.generateAddressId(30));
            user.getAddress().set(i,address);
        }
//        UserEntity userEntity = new UserEntity();
        ModelMapper modelMapper =new ModelMapper();
        UserEntity userEntity = modelMapper.map(user,UserEntity.class);
//        BeanUtils.copyProperties(user,userEntity);
        userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        String publicUserId = utils.generateUserId(30);
        userEntity.setUserId(publicUserId);
        userEntity.setEmailVerificationToken(utils.generateEmailVerificationToken(publicUserId));

        UserEntity storedUserDetails = userRepository.save(userEntity);

        String link = "http://localhost:8080/mobile-app-webservices/users/email-verification?token="
                +userEntity.getEmailVerificationToken();

        EmailBuilder emailBuilder = new EmailBuilder();
        emailService.send(userEntity.getEmail(),emailBuilder.buildRegistrationContent(userEntity.getFirstName(),link));

//        UserDto returnValue = new UserDto();
        UserDto returnValue = modelMapper.map(storedUserDetails,UserDto.class);
//        BeanUtils.copyProperties(storedUserDetails,returnValue);

        return returnValue;
    }

    @Override
    public UserDto getUser(String email){
        UserEntity userEntity = userRepository.findByEmail(email);
        if (userEntity == null) throw new UsernameNotFoundException(email);

        UserDto returnValue = new UserDto();
        BeanUtils.copyProperties(userEntity,returnValue);

        return returnValue;
    }

    @Override
    public UserDto getUserByUserId(String userId) {
//        UserDto returnValue = new UserDto();
        UserEntity userEntity = userRepository.findByUserId(userId);
        if (userEntity == null) throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        ModelMapper modelMapper = new ModelMapper();
        UserDto returnValue = modelMapper.map(userEntity,UserDto.class);
//        BeanUtils.copyProperties(userEntity,returnValue);
        return returnValue;
    }

    @Override
    public UserDto updateUser(String userId, UserDto user) {
        UserDto returnValue = new UserDto();
        UserEntity userEntity = userRepository.findByUserId(userId);
        if (userEntity == null) throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        userEntity.setFirstName(user.getFirstName());
        userEntity.setLastName(user.getLastName());
        UserEntity updatedUserDetails = userRepository.save(userEntity);
        BeanUtils.copyProperties(updatedUserDetails,returnValue);
        return returnValue;
    }

    @Override
    public void deleteUser(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        if (userEntity == null) throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        userRepository.delete(userEntity);
    }

    @Override
    public List<UserDto> getUsers(int page, int limit) {
        List<UserDto> returnValue = new ArrayList<>();
        Pageable pageableRequest = PageRequest.of(page,limit);
        Page<UserEntity> usersPage = userRepository.findAll(pageableRequest);
        List<UserEntity> users = usersPage.getContent();

        for (UserEntity userEntity : users){
            UserDto userDto = new UserDto();
            BeanUtils.copyProperties(userEntity,userDto);
            returnValue.add(userDto);
        }
        return returnValue;
    }

    @Override
    public Boolean verifyEmailToken(String token) {
        boolean returnValue = false;

        UserEntity userEntity = userRepository.findByEmailVerificationToken(token);

        if(userEntity != null){
            boolean hastokenExpired = Utils.hasTokenExpired(token);
            if(!hastokenExpired){
                userEntity.setEmailVerificationToken(null);
                userEntity.setEmailVerificationStatus(Boolean.TRUE);
                userRepository.save(userEntity);
                returnValue = true;
            }
        }
        return returnValue;
    }

    @Override
    public Boolean requestPasswordReset(String email) {
        Boolean returnValue =false;

        UserEntity userEntity = userRepository.findByEmail(email);

        if(userEntity == null){
            return returnValue;
        }
        String token = new Utils().generatePasswordResetToken(userEntity.getUserId());

        PasswordResetTokenEntity passwordResetTokenEntity = new PasswordResetTokenEntity();
        passwordResetTokenEntity.setToken(token);
        passwordResetTokenEntity.setUserDetails(userEntity);
        passwordResetTokenRepository.save(passwordResetTokenEntity);

        String link = "http://localhost:8080/mobile-app-webservices/users/password-reset?token="
                +passwordResetTokenEntity.getToken();

        EmailBuilder emailBuilder = new EmailBuilder();
        emailService.send(userEntity.getEmail(),emailBuilder.buildPasswordResetContent(userEntity.getFirstName(),link));
        returnValue = true;
        return returnValue;
    }

    @Override
    public Boolean verifyPasswordResetToken(String token,String password1,String password2) {
        Boolean returnValue =false;
        PasswordResetTokenEntity passwordResetTokenEntity = passwordResetTokenRepository.findByToken(token);
        if (passwordResetTokenEntity == null || !password1.equals(password2)){
            return returnValue;
        }

        UserEntity userEntity = passwordResetTokenEntity.getUserDetails();
        userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(password1));
        userRepository.save(userEntity);
        returnValue = true;

        return returnValue;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(email);

        if (userEntity == null) throw new UsernameNotFoundException(email);
        return new User(userEntity.getEmail(),
                userEntity.getEncryptedPassword(),
                userEntity.getEmailVerificationStatus(),
                true,true,true,
                new ArrayList<>());

//        return new User(userEntity.getEmail(),userEntity.getEncryptedPassword(),new ArrayList<>());
    }
}
