/*
 * Uygulama ilk basladiginda varsayilan admin kullanicisi olusturur.
 * Production'da bu sifreyi degistir.
 */
//TODO şifreyi değiştirmeyi unutma
package com.awsokanclub.GameAdmin.config;

import com.awsokanclub.GameAdmin.model.AdminUser;
import com.awsokanclub.GameAdmin.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (adminUserRepository.findByUsername(ADMIN_USERNAME).isEmpty()) {
            AdminUser admin = AdminUser.builder()
                    .username(ADMIN_USERNAME)
                    .password(passwordEncoder.encode(PASSWORD))
                    .role("ADMIN")
                    .build();
            adminUserRepository.save(admin);
        }

        if (adminUserRepository.findByUsername(HOST_USERNAME).isEmpty()) {
            AdminUser host = AdminUser.builder()
                    .username(HOST_USERNAME)
                    .password(passwordEncoder.encode("PASSWORD"))
                    .role("HOST")
                    .build();
            adminUserRepository.save(host);
        }
    }
}