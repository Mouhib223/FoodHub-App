package com.foodhub.user.service;

import com.foodhub.user.dto.*;
import com.foodhub.user.entity.Address;
import com.foodhub.user.entity.User;
import com.foodhub.user.exception.ResourceNotFoundException;
import com.foodhub.user.repository.AddressRepository;
import com.foodhub.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final RabbitTemplate rabbitTemplate;

    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        User user = User.builder()
            .keycloakId(request.getKeycloakId())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .phone(request.getPhone())
            .role(request.getRole())
            .profileImageUrl(request.getProfileImageUrl())
            .active(true)
            .build();

        user = userRepository.save(user);
        log.info("Created user with id: {}", user.getId());

        // Publish user registration event
        rabbitTemplate.convertAndSend("foodhub.exchange", "user.registered",
            new UserRegisteredEvent(user.getId(), user.getEmail(), user.getRole().name()));

        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByKeycloakId(String keycloakId) {
        User user = userRepository.findByKeycloakId(keycloakId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with keycloak id: " + keycloakId));
        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setProfileImageUrl(request.getProfileImageUrl());

        user = userRepository.save(user);
        log.info("Updated user with id: {}", user.getId());
        return mapToResponse(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setActive(false);
        userRepository.save(user);
        log.info("Deactivated user with id: {}", id);
    }

    // Address management
    public AddressResponse addAddress(Long userId, AddressRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // If this is the default address, unset others
        if (request.isDefault()) {
            addressRepository.findByUserIdAndIsDefault(userId, true)
                .forEach(addr -> {
                    addr.setDefault(false);
                    addressRepository.save(addr);
                });
        }

        Address address = Address.builder()
            .user(user)
            .label(request.getLabel())
            .street(request.getStreet())
            .city(request.getCity())
            .state(request.getState())
            .postalCode(request.getPostalCode())
            .country(request.getCountry())
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .isDefault(request.isDefault())
            .build();

        address = addressRepository.save(address);
        return mapAddressToResponse(address);
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> getUserAddresses(Long userId) {
        return addressRepository.findByUserId(userId).stream()
            .map(this::mapAddressToResponse)
            .collect(Collectors.toList());
    }

    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        if (!address.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Address does not belong to user");
        }
        addressRepository.delete(address);
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setKeycloakId(user.getKeycloakId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setActive(user.isActive());
        response.setProfileImageUrl(user.getProfileImageUrl());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        if (user.getAddresses() != null) {
            response.setAddresses(user.getAddresses().stream()
                .map(this::mapAddressToResponse).collect(Collectors.toList()));
        }
        return response;
    }

    private AddressResponse mapAddressToResponse(Address address) {
        AddressResponse response = new AddressResponse();
        response.setId(address.getId());
        response.setLabel(address.getLabel());
        response.setStreet(address.getStreet());
        response.setCity(address.getCity());
        response.setState(address.getState());
        response.setPostalCode(address.getPostalCode());
        response.setCountry(address.getCountry());
        response.setLatitude(address.getLatitude());
        response.setLongitude(address.getLongitude());
        response.setDefault(address.isDefault());
        return response;
    }

    // Inner event record
    public record UserRegisteredEvent(Long userId, String email, String role) {}
}

