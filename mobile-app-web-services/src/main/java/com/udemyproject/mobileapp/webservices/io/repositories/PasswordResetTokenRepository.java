package com.udemyproject.mobileapp.webservices.io.repositories;

import com.udemyproject.mobileapp.webservices.io.entity.PasswordResetTokenEntity;
import org.springframework.data.repository.CrudRepository;

public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetTokenEntity,Long> {

    PasswordResetTokenEntity findByToken(String token);
}
