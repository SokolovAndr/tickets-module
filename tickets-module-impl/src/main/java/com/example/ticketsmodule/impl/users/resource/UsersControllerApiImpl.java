package com.example.ticketsmodule.impl.users.resource;

import com.example.ticketsmodule.api.controller.UsersControllerApi;
import com.example.ticketsmodule.api.model.ChangePasswordRequest;
import com.example.ticketsmodule.api.model.UserPatchRequest;
import com.example.ticketsmodule.api.model.UserResponse;
import com.example.ticketsmodule.impl.oauth.service.JwtCurrentUserService;
import com.example.ticketsmodule.impl.users.domain.UserEntity;
import com.example.ticketsmodule.impl.users.service.UsersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UsersControllerApiImpl implements UsersControllerApi {

    private final JwtCurrentUserService jwtCurrentUserService;
    private final UsersService usersService;

    @Override
    public ResponseEntity<Void> changeCurrentUserPassword(@Valid ChangePasswordRequest changePasswordRequest) {
        final UserEntity user = jwtCurrentUserService.getCurrentUser();
        usersService.changeCurrentUserPassword(user, changePasswordRequest);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<UserResponse> getCurrentUser() {
        final UserResponse user = usersService.toResponse(jwtCurrentUserService.getCurrentUser());
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @Override
    public ResponseEntity<UserResponse> patchCurrentUser(@Valid UserPatchRequest userPatchRequest) {
        final UserEntity currentUser = jwtCurrentUserService.getCurrentUser();

        final UserResponse user = usersService.patchCurrentUser(currentUser, userPatchRequest);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }
}
