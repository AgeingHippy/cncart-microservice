package com.ageinghippy.cncart_microservice.service;

import com.ageinghippy.cncart_microservice.model.Cart;
import com.ageinghippy.cncart_microservice.model.CartItem;
import com.ageinghippy.cncart_microservice.repository.CartRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class CartServiceTest {

    @Mock
    CartRepository cartRepository;

    @InjectMocks
    @Spy
    CartService cartService;

    private AutoCloseable autoCloseable;

    @BeforeEach
    public void init() {
        autoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void cleanUp() throws Exception {
        autoCloseable.close();
    }

    @Test
    void getCartByUserId_cart_does_not_exist() {
        //WHEN the cart does not exist in the database
        Long userId = 1L;
        Cart testCart = new Cart();

        ArgumentCaptor<Cart> argument = ArgumentCaptor.forClass(Cart.class);

        doReturn(null).when(cartRepository).findByUserId(userId);
        doReturn(testCart).when(cartRepository).save(argument.capture());

        //WHEN the cart is requested
        Cart cart = cartService.getCartByUserId(1L);

        //THEN a new empty cart with the userId was saved and returned
        assertEquals(argument.getValue().getUserId(), userId);
        assertNotNull(cart);
        assertEquals(cart, testCart);
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void getCartByUserId_cart_does_exist() {
        //GIVEN the cart does exist in the database
        Long userId = 2L;
        Cart testCart = Cart.builder().id(22L).userId(userId).build();

        doReturn(testCart).when(cartRepository).findByUserId(userId);

        //WHEN the cart is requested
        Cart cart = cartService.getCartByUserId(userId);

        //THEN the expected cart is returned
        assertEquals(cart, testCart);
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, times(0)).save(any(Cart.class));
    }

    @Test
    void addItem_item_not_already_in_cart() {
        //GIVEN the cart does not already contain the item
        Long userId = 22L;
        Long itemId = 17L;
        Cart testCart = Cart.builder().id(10L).userId(userId).build();

        doReturn(testCart).when(cartService).getCartByUserId(userId);
        when(cartRepository.save(any(Cart.class))).then(AdditionalAnswers.returnsFirstArg());

        //WHEN we add an item to an empty cart
        Cart cart = cartService.addItem(itemId, userId);

        //THEN the expected cart is returned with the item added and the count  of items is 1
        assertEquals(cart.getId(), testCart.getId());
        assertEquals(cart.getUserId(), testCart.getUserId());
        assertEquals(cart.getItems().size(), 1);
        assertEquals(cart.getItems().getFirst().getItemId(), itemId);
        assertEquals(cart.getItems().getFirst().getAmount(), 1);
    }

    @Test
    void addItem_item_already_in_cart() {
        //GIVEN the cart does already contain the item
        Long userId = 22L;
        Long itemId = 17L;
        Long cartItemId = 132L;
        Cart testCart = Cart.builder()
                .id(10L)
                .userId(userId)
                .items(List.of(
                        CartItem.builder().id(44L).itemId(3L).amount(1).build(),
                        CartItem.builder().id(cartItemId).itemId(itemId).amount(3).build()
                ))
                .build();

        doReturn(testCart).when(cartService).getCartByUserId(userId);
        when(cartRepository.save(any(Cart.class))).then(AdditionalAnswers.returnsFirstArg());

        //WHEN we add an item to a cart already containing the item
        Cart cart = cartService.addItem(itemId, userId);

        //THEN the expected cart is returned with the item added and the count  of items is incremented by 1
        assertEquals(cart.getId(), testCart.getId());
        assertEquals(cart.getUserId(), testCart.getUserId());
        assertEquals(cart.getItems().size(), 2);
        CartItem modifiedCartItem = cart.getItems().stream()
                .filter(item -> item.getItemId().equals(itemId))
                .findFirst().orElseThrow(AssertionError::new);
        assertEquals(modifiedCartItem.getItemId(), itemId);
        assertEquals(modifiedCartItem.getAmount(), 4);
    }

    @Test
    void removeItem_withCount_more_than_one() {
        //GIVEN the cart contains the item with an amount > 0
        Long userId = 22L;
        Long itemId = 17L;
        Long cartItemId = 132L;
        Cart testCart = Cart.builder()
                .id(10L)
                .userId(userId)
                .items(List.of(
                        CartItem.builder().id(44L).itemId(3L).amount(1).build(),
                        CartItem.builder().id(cartItemId).itemId(itemId).amount(3).build()
                ))
                .build();

        doReturn(testCart).when(cartService).getCartByUserId(userId);
        when(cartRepository.save(any(Cart.class))).then(AdditionalAnswers.returnsFirstArg());

        //WHEN we remove an item from the cart
        Cart cart = cartService.removeItem(itemId, userId);

        //THEN the expected cart is returned with the item count decremented by 1
        assertEquals(cart.getId(), testCart.getId());
        assertEquals(cart.getUserId(), testCart.getUserId());
        assertEquals(cart.getItems().size(), 2);
        CartItem modifiedCartItem = cart.getItems().stream()
                .filter(item -> item.getItemId().equals(itemId))
                .findFirst().orElseThrow(AssertionError::new);
        assertEquals(modifiedCartItem.getItemId(), itemId);
        assertEquals(modifiedCartItem.getAmount(), 2);
    }

    @Test
    void removeItem_withCount_one() {
        //GIVEN the cart contains the item with an amount > 0
        Long userId = 22L;
        Long itemId = 17L;
        Long cartItemId = 132L;
        ArrayList<CartItem> cartItems = new ArrayList<>();
        cartItems.add(CartItem.builder().id(44L).itemId(3L).amount(1).build());
        cartItems.add(CartItem.builder().id(cartItemId).itemId(itemId).amount(1).build());

        Cart testCart = Cart.builder()
                .id(10L)
                .userId(userId)
                .items(cartItems)
                .build();

        doReturn(testCart).when(cartService).getCartByUserId(userId);
        when(cartRepository.save(any(Cart.class))).then(AdditionalAnswers.returnsFirstArg());

        //WHEN we remove an item from the cart
        Cart cart = cartService.removeItem(itemId, userId);

        //THEN the expected cart is returned with the cartItem removed
        assertEquals(cart.getId(), testCart.getId());
        assertEquals(cart.getUserId(), testCart.getUserId());
        assertEquals(cart.getItems().size(), 1);
        CartItem remainCartItem = cart.getItems().getFirst();
        assertEquals(remainCartItem.getId(), 44L);
        assertEquals(remainCartItem.getItemId(), 3L);
        assertEquals(remainCartItem.getAmount(), 1);
    }

    @Test
    void removeItem_notFound() {
        //GIVEN the cart does not contain the item
        Long userId = 22L;
        Long itemId = 17L;
        ArrayList<CartItem> cartItems = new ArrayList<>();
        cartItems.add(CartItem.builder().id(44L).itemId(3L).amount(1).build());

        Cart testCart = Cart.builder()
                .id(10L)
                .userId(userId)
                .items(cartItems)
                .build();

        doReturn(testCart).when(cartService).getCartByUserId(userId);
        when(cartRepository.save(any(Cart.class))).then(AdditionalAnswers.returnsFirstArg());

        //WHEN we remove an item from the cart
        Cart cart = cartService.removeItem(itemId, userId);

        //THEN the expected cart is returned with no changes
        assertEquals(cart.getId(), testCart.getId());
        assertEquals(cart.getUserId(), testCart.getUserId());
        assertEquals(cart.getItems().size(), 1);
        CartItem remainCartItem = cart.getItems().getFirst();
        assertEquals(remainCartItem.getId(), 44L);
        assertEquals(remainCartItem.getItemId(), 3L);
        assertEquals(remainCartItem.getAmount(), 1);
    }

    @Test
    void removeCartItem() {
        //GIVEN the cart contains the item
        Long userId = 22L;
        Long itemId = 17L;
        Long cartItemId = 132L;
        ArrayList<CartItem> cartItems = new ArrayList<>();
        cartItems.add(CartItem.builder().id(44L).itemId(3L).amount(1).build());
        cartItems.add(CartItem.builder().id(cartItemId).itemId(itemId).amount(2).build());

        Cart testCart = Cart.builder()
                .id(10L)
                .userId(userId)
                .items(cartItems)
                .build();

        doReturn(testCart).when(cartService).getCartByUserId(userId);
        when(cartRepository.save(any(Cart.class))).then(AdditionalAnswers.returnsFirstArg());

        //WHEN we remove an item from the cart
        Cart cart = cartService.removeCartItem(cartItemId, userId);

        //THEN the expected cart is returned with the cartItem removed
        assertEquals(cart.getId(), testCart.getId());
        assertEquals(cart.getUserId(), testCart.getUserId());
        assertEquals(cart.getItems().size(), 1);
        CartItem remainCartItem = cart.getItems().getFirst();
        assertEquals(remainCartItem.getId(), 44L);
        assertEquals(remainCartItem.getItemId(), 3L);
        assertEquals(remainCartItem.getAmount(), 1);
    }

    @Test
    void removeCartItem_itemNotFound() {
        //GIVEN the cart contains the item
        Long userId = 22L;
        Long itemId = 17L;
        Long cartItemId = 132L;
        ArrayList<CartItem> cartItems = new ArrayList<>();
        cartItems.add(CartItem.builder().id(44L).itemId(3L).amount(1).build());

        Cart testCart = Cart.builder()
                .id(10L)
                .userId(userId)
                .items(cartItems)
                .build();

        doReturn(testCart).when(cartService).getCartByUserId(userId);
        when(cartRepository.save(any(Cart.class))).then(AdditionalAnswers.returnsFirstArg());

        //WHEN we remove an item from the cart
        Cart cart = cartService.removeCartItem(cartItemId, userId);

        //THEN the expected cart is returned with the cartItem removed
        assertEquals(cart.getId(), testCart.getId());
        assertEquals(cart.getUserId(), testCart.getUserId());
        assertEquals(cart.getItems().size(), 1);
        CartItem remainCartItem = cart.getItems().getFirst();
        assertEquals(remainCartItem.getId(), 44L);
        assertEquals(remainCartItem.getItemId(), 3L);
        assertEquals(remainCartItem.getAmount(), 1);
    }
}