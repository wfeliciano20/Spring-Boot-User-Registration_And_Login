package com.williamfeliciano.registrationandlogin.services;

import com.williamfeliciano.registrationandlogin.models.AppUser;
import com.williamfeliciano.registrationandlogin.models.ConfirmationToken;
import com.williamfeliciano.registrationandlogin.repositories.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppUserService implements UserDetailsService {
    private final static String USER_NOT_FOUND_MSG = "User with email %s not found";
    private final AppUserRepository appUserRepository;
    private final ConfirmationTokenService confirmationTokenService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        return appUserRepository.findByEmail(email).orElseThrow(()->
                new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG,email)));
    }

    public String registerUser(AppUser appUser){
        boolean userExists = appUserRepository.findByEmail(appUser.getEmail()).isPresent();
        if(userExists) {
            throw new IllegalStateException("Email Already registered");
        }
        String encodedPassword = bCryptPasswordEncoder.encode(appUser.getPassword());
        appUser.setPassword(encodedPassword);

        appUserRepository.save(appUser);

        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                appUser
        );
        confirmationTokenService.saveConfirmationToken(confirmationToken);
        return token;
    }


    public int enableAppUser(String email) {
        return appUserRepository.enableAppUser(email);
    }
}
