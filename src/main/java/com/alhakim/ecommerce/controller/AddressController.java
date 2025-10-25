package com.alhakim.ecommerce.controller;

import com.alhakim.ecommerce.model.UserAddressRequest;
import com.alhakim.ecommerce.model.UserAddressResponse;
import com.alhakim.ecommerce.model.UserInfo;
import com.alhakim.ecommerce.service.UserAddressService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
public class AddressController {
    private final UserAddressService userAddressService;

    @PostMapping
    public ResponseEntity<UserAddressResponse> create(@Valid @RequestBody UserAddressRequest addressRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();

        UserAddressResponse response = userAddressService.createAddress(userInfo.getUser().getUserId(), addressRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UserAddressResponse>> findAddressByUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();

        List<UserAddressResponse> addressResponses = userAddressService.findAddressesByUserId(userInfo.getUser().getUserId());
        return ResponseEntity.ok(addressResponses);
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<UserAddressResponse> get(@PathVariable Long addressId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();

        UserAddressResponse addressResponses = userAddressService.findAddressById(addressId);
        return ResponseEntity.ok(addressResponses);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<UserAddressResponse> update(@PathVariable Long addressId, @Valid @RequestBody UserAddressRequest addressRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();

        UserAddressResponse response = userAddressService.updateAddress(addressId, addressRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> delete(@PathVariable Long addressId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();

        userAddressService.deleteAddress(addressId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{addressId}/set-default")
    public ResponseEntity<UserAddressResponse> setDefaultAddress(@PathVariable Long addressId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();

        UserAddressResponse response = userAddressService.setDefaultAddress(userInfo.getUser().getUserId(), addressId);
        return ResponseEntity.ok(response);
    }
}
