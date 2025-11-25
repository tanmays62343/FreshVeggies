package com.trx.freshveggies.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.trx.freshveggies.data.model.Vegetable
import com.trx.freshveggies.databinding.ItemVegetableBinding

class VegetableAdapter(
    private val context : Context,
    private var vegetables: MutableList<Vegetable>,
    private val onAddClick: (Vegetable) -> Unit,
    private val onIncreaseClick: (Vegetable) -> Unit,
    private val onDecreaseClick: (Vegetable) -> Unit,
    private val getQuantity: (Vegetable) -> Int
) : RecyclerView.Adapter<VegetableAdapter.VegetableViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VegetableViewHolder {
        val binding = ItemVegetableBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VegetableViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VegetableViewHolder, position: Int) {
        holder.bind(vegetables[position])
    }

    override fun getItemCount(): Int = vegetables.size

    fun updateList(newVegetables: List<Vegetable>){
        vegetables = newVegetables.toMutableList()
        notifyDataSetChanged()
    }

    inner class VegetableViewHolder(private val binding: ItemVegetableBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(vegetable: Vegetable) {
            //binding.imageViewVegetable.setImageResource(vegetable.imageRes)

            Glide
                .with(context)
                .load(vegetable.image)
                .into(binding.imageViewVegetable)

            binding.textViewVegetableName.text = vegetable.name
            binding.textViewVegetablePrice.text = "₹${String.format("%.2f", vegetable.price)}"

            val quantity = getQuantity(vegetable)

            if (quantity > 0) {
                binding.buttonAdd.visibility = View.GONE
                binding.layoutQuantityControls.visibility = View.VISIBLE
                binding.textViewQuantity.text = quantity.toString()
            } else {
                binding.buttonAdd.visibility = View.VISIBLE
                binding.layoutQuantityControls.visibility = View.GONE
            }

            binding.buttonAdd.setOnClickListener {
                onAddClick(vegetable)
            }

            binding.buttonIncrease.setOnClickListener {
                onIncreaseClick(vegetable)
            }

            binding.buttonDecrease.setOnClickListener {
                onDecreaseClick(vegetable)
            }
        }
    }
}
