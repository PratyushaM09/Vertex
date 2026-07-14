package com.vertexplacements.trackerapi.service;

import com.vertexplacements.trackerapi.dto.ChangePasswordRequestDTO;
import com.vertexplacements.trackerapi.dto.RegisterRequestDTO;
import com.vertexplacements.trackerapi.dto.UpdateProfileRequestDTO;
import com.vertexplacements.trackerapi.dto.UserProfileResponseDTO;
import com.vertexplacements.trackerapi.entity.User;

public interface UserService {

    User registerUser(RegisterRequestDTO dto);

    User getUserEntityByEmail(String email);

    UserProfileResponseDTO getProfile(String email);

    UserProfileResponseDTO updateProfile(String email, UpdateProfileRequestDTO dto);

    void changePassword(String email, ChangePasswordRequestDTO dto);
}
