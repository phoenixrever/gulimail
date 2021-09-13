package com.phoenixhell.gulimall.cart.service;

import com.phoenixhell.gulimall.cart.vo.Cart;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface CartService {
    Cart getCart();

    Cart.CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    Cart.CartItem getCartItem(Long skuId);

    void clearCart();

    void check(String isChecked, List<String> skuId);

    void addCount(String skuId, Integer count);

    void deleteCartItems(String[] skuIds);

    List<Cart.CartItem> getCheckedCartItems();
}
