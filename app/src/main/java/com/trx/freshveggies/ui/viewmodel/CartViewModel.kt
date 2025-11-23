package com.trx.freshveggies.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trx.freshveggies.data.model.CartItem
import com.trx.freshveggies.data.model.Order
import com.trx.freshveggies.data.model.Vegetable
import com.trx.freshveggies.data.repository.VegetableRepository
import kotlinx.coroutines.launch

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

    /*fun onPaymentSuccess(paymentRefId: String?){

    }*/

    fun onPaymentSuccess(paymentRefId: String) {
        val itemsSnapshot = cartItems.value?.map { it.copy() } ?: emptyList()
        val total = cartTotal.value ?: 0.0

        val order = Order(
            id = System.currentTimeMillis().toString(),
            items = itemsSnapshot,
            totalAmount = total,
            paymentRefId = paymentRefId,
            timeStamp = System.currentTimeMillis()
        )

        Log.d("PAYMENT", "=== PAYMENT DETAILS ===")
        Log.d("PAYMENT", "Items purchased:")
        itemsSnapshot.forEach { cartItem ->
            Log.d(
                "PAYMENT",
                "${cartItem.vegetable.name} - Qty: ${cartItem.quantity}, " +
                        "Unit: ₹${cartItem.vegetable.price}, Line Total: ₹${cartItem.lineTotal}"
            )
        }
        Log.d("PAYMENT", "GRAND TOTAL: ₹$total")
        Log.d("PAYMENT", "Payment Ref: $paymentRefId")
        Log.d("PAYMENT", "======================")

        viewModelScope.launch {
            // 1. Save order
            //repository.saveOrder(order)

            // 2. Send bill to admin via backend
            try {
                //repository.sendBillEmailToAdmin(order)
            } catch (e: Exception) {
                Log.e("PAYMENT", "Failed to email bill: ${e.message}", e)
            }

            // 3. Clear cart
            repository.clearCart()

            //4.Redirect to main screen

        }
    }

}
