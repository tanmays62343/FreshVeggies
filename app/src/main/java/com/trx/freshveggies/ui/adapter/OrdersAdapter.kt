package com.trx.freshveggies.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.trx.freshveggies.data.model.Order
import com.trx.freshveggies.databinding.ItemOrderBinding

class OrdersAdapter(
    private val onMarkDeliveredClick: (Order) -> Unit
) : ListAdapter<Order, OrdersAdapter.OrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order)
    }

    inner class OrderViewHolder(private val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: Order) {
            val name = order.address?.fullName ?: "Unknown Customer"
            val phone = if (order.address?.phoneNumber?.isNotEmpty() == true) {
                order.address.phoneNumber
            } else {
                order.phoneNumber
            }
            
            binding.tvOrderName.text = name
            binding.tvOrderContact.text = phone
            
            val addressText = order.address?.let {
                "${it.flatNo}, ${it.society}"
            } ?: "No Address Provided"
            binding.tvOrderAddress.text = addressText

            binding.tvOrderStatus.text = order.status
            
            // Generate items summary
            // Generate items summary with Bold Quantity
            val spannableBuilder = android.text.SpannableStringBuilder()
            order.items.forEachIndexed { index, cartItem ->
                val quantityText = "${cartItem.quantity} x "
                val itemName = (cartItem.vegetable.name ?: "Item") + if (index < order.items.size - 1) "\n" else ""
                
                val start = spannableBuilder.length
                spannableBuilder.append(quantityText)
                spannableBuilder.setSpan(
                    android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                    start,
                    start + quantityText.length,
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannableBuilder.append(itemName)
            }
            binding.tvOrderItems.text = spannableBuilder
            
            binding.tvPaymentRef.text = order.paymentRefId
            binding.tvTotalAmount.text = "₹ ${order.totalAmount}"
            
            if (order.status.equals("Delivered", ignoreCase = true)) {
                binding.btnMarkDelivered.visibility = View.GONE
                binding.tvOrderStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_green_dark))
            } else {
                binding.btnMarkDelivered.visibility = View.VISIBLE
                binding.btnMarkDelivered.setOnClickListener {
                    onMarkDeliveredClick(order)
                }
                binding.tvOrderStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_orange_dark))
            }
        }
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }
}
