package com.vertexplacements.trackerapi.service.impl;

import com.vertexplacements.trackerapi.dto.ChangePasswordRequestDTO;
import com.vertexplacements.trackerapi.dto.RegisterRequestDTO;
import com.vertexplacements.trackerapi.dto.UpdateProfileRequestDTO;
import com.vertexplacements.trackerapi.dto.UserProfileResponseDTO;
import com.vertexplacements.trackerapi.entity.User;
import com.vertexplacements.trackerapi.exception.EmailAlreadyInUseException;
import com.vertexplacements.trackerapi.exception.InvalidPasswordException;
import com.vertexplacements.trackerapi.exception.ResourceNotFoundException;
import com.vertexplacements.trackerapi.repository.UserRepository;
import com.vertexplacements.trackerapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User registerUser(RegisterRequestDTO dto) {
        String normalizedEmail = dto.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyInUseException(normalizedEmail);
        }

        User user = User.builder()
                .fullName(dto.getFullName().trim())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(dto.getPassword()))
                .build();

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserEntityByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> ResourceNotFoundException.forUser(email));
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponseDTO getProfile(String email) {
        return toDto(getUserEntityByEmail(email));
    }

    @Override
    public UserProfileResponseDTO updateProfile(String email, UpdateProfileRequestDTO dto) {
        User user = getUserEntityByEmail(email);
        user.setFullName(dto.getFullName().trim());
        return toDto(userRepository.save(user));
    }

    @Override
    public void changePassword(String email, ChangePasswordRequestDTO dto) {
        User user = getUserEntityByEmail(email);

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    private UserProfileResponseDTO toDto(User user) {
        return UserProfileResponseDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .build();
    }
}
