package com.example.lankasmartmart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartManager {
    private static CartManager instance;
    private final Map<Integer, CartItemModel> cartItems;

    private CartManager() {
        cartItems = new HashMap<>();
    }

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addToCart(Product product, int quantity) {
        if (cartItems.containsKey(product.getId())) {
            CartItemModel item = cartItems.get(product.getId());
            if (item != null) {
                item.setQuantity(item.getQuantity() + quantity);
            }
        } else {
            cartItems.put(product.getId(), new CartItemModel(product, quantity));
        }
    }

    public void removeFromCart(int productId) {
        cartItems.remove(productId);
    }

    public void updateQuantity(int productId, int quantity) {
        if (cartItems.containsKey(productId)) {
            if (quantity <= 0) {
                removeFromCart(productId);
            } else {
                CartItemModel item = cartItems.get(productId);
                if (item != null) {
                    item.setQuantity(quantity);
                }
            }
        }
    }

    public List<CartItemModel> getCartItems() {
        return new ArrayList<>(cartItems.values());
    }

    public double getCartTotal() {
        double total = 0;
        for (CartItemModel item : cartItems.values()) {
            total += item.getProduct().getPrice() * item.getQuantity();
        }
        return total;
    }

    public int getItemCount() {
        int count = 0;
        for (CartItemModel item : cartItems.values()) {
            count += item.getQuantity();
        }
        return count;
    }

    public void clearCart() {
        cartItems.clear();
    }

    public static class CartItemModel {
        private Product product;
        private int quantity;

        public CartItemModel(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public Product getProduct() { return product; }
        public void setProduct(Product product) { this.product = product; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public double getTotal() {
            return product.getPrice() * quantity;
        }
    }
}