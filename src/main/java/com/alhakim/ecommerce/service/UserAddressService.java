package com.alhakim.ecommerce.service;

import com.alhakim.ecommerce.model.UserAddressRequest;
import com.alhakim.ecommerce.model.UserAddressResponse;

import java.util.List;
import java.util.Optional;

public interface UserAddressService {
    UserAddressResponse createAddress(Long userId, UserAddressRequest addressRequest);
    List<UserAddressResponse> findAddressesByUserId(Long userId);
    UserAddressResponse findAddressById(Long addressId);
    UserAddressResponse updateAddress(Long addressId, UserAddressRequest addressRequest);
    void deleteAddress(Long addressId);
    UserAddressResponse setDefaultAddress(Long userId, Long addressId);
}
