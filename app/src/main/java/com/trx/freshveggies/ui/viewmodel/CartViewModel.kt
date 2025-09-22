package com.trx.freshveggies.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.trx.freshveggies.data.model.CartItem
import com.trx.freshveggies.data.model.Vegetable
import com.trx.freshveggies.data.repository.VegetableRepository

class CartViewModel : ViewModel() {
    
    private val repository = VegetableRepository.getInstance()
    
    val cartItems: LiveData<MutableList<CartItem>> = repository.cartItems
    val cartTotal: LiveData<Double> = repository.cartTotal
    val cartItemCount: LiveData<Int> = repository.cartItemCount
    
    fun updateQuantity(vegetable: Vegetable, quantity: Int) {
        repository.updateQuantity(vegetable, quantity)
    }
    
    fun processPayment() {
        val items = cartItems.value ?: return
        val total = cartTotal.value ?: 0.0
        
        Log.d("PAYMENT", "=== PAYMENT DETAILS ===")
        Log.d("PAYMENT", "Items purchased:")
        
        items.forEach { cartItem ->
            Log.d("PAYMENT", "${cartItem.vegetable.name} - Qty: ${cartItem.quantity}, Unit: ₹${cartItem.vegetable.price}, Line Total: ₹${cartItem.lineTotal}")
        }
        
        Log.d("PAYMENT", "GRAND TOTAL: ₹$total")
        Log.d("PAYMENT", "======================")
        
        // Clear cart after payment
        repository.clearCart()
    }
}
