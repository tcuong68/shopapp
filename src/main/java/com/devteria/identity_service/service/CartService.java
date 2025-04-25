package com.devteria.identity_service.service;

import com.devteria.identity_service.dto.response.CartItemResponse;
import com.devteria.identity_service.dto.response.CartResponse;
import com.devteria.identity_service.entity.Cart;
import com.devteria.identity_service.entity.CartItem;
import com.devteria.identity_service.entity.ProductVariant;
import com.devteria.identity_service.entity.User;
import com.devteria.identity_service.mapper.CartItemMapper;
import com.devteria.identity_service.repository.CartItemRepository;
import com.devteria.identity_service.mapper.CartMapper;
import com.devteria.identity_service.repository.CartRepository;
import com.devteria.identity_service.repository.ProductVariantRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EnableMethodSecurity
public class CartService {

    CartRepository cartRepository;
    CartItemRepository cartItemRepository;
    ProductVariantRepository productVariantRepository;
    UserService userService;
    CartMapper cartMapper;
    CartItemMapper cartItemMapper;


    public CartResponse getCart() {
        User currentUser = userService.getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> createNewCart(currentUser));
        CartResponse cartResponse = cartMapper.toCartResponse(cart);
        if(!cart.getCartItems().isEmpty()) {
            List<CartItemResponse> cartItemResponses = new ArrayList<>();
            Long subTotal = 0L;
            for (CartItem cartItem : cart.getCartItems()) {
                CartItemResponse cartItemResponse = cartItemMapper.toCartItemResponse(cartItem);
                cartItemResponse.setId(cartItem.getId());
                cartItemResponse.setColor(cartItem.getProductVariant().getColor());
                cartItemResponse.setPrice(cartItem.getProductVariant().getPrice());
                cartItemResponse.setSize(cartItem.getProductVariant().getSize());
                cartItemResponse.setVariantId(cartItem.getProductVariant().getId());
                cartItemResponse.setProductId(cartItem.getProductVariant().getProduct().getId());
                cartItemResponse.setProductName(cartItem.getProductVariant().getProduct().getName());
                cartItemResponse.setTotalPrice(cartItem.getProductVariant().getPrice()*cartItem.getQuantity());
                subTotal += cartItemResponse.getTotalPrice();
                cartItemResponses.add(cartItemResponse);
            }
            cartResponse.setItems(cartItemResponses);
            cartResponse.setSubTotal(subTotal);
        }
        return cartResponse;
    }



    public Cart createNewCart(User currentUser) {
        Cart cart = new Cart();
        cart.setUser(currentUser);
        cart.setCartItems(new ArrayList<>());
        return cartRepository.save(cart);
    }

    public CartResponse addToCart(Long variantId, int quantity) {
        User currentUser = userService.getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> createNewCart(currentUser));
        ProductVariant productVariant = productVariantRepository.findById(variantId).orElseThrow();
        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProductVariant().getId().equals(variantId))
                .findFirst();

        if(existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItemRepository.save(cartItem);
        }
        else {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProductVariant(productVariant);
            cartItem.setQuantity(quantity);

            cart.getCartItems().add(cartItem);
            cartItemRepository.save(cartItem);
        }
        cartRepository.save(cart);
        CartResponse cartResponse = cartMapper.toCartResponse(cart);
        if(!cart.getCartItems().isEmpty()) {
            List<CartItemResponse> cartItemResponses = new ArrayList<>();
            Long subTotal = 0L;
            for (CartItem cartItem : cart.getCartItems()) {
                CartItemResponse cartItemResponse = cartItemMapper.toCartItemResponse(cartItem);
                cartItemResponse.setId(cartItem.getId());
                cartItemResponse.setColor(cartItem.getProductVariant().getColor());
                cartItemResponse.setPrice(cartItem.getProductVariant().getPrice());
                cartItemResponse.setSize(cartItem.getProductVariant().getSize());
                cartItemResponse.setVariantId(cartItem.getProductVariant().getId());
                cartItemResponse.setProductId(cartItem.getProductVariant().getProduct().getId());
                cartItemResponse.setProductName(cartItem.getProductVariant().getProduct().getName());
                cartItemResponse.setTotalPrice(cartItem.getProductVariant().getPrice()*cartItem.getQuantity());
                subTotal += cartItemResponse.getTotalPrice();
                cartItemResponses.add(cartItemResponse);
            }
            cartResponse.setItems(cartItemResponses);
            cartResponse.setSubTotal(subTotal);
        }
        return cartResponse;
    }

    public CartResponse updateCart(Long cartItemId, Integer quantity) {
        User currentUser = userService.getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> createNewCart(currentUser));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow();

        //kiem tra xem cartitem nay co dung nam trong cart cua user nay ko
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new AccessDeniedException("You do not have permission to update this cart item");
        }

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        return cartMapper.toCartResponse(cart);
    }


    public CartResponse deleteCartItem(Long cartItemId) {
        User currentUser = userService.getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> createNewCart(currentUser));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow();

        //kiem tra xem cartitem nay co dung nam trong cart cua user nay ko
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this cart item");
        }

        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        return cartMapper.toCartResponse(cart);
    }
}
