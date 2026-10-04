package com.wil.reservation_api.config;

import com.wil.reservation_api.entity.Role;
import com.wil.reservation_api.entity.User;
import com.wil.reservation_api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminBootstrap(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          @Value("${app.admin.email:}") String adminEmail,
                          @Value("${app.admin.password:}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            log.info("Admin bootstrap skipped: app.admin.email/password not configured");
            return;
        }
        if (userRepository.findByEmail(adminEmail).isPresent()) {
            log.info("Admin bootstrap skipped: {} already exists", adminEmail);
            return;
        }
        User admin = new User(adminEmail, passwordEncoder.encode(adminPassword));
        admin.assignRole(Role.ADMIN);
        userRepository.save(admin);
        log.info("Bootstrapped admin account {}", adminEmail);
    }
}
