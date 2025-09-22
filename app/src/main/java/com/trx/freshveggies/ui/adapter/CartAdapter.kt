package com.trx.freshveggies.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.trx.freshveggies.data.model.CartItem
import com.trx.freshveggies.data.model.Vegetable
import com.trx.freshveggies.databinding.ItemCartBinding

class CartAdapter(
    private var cartItems: MutableList<CartItem>,
    private val onIncreaseClick: (Vegetable) -> Unit,
    private val onDecreaseClick: (Vegetable) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(private val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cartItem: CartItem) {
            binding.textViewItemName.text = cartItem.vegetable.name
            binding.textViewUnitPrice.text = "₹${String.format("%.2f", cartItem.vegetable.price)} per unit"
            binding.textViewQuantity.text = cartItem.quantity.toString()
            binding.textViewLineTotal.text = "₹${String.format("%.2f", cartItem.lineTotal)}"

            binding.buttonIncrease.setOnClickListener {
                onIncreaseClick(cartItem.vegetable)
            }

            binding.buttonDecrease.setOnClickListener {
                onDecreaseClick(cartItem.vegetable)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateCartItems(newCartItems: MutableList<CartItem>) {
        cartItems = newCartItems
        notifyDataSetChanged()
    }
}
