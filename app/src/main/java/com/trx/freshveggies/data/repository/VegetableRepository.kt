package com.trx.freshveggies.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.trx.freshveggies.R
import com.trx.freshveggies.data.model.CartItem
import com.trx.freshveggies.data.model.Vegetable

class VegetableRepository private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: VegetableRepository? = null
        
        fun getInstance(): VegetableRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VegetableRepository().also { INSTANCE = it }
            }
        }
    }
    
    // Sample vegetable data
    private val vegetables = listOf(
        Vegetable(1, "Tomato", 25.0, R.drawable.ic_tomato),
        Vegetable(2, "Onion", 20.0, R.drawable.ic_onion),
        Vegetable(3, "Potato", 15.0, R.drawable.ic_vegetable_placeholder),
        Vegetable(4, "Carrot", 30.0, R.drawable.ic_carrot),
        Vegetable(5, "Broccoli", 45.0, R.drawable.ic_broccoli),
        Vegetable(6, "Spinach", 35.0, R.drawable.ic_vegetable_placeholder),
        Vegetable(7, "Bell Pepper", 40.0, R.drawable.ic_bell_pepper),
        Vegetable(8, "Cucumber", 18.0, R.drawable.ic_vegetable_placeholder),
        Vegetable(9, "Cauliflower", 35.0, R.drawable.ic_vegetable_placeholder),
        Vegetable(10, "Green Beans", 28.0, R.drawable.ic_vegetable_placeholder)
    )
    
    // Cart items
    private val _cartItems = MutableLiveData<MutableList<CartItem>>(mutableListOf())
    val cartItems: LiveData<MutableList<CartItem>> = _cartItems
    
    // Cart total
    private val _cartTotal = MutableLiveData<Double>(0.0)
    val cartTotal: LiveData<Double> = _cartTotal
    
    // Cart item count
    private val _cartItemCount = MutableLiveData<Int>(0)
    val cartItemCount: LiveData<Int> = _cartItemCount
    
    fun getVegetables(): List<Vegetable> = vegetables
    
    fun addToCart(vegetable: Vegetable) {
        val currentCart = _cartItems.value ?: mutableListOf()
        val existingItem = currentCart.find { it.vegetable.id == vegetable.id }
        
        if (existingItem != null) {
            existingItem.quantity++
        } else {
            currentCart.add(CartItem(vegetable, 1))
        }
        
        _cartItems.value = currentCart
        updateCartTotals()
    }
    
    fun removeFromCart(vegetable: Vegetable) {
        val currentCart = _cartItems.value ?: mutableListOf()
        val existingItem = currentCart.find { it.vegetable.id == vegetable.id }
        
        existingItem?.let {
            if (it.quantity > 1) {
                it.quantity--
            } else {
                currentCart.remove(it)
            }
        }
        
        _cartItems.value = currentCart
        updateCartTotals()
    }
    
    fun updateQuantity(vegetable: Vegetable, quantity: Int) {
        val currentCart = _cartItems.value ?: mutableListOf()
        val existingItem = currentCart.find { it.vegetable.id == vegetable.id }
        
        existingItem?.let {
            if (quantity <= 0) {
                currentCart.remove(it)
            } else {
                it.quantity = quantity
            }
        }
        
        _cartItems.value = currentCart
        updateCartTotals()
    }
    
    fun getQuantityForVegetable(vegetable: Vegetable): Int {
        return _cartItems.value?.find { it.vegetable.id == vegetable.id }?.quantity ?: 0
    }
    
    fun clearCart() {
        _cartItems.value = mutableListOf()
        updateCartTotals()
    }
    
    private fun updateCartTotals() {
        val currentCart = _cartItems.value ?: mutableListOf()
        val total = currentCart.sumOf { it.lineTotal }
        val itemCount = currentCart.sumOf { it.quantity }
        
        _cartTotal.value = total
        _cartItemCount.value = itemCount
    }
}
