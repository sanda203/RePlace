package com.wil.reservation_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private  UUID id;

    @Email
    @Column(name = "email", nullable = false )
    private  String email;

    @Column(name = "password", nullable = false)
    private  String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    protected User(){}

    public User(String email, String password){
        this.email = email;
        this.password = password;
        this.role = Role.USER;
    }

    public void assignRole(Role role) {
        this.role = role;
    }

}
