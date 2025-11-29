package org.gs.controller;
import io.smallrye.jwt.build.Jwt;
import org.eclipse.microprofile.jwt.Claims;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.gs.dto.LoginRequest;
import org.gs.dto.LoginResponse;
import org.gs.dto.RefreshRequest;
import org.gs.dto.UserRequest;
import org.gs.model.User;
import org.gs.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    UserRepository userRepository;

    @POST
    @Path("/register")
    @PermitAll
    public Response register(UserRequest userRequest) {
        try {
            // Validate request
            if (userRequest.getEmail() == null || userRequest.getPassword() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Email and password are required")
                        .build();
            }

            // Check if user exists
            User existingUser = userRepository.findByEmail(userRequest.getEmail());
            if (existingUser != null) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("Email already exists")
                        .build();
            }
            
            // Create user
            User createdUser = userRepository.addUser(userRequest);
            
            // Generate tokens
            String accessToken = generateAccessToken(createdUser);
            String refreshToken = generateRefreshToken(createdUser);
            
            // Update user with refresh token
            userRepository.updateRefreshToken(createdUser.getId(), refreshToken);
            
            // Return response
            LoginResponse response = new LoginResponse();
            response.setMessage("Registration successful");
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error creating user: " + e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/login")
    @PermitAll
    public Response login(LoginRequest loginRequest) {
        try {
            // Validate request
            if (loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Email and password are required")
                        .build();
            }

            // Find user
            User user = userRepository.findByEmail(loginRequest.getEmail());
            if (user == null || !BCrypt.checkpw(loginRequest.getPassword(), user.getPassword())) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Invalid email or password")
                        .build();
            }

            // Generate tokens
            String accessToken = generateAccessToken(user);
            String refreshToken = generateRefreshToken(user);
            
            // Update refresh token in database
            userRepository.updateRefreshToken(user.getId(), refreshToken);
            
            // Create response
            LoginResponse response = new LoginResponse();
            response.setMessage("Login successful");
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error during login: " + e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/refresh")
    @PermitAll
    public Response refresh(RefreshRequest refreshRequest) {
        try {
            // Validate request
            if (refreshRequest.getRefreshToken() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Refresh token is required")
                        .build();
            }

            // Find user by refresh token
            User user = userRepository.findByRefreshToken(refreshRequest.getRefreshToken());
            if (user == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Invalid refresh token")
                        .build();
            }

            // Generate new tokens
            String newAccessToken = generateAccessToken(user);
            String newRefreshToken = generateRefreshToken(user);
            
            // Update refresh token in database
            userRepository.updateRefreshToken(user.getId(), newRefreshToken);
            
            // Create response
            LoginResponse response = new LoginResponse();
            response.setMessage("Token refreshed successfully");
            response.setAccessToken(newAccessToken);
            response.setRefreshToken(newRefreshToken);
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error refreshing token: " + e.getMessage())
                    .build();
        }
    }

    private String generateAccessToken(User user) {
        return Jwt.issuer("yours")
                .upn(user.getEmail())
                .subject(user.getId())
               // .groups(new HashSet<>(user.getRoles()))
                .claim(Claims.full_name.name(), user.getFirstName() + " " + user.getLastName())
                .expiresAt(Instant.now().plus(15, ChronoUnit.MINUTES)) // Shorter lifetime for access token
                .sign();
    }

   
    private String generateRefreshToken(User user) {
    return Jwt.issuer("yours")
            .subject(user.getId())
            .claim("token_type", "refresh")
            .claim(Claims.jti.name(), UUID.randomUUID().toString()) 
            .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
            .sign();
}
}