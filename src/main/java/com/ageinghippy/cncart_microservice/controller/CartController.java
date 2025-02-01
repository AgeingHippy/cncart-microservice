package com.ageinghippy.cncart_microservice.controller;

import com.ageinghippy.cncart_microservice.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    CartService cartService;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getCartByUserId(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(cartService.getCartByUserId(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{userId}")
    public ResponseEntity<?> addNewCartItem(
            @RequestParam("item-id") Long itemId,
            @PathVariable Long userId) {

        try {
            return ResponseEntity.ok(
                    cartService.addItem(itemId, userId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @DeleteMapping("/{userId}/{cartItemId}")
    public ResponseEntity<?> removeCartItem(
            @PathVariable("cartItemId") Long cartItemId,
            @PathVariable("userId") Long userId) {

        try {
            return ResponseEntity.ok(
                    cartService.removeCartItem(cartItemId, userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{userId}/{cartItemId}")
    public ResponseEntity<?> updateItemAmount(
            @PathVariable("userId") Long userId,
            @PathVariable("cartItemId") Long cartItemId,
            @RequestParam("amount") Integer amount) {

        try {
            return ResponseEntity.ok(
                    cartService.updateAmount(userId, cartItemId, amount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

