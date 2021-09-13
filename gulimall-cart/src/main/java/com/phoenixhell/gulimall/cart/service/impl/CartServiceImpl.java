package com.phoenixhell.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.phoenixhell.common.constant.CartConstant;
import com.phoenixhell.common.utils.R;
import com.phoenixhell.gulimall.cart.feign.SkuInfoFeignService;
import com.phoenixhell.gulimall.cart.interceptor.CartInterceptor;
import com.phoenixhell.gulimall.cart.service.CartService;
import com.phoenixhell.gulimall.cart.vo.Cart;
import com.phoenixhell.gulimall.cart.vo.SkuInfoVo;
import com.phoenixhell.gulimall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private SkuInfoFeignService skuInfoFeignService;
    @Autowired
    private ThreadPoolExecutor executor;


    @Override
    public Cart getCart() {
        UserInfoTo userInfoTo = CartInterceptor.userInfoToThreadLocal.get();
        List<Cart.CartItem> cartItemsId = null;
        List<Cart.CartItem> cartItemsKey = null;
        //登录状态
        if (userInfoTo.getUserId() != null) {
            BoundHashOperations<String, Object, Object> operationsUserId = redisTemplate.boundHashOps(CartConstant.CART_PREFIX + userInfoTo.getUserId());
            cartItemsId = JSON.parseObject(operationsUserId.values().toString(), new TypeReference<List<Cart.CartItem>>() {
            });
        }
        BoundHashOperations<String, Object, Object> operationsKey = redisTemplate.boundHashOps(CartConstant.CART_PREFIX + userInfoTo.getUserKey());
        cartItemsKey = JSON.parseObject(operationsKey.values().toString(), new TypeReference<List<Cart.CartItem>>() {
        });

        Cart cart = new Cart();

        //登录状态
        if (userInfoTo.getUserId() != null) {
            //合并购物车
            HashMap<Long, Cart.CartItem> cartItems = new HashMap<>();

            if (cartItemsId != null && cartItemsId.size() > 0) {
                cartItemsId.forEach(c -> {
                    if (cartItems.containsKey(c.getSkuId())) {
                        Cart.CartItem cartItem = cartItems.get(c.getSkuId());
                        cartItem.setCount(cartItem.getCount() + c.getCount());
                        cartItems.put(c.getSkuId(), cartItem);
                    } else {
                        cartItems.put(c.getSkuId(), c);
                    }
                });
            }
            if (cartItemsKey != null && cartItemsKey.size() > 0) {
                cartItemsKey.forEach(c -> {
                    if (cartItems.containsKey(c.getSkuId())) {
                        Cart.CartItem cartItem = cartItems.get(c.getSkuId());
                        cartItem.setCount(cartItem.getCount() + c.getCount());
                        cartItems.put(c.getSkuId(), cartItem);
                    } else {
                        cartItems.put(c.getSkuId(), c);
                    }
                });
                //合并完成后删除临时购物车
                redisTemplate.delete(CartConstant.CART_PREFIX + userInfoTo.getUserKey());
            }
            List<Cart.CartItem> items = cartItems.values().stream().collect(Collectors.toList());
            cart.setItems(items);
        } else {
            cart.setItems(cartItemsKey);
        }
        return cart;
    }

    @Override
    public Cart.CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> operations = getCartRedisOperations();
        String cartItemJson = (String) operations.get(skuId.toString());
        if (!StringUtils.isEmpty(cartItemJson)) {
            Cart.CartItem cartItem = JSON.parseObject(cartItemJson, new TypeReference<Cart.CartItem>() {
            });
            cartItem.setCount(cartItem.getCount() + num);
            String jsonString = JSON.toJSONString(cartItem);
            operations.put(skuId.toString(), jsonString);
            return cartItem;
        }
        //添加新商品
        Cart.CartItem cartItem = new Cart.CartItem();
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            //远程查询商品信息
            R info = skuInfoFeignService.info(skuId);
            SkuInfoVo skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoVo>() {
            });

            cartItem.setSkuId(skuId);
            cartItem.setChecked(true);
            cartItem.setCount(num);
            cartItem.setTitle(skuInfo.getSkuTitle());
            cartItem.setPrice(skuInfo.getPrice());
            cartItem.setDefaultImg(skuInfo.getSkuDefaultImg());
        }, executor);

        //远程查询当前skuid的销售属性(也可以在查询vo的时候一起返回) 这里只是复习下异步
        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
            List<String> saleAttrValues = skuInfoFeignService.getSaleAttrValues(skuId);
            cartItem.setSkuAttr(saleAttrValues);
        }, executor);
        CompletableFuture.allOf(future, future2).get();
        String jsonString = JSON.toJSONString(cartItem);
        operations.put(skuId.toString(), jsonString);
        return cartItem;
    }

    @Override
    public Cart.CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> operations = getCartRedisOperations();
        String cartItemJson = (String) operations.get(skuId.toString());
        Cart.CartItem cartItem = JSON.parseObject(cartItemJson, new TypeReference<Cart.CartItem>() {
        });
        return cartItem;
    }

    @Override
    public void clearCart() {
        UserInfoTo userInfoTo = CartInterceptor.userInfoToThreadLocal.get();
        if (userInfoTo.getUserId() != null) {
            redisTemplate.delete(CartConstant.CART_PREFIX + userInfoTo.getUserId());
        }
        redisTemplate.delete(CartConstant.CART_PREFIX + userInfoTo.getUserKey());
    }

    @Override
    public void check(String isChecked, List<String> skuIds) {
        skuIds.forEach(skuId -> {
            boolean b = "1".equalsIgnoreCase(isChecked);
            updateCartItem(skuId, b, "setChecked");
        });
    }

    //参数1 要操作的商品存储id   操作参数   操作方法
    private void updateCartItem(String skuId, Object param, String methodString) {
        try {
            BoundHashOperations<String, Object, Object> operations = getCartRedisOperations();
            String jsonString = (String) operations.get(skuId);
            Cart.CartItem cartItem = JSON.parseObject(jsonString, new TypeReference<Cart.CartItem>() {
            });

            Class<Cart.CartItem> cartItemClazz = Cart.CartItem.class;

            if (cartItem != null) {
                if (param instanceof String) {
                    Method method = cartItemClazz.getDeclaredMethod(methodString, String.class);
                    method.setAccessible(true);
                    method.invoke(cartItem, (String) param);
                }

                if (param instanceof Integer) {
                    Method method = cartItemClazz.getDeclaredMethod(methodString, Integer.class);
                    method.setAccessible(true);
                    method.invoke(cartItem, (Integer) param);
                }

                if (param instanceof BigDecimal) {
                    Method method = cartItemClazz.getDeclaredMethod(methodString, BigDecimal.class);
                    method.setAccessible(true);
                    method.invoke(cartItem, (BigDecimal) param);
                }

                if (param instanceof Long) {
                    Method method = cartItemClazz.getDeclaredMethod(methodString, Long.class);
                    method.setAccessible(true);
                    method.invoke(cartItem, (Long) param);
                }

                if (param instanceof Boolean) {
                    Method method = cartItemClazz.getDeclaredMethod(methodString, Boolean.class);
                    method.setAccessible(true);
                    method.invoke(cartItem, (Boolean) param);
                }

                String cartItemJson = JSON.toJSONString(cartItem);
                operations.put(skuId, cartItemJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addCount(String skuId, Integer count) {
        updateCartItem(skuId, count, "setCount");
    }

    @Override
    public void deleteCartItems(String[] skuIds) {
        BoundHashOperations<String, Object, Object> operations = getCartRedisOperations();
        operations.delete(skuIds);
    }

    //选出选中的购物项
    @Override
    public List<Cart.CartItem> getCheckedCartItems() {

        BoundHashOperations<String, Object, Object> operations = getCartRedisOperations();
        List<Cart.CartItem> cartItems = operations.values().stream().map(item -> {
                    String s = (String) item;
                    Cart.CartItem cartItem = JSON.parseObject(s, new TypeReference<Cart.CartItem>() {
                    });
                    return cartItem;
                })
                .filter(cartItem -> cartItem.getChecked())
                .map(cartItem -> {
                    //远程查询当前商品的价格
                    BigDecimal skuPrice = skuInfoFeignService.getSkuPrice(cartItem.getSkuId());
                    cartItem.setPrice(skuPrice);
                    return cartItem;
                })
                .collect(Collectors.toList());
        return cartItems;
    }

    //操作购物车经常要用 抽取出来封装成方法
    private BoundHashOperations<String, Object, Object> getCartRedisOperations() {
        UserInfoTo userInfoTo = CartInterceptor.userInfoToThreadLocal.get();

        String cartKey = "";
        if (userInfoTo.getUserId() != null) {
            cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserId();
        } else {
            cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserKey();
        }
        //所有都是hash操作 太麻烦 有个简化方法
        //Object o = redisTemplate.opsForHash().get(cartKey, skuId);

        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
        return operations;
    }
}
