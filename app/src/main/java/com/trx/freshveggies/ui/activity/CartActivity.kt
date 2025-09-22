package com.trx.freshveggies.ui.activity

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.trx.freshveggies.databinding.ActivityCartBinding
import com.trx.freshveggies.ui.adapter.CartAdapter
import com.trx.freshveggies.ui.viewmodel.CartViewModel

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private val viewModel: CartViewModel by viewModels()
    private lateinit var cartAdapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        observeViewModel()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            cartItems = mutableListOf(),
            onIncreaseClick = { vegetable ->
                val currentQuantity = viewModel.cartItems.value?.find { it.vegetable.id == vegetable.id }?.quantity ?: 0
                viewModel.updateQuantity(vegetable, currentQuantity + 1)
            },
            onDecreaseClick = { vegetable ->
                val currentQuantity = viewModel.cartItems.value?.find { it.vegetable.id == vegetable.id }?.quantity ?: 0
                viewModel.updateQuantity(vegetable, currentQuantity - 1)
            }
        )

        binding.recyclerViewCartItems.apply {
            layoutManager = LinearLayoutManager(this@CartActivity)
            adapter = cartAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.cartItems.observe(this) { cartItems ->
            cartAdapter.updateCartItems(cartItems)
            
            if (cartItems.isEmpty()) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.recyclerViewCartItems.visibility = View.GONE
                binding.cardViewCheckout.visibility = View.GONE
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.recyclerViewCartItems.visibility = View.VISIBLE
                binding.cardViewCheckout.visibility = View.VISIBLE
            }
        }

        viewModel.cartTotal.observe(this) { total ->
            binding.textViewGrandTotal.text = "₹${String.format("%.2f", total)}"
        }
    }

    private fun setupClickListeners() {
        binding.buttonPay.setOnClickListener {
            viewModel.processPayment()
            
            Snackbar.make(
                binding.root,
                "Payment successful! Check Logcat for details.",
                Snackbar.LENGTH_LONG
            ).show()
            
            // Finish activity after payment
            finish()
        }
    }
}
