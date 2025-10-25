package com.alhakim.ecommerce.service;

import com.alhakim.ecommerce.common.errors.ForbiddenAccessException;
import com.alhakim.ecommerce.common.errors.ResourceNotFoundException;
import com.alhakim.ecommerce.entity.UserAddress;
import com.alhakim.ecommerce.model.UserAddressRequest;
import com.alhakim.ecommerce.model.UserAddressResponse;
import com.alhakim.ecommerce.repository.UserAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAddressServiceImpl implements UserAddressService {

    private final UserAddressRepository userAddressRepository;

    @Override
    @Transactional
    public UserAddressResponse createAddress(Long userId, UserAddressRequest addressRequest) {
        UserAddress newAddress = UserAddress.builder()
                .userId(userId)
                .addressName(addressRequest.getAddressName())
                .streetAddress(addressRequest.getStreetAddress())
                .city(addressRequest.getCity())
                .state(addressRequest.getState())
                .postalCode(addressRequest.getPostalCode())
                .country(addressRequest.getCountry())
                .isDefault(addressRequest.isDefault())
                .build();

        if (addressRequest.isDefault()) {
            Optional<UserAddress> existingDefault = userAddressRepository.findByUserIdAndIsDefaultTrue(userId);
            if (existingDefault.isPresent()) {
                existingDefault.get().setIsDefault(false);
                userAddressRepository.save(existingDefault.get());
            }
        }

        UserAddress savedAddress = userAddressRepository.save(newAddress);
        return UserAddressResponse.fromUserAddress(savedAddress);
    }

    @Override
    public List<UserAddressResponse> findAddressesByUserId(Long userId) {
        List<UserAddress> addresses = userAddressRepository.findByUserId(userId);
        return addresses.stream().map(UserAddressResponse::fromUserAddress).toList();
    }

    @Override
    public UserAddressResponse findAddressById(Long addressId) {
        UserAddress userAddress = userAddressRepository
                .findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        return UserAddressResponse.fromUserAddress(userAddress);
    }

    @Override
    @Transactional
    public UserAddressResponse updateAddress(Long addressId, UserAddressRequest addressRequest) {
        UserAddress existingUserAddress = userAddressRepository
                .findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        UserAddress updatedUserAddress = UserAddress.builder()
                .userAddressId(existingUserAddress.getUserAddressId())
                .userId(existingUserAddress.getUserId())
                .addressName(addressRequest.getAddressName())
                .streetAddress(addressRequest.getStreetAddress())
                .city(addressRequest.getCity())
                .state(addressRequest.getState())
                .postalCode(addressRequest.getPostalCode())
                .country(addressRequest.getCountry())
                .isDefault(addressRequest.isDefault())
                .build();

        if (addressRequest.isDefault() && !existingUserAddress.getIsDefault()) {
            Optional<UserAddress> existingDefault = userAddressRepository.findByUserIdAndIsDefaultTrue(existingUserAddress.getUserId());
            if (existingDefault.isPresent()) {
                existingDefault.get().setIsDefault(false);
                userAddressRepository.save(existingDefault.get());
            }
        }

        UserAddress savedAddress = userAddressRepository.save(updatedUserAddress);
        return UserAddressResponse.fromUserAddress(savedAddress);
    }

    @Override
    @Transactional
    public void deleteAddress(Long addressId) {
        UserAddress existingUserAddress = userAddressRepository
                .findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        userAddressRepository.delete(existingUserAddress);
        if (existingUserAddress.getIsDefault()) {
            List<UserAddress> remainingUserAddress = userAddressRepository.findByUserId(existingUserAddress.getUserId());
            if (!remainingUserAddress.isEmpty()) {
                UserAddress newDefaultAddress = remainingUserAddress.getFirst();
                newDefaultAddress.setIsDefault(true);
                userAddressRepository.save(newDefaultAddress);
            }
        }
    }

    @Override
    public UserAddressResponse setDefaultAddress(Long userId, Long addressId) {
        UserAddress existingUserAddress = userAddressRepository
                .findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (!existingUserAddress.getUserId().equals(userId)) {
            throw new ForbiddenAccessException("You are not allowed to change this user address");
        }

        Optional<UserAddress> existingDefault = userAddressRepository.findByUserIdAndIsDefaultTrue(existingUserAddress.getUserId());
        if (existingDefault.isPresent()) {
            existingDefault.get().setIsDefault(false);
            userAddressRepository.save(existingDefault.get());
        }

        existingUserAddress.setIsDefault(true);
        UserAddress userAddress = userAddressRepository.save(existingUserAddress);
        return UserAddressResponse.fromUserAddress(userAddress);
    }
}
