package com.trx.freshveggies.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.trx.freshveggies.data.model.CartItem
import com.trx.freshveggies.data.model.Vegetable
import com.trx.freshveggies.data.repository.VegetableRepository

class VegetableListViewModel : ViewModel() {
    
    private val repository = VegetableRepository.getInstance()
    
    val cartItems: LiveData<MutableList<CartItem>> = repository.cartItems
    val cartTotal: LiveData<Double> = repository.cartTotal
    val cartItemCount: LiveData<Int> = repository.cartItemCount

    val vegetables: LiveData<List<Vegetable>> = repository.vegetables
    
    fun addToCart(vegetable: Vegetable) {
        repository.addToCart(vegetable)
    }
    
    fun removeFromCart(vegetable: Vegetable) {
        repository.removeFromCart(vegetable)
    }
    
    fun getQuantityForVegetable(vegetable: Vegetable): Int {
        return repository.getQuantityForVegetable(vegetable)
    }
    
    fun updateQuantity(vegetable: Vegetable, quantity: Int) {
        repository.updateQuantity(vegetable, quantity)
    }
}
