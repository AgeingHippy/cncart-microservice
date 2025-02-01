package com.ageinghippy.cncart_microservice.service;

import com.ageinghippy.cncart_microservice.model.Cart;
import com.ageinghippy.cncart_microservice.model.CartItem;
import com.ageinghippy.cncart_microservice.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    @Autowired
    CartRepository cartRepository;

    /**
     * Return the cart associated with the given User.
     * If the cart does not exist, return a new empty Cart
     * @param userId the ID of the User
     * @return the Cart associated with the User
     */
    @Transactional
    public Cart getCartByUserId(Long userId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            cart = Cart.builder().userId(userId).build();
            cart = cartRepository.save(cart);
        }
        return cart;
    }

    /**
     * Increment the item count if the item is already in the cart,
     * else add the item to the cart.
     * @param itemId the ID of the Item
     * @param userId the ID of the User
     * @return updated Cart
     */
    @Transactional
    public Cart addItem(Long itemId, Long userId) {
        Cart cart = getCartByUserId(userId);
        for (CartItem item : cart.getItems()) {
            if (item.getItemId().equals(itemId)) {
                item.setAmount(item.getAmount() + 1);
                return cartRepository.save(cart);
            }
        }
        cart.addCartItem(itemId);
        return cartRepository.save(cart);
    }

    /**
     * Decrement the item count if the item is on the cart nad count > 1,
     * else remove the item from the cart.
     * @param itemId the ID of the Item to decrement/remove
     * @param userId the ID of the User
     * @return updated Cart
     */
    @Transactional
    public Cart removeItem(Long itemId, Long userId) {
        Cart cart = getCartByUserId(userId);
        for (CartItem item : cart.getItems()) {
            if (item.getItemId().equals(itemId)) {
                item.setAmount(item.getAmount() - 1);
                if (item.getAmount() <= 0){
                    cart.removeCartItem(item.getId());
                }
                return cartRepository.save(cart);
            }
        }
        return cartRepository.save(cart);
    }

    /**
     * Remove the CartItem regardless of the count of items
     * @param cartItemId the ID of the CartItem to remove
     * @param userId the id of the User
     * @return updated Cart
     */
    @Transactional
    public Cart removeCartItem(Long cartItemId, Long userId) {
        Cart cart = getCartByUserId(userId);
        cart.removeCartItem(cartItemId);
        return cartRepository.save(cart);
    }

    @Transactional
    @Deprecated
    public Cart updateAmount(Long userId, Long cartItemId, Integer amount) {
        Cart cart = getCartByUserId(userId);
        cart.getItems().stream()
                .filter(i -> i.getId().compareTo(cartItemId) == 0)
                .findFirst()
                .ifPresent(cartItem -> cartItem.setAmount(amount));
        return cart; //todo - this is not saving the update. Do I need this method anyway?
    }
}
