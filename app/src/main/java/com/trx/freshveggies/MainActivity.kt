package com.trx.freshveggies

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.trx.freshveggies.databinding.ActivityMainBinding
import com.trx.freshveggies.ui.activity.CartActivity
import com.trx.freshveggies.ui.adapter.VegetableAdapter
import com.trx.freshveggies.ui.viewmodel.VegetableListViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: VegetableListViewModel by viewModels()
    private lateinit var vegetableAdapter: VegetableAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeViewModel()

        binding.layoutViewCart.setOnClickListener {
            Log.d("BRB", "Pressed")
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        setupClickListeners()
    }

    private fun setupRecyclerView() {
        vegetableAdapter = VegetableAdapter(
            vegetables = viewModel.getVegetables(),
            onAddClick = { vegetable ->
                viewModel.addToCart(vegetable)
            },
            onIncreaseClick = { vegetable ->
                viewModel.addToCart(vegetable)
            },
            onDecreaseClick = { vegetable ->
                viewModel.removeFromCart(vegetable)
            },
            getQuantity = { vegetable ->
                viewModel.getQuantityForVegetable(vegetable)
            }
        )

        binding.recyclerViewVegetables.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2) // 2 columns
            adapter = vegetableAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.cartItemCount.observe(this) { count ->
            if (count > 0) {
                binding.cardViewCart.visibility = View.VISIBLE
                binding.textViewCartItems.text = if (count == 1) "$count item" else "$count items"
            } else {
                binding.cardViewCart.visibility = View.GONE
            }
        }

        viewModel.cartTotal.observe(this) { total ->
            binding.textViewCartTotal.text = "₹${String.format("%.2f", total)}"
        }

        // Observe cart items to refresh the adapter when quantities change
        viewModel.cartItems.observe(this) {
            vegetableAdapter.notifyDataSetChanged()
        }
    }

    private fun setupClickListeners() {

    }
}