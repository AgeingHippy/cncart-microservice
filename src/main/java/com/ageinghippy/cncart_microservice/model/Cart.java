package com.ageinghippy.cncart_microservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "cart_id")
    @Getter(AccessLevel.NONE)
    private List<CartItem> items;

    public List<CartItem> getItems() {
        initiateItems();
        return this.items;
    }

    public void addCartItem(Long itemId) {
        initiateItems();
        items.add(CartItem.builder().itemId(itemId).amount(1).build());
    }

    public void removeCartItem(Long cartItemId) {
        initiateItems();
        items.removeIf(ci -> ci.getId().equals(cartItemId));
    }

    private void initiateItems() {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
    }
}

