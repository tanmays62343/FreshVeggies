package com.trx.freshveggies.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.trx.freshveggies.R
import com.trx.freshveggies.data.model.Order
import com.trx.freshveggies.databinding.ActivityOrdersBinding
import com.trx.freshveggies.ui.adapter.OrdersAdapter
import androidx.activity.enableEdgeToEdge
import com.trx.freshveggies.utils.applySystemBarInsets
import java.time.LocalDate
import java.time.ZoneId

class OrdersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrdersBinding
    private lateinit var ordersAdapter: OrdersAdapter
    private val firestore = FirebaseFirestore.getInstance()
    
    private var isFilterToday = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        binding.root.applySystemBarInsets(binding.bgStatusBar, binding.bgBottomBar)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()
        
        // Initial load
        setFilter(true)
    }

    private fun setupRecyclerView() {
        ordersAdapter = OrdersAdapter { order ->
            updateOrderStatus(order)
        }
        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(this@OrdersActivity)
            adapter = ordersAdapter
        }
    }

    private fun setupListeners() {
        binding.btnRefresh.setOnClickListener {
            fetchOrders()
            Toast.makeText(this, "Refreshing orders...", Toast.LENGTH_SHORT).show()
        }

        binding.btnFilterToday.setOnClickListener {
            setFilter(true)
        }

        binding.btnFilterAll.setOnClickListener {
            setFilter(false)
        }
    }

    private fun setFilter(today: Boolean) {
        isFilterToday = today
        updateFilterButtons()
        fetchOrders()
    }

    private fun updateFilterButtons() {
        if (isFilterToday) {
            binding.btnFilterToday.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green_primary)
            binding.btnFilterAll.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.darker_gray)
        } else {
            binding.btnFilterToday.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.darker_gray)
            binding.btnFilterAll.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green_primary)
        }
    }

    private fun fetchOrders() {
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.tvEmptyState.visibility = android.view.View.GONE
        binding.rvOrders.visibility = android.view.View.GONE

        val collectionRef = firestore.collection("orders")
        val query = if (isFilterToday) {
            val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfDay = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            collectionRef
                .whereGreaterThanOrEqualTo("timeStamp", startOfDay)
                .whereLessThanOrEqualTo("timeStamp", endOfDay)
                .orderBy("timeStamp", Query.Direction.DESCENDING)
        } else {
            collectionRef.orderBy("timeStamp", Query.Direction.DESCENDING)
        }

        query.get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = android.view.View.GONE
                val orders = documents.mapNotNull { it.toObject(Order::class.java).copy(id = it.id) }
                
                if (orders.isEmpty()) {
                    binding.tvEmptyState.visibility = android.view.View.VISIBLE
                } else {
                    binding.rvOrders.visibility = android.view.View.VISIBLE
                    ordersAdapter.submitList(orders)
                }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = android.view.View.GONE
                Toast.makeText(this, "Error fetching orders: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
    }

    private fun updateOrderStatus(order: Order) {
        if (order.id.isEmpty()) {
            Toast.makeText(this, "Invalid Order ID", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = android.view.View.VISIBLE
        firestore.collection("orders").document(order.id)
            .update("status", "Delivered")
            .addOnSuccessListener {
                binding.progressBar.visibility = android.view.View.GONE
                Toast.makeText(this, "Order marked as Delivered", Toast.LENGTH_SHORT).show()
                fetchOrders() // Refresh list
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = android.view.View.GONE
                Toast.makeText(this, "Failed to update status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
