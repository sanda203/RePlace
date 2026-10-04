package com.wil.reservation_api.security;

import com.wil.reservation_api.entity.Role;
import com.wil.reservation_api.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UserPrincipalTest {

    private User userWithRole(Role role) {
        User user = new User("user@test.com", "ENCODED");
        ReflectionTestUtils.setField(user, "role", role);
        return user;
    }

    @Test
    void givenAdminUser_whenGetAuthorities_thenContainsRoleAdmin() {
        UserPrincipal principal = new UserPrincipal(userWithRole(Role.ADMIN));

        boolean hasRoleAdmin = principal.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        assertTrue(hasRoleAdmin);
    }

    @Test
    void givenRegularUser_whenGetAuthorities_thenContainsRoleUser() {
        UserPrincipal principal = new UserPrincipal(userWithRole(Role.USER));

        boolean hasRoleUser = principal.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER"));

        assertTrue(hasRoleUser);
    }
}
