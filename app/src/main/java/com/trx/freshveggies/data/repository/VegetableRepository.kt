package com.trx.freshveggies.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.trx.freshveggies.R
import com.trx.freshveggies.data.model.CartItem
import com.trx.freshveggies.data.model.Vegetable

class VegetableRepository private constructor() {

    private val db = Firebase.firestore
    
    companion object {
        @Volatile
        private var INSTANCE: VegetableRepository? = null
        
        fun getInstance(): VegetableRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VegetableRepository().also { INSTANCE = it }
            }
        }
    }

    //Veggies list
    private val _vegetables = MutableLiveData<List<Vegetable>>()
    val vegetables: LiveData<List<Vegetable>> get() = _vegetables

    init {
        fetchVegetablesOnce()
    }
    
    // Cart items
    private val _cartItems = MutableLiveData<MutableList<CartItem>>(mutableListOf())
    val cartItems: LiveData<MutableList<CartItem>> = _cartItems
    
    // Cart total
    private val _cartTotal = MutableLiveData<Double>(0.0)
    val cartTotal: LiveData<Double> = _cartTotal
    
    // Cart item count
    private val _cartItemCount = MutableLiveData<Int>(0)
    val cartItemCount: LiveData<Int> = _cartItemCount

    // If you prefer one-time fetch instead of real-time:
    fun fetchVegetablesOnce() {
        db.collection("products")
            .get()
            .addOnSuccessListener { snapshot ->
                //Log.d("BRB", "Veggies: ${snapshot.documents}")
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Vegetable::class.java)
                }
                _vegetables.postValue(list)
                //vegetables = list
                Log.d("BRB", "listenToVegetables: $list")
            }
            .addOnFailureListener {
                Log.e("BRB", "Error getting vegetables", it)
            }
    }
    
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
