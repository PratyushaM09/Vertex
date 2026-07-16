package com.vertexplacements.trackerapi.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /** BCrypt-hashed — never stored or returned in plain text. */
    @Column(nullable = false)
    private String password;

    @Column(name = "roll_number", length = 50)
    private String rollNumber;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'STUDENT'")
    @Column(nullable = false, length = 30)
    @Builder.Default
    private UserRole role = UserRole.STUDENT;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}