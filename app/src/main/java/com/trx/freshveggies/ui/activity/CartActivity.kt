package com.trx.freshveggies.ui.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.trx.freshveggies.databinding.ActivityCartBinding
import com.trx.freshveggies.ui.adapter.CartAdapter
import com.trx.freshveggies.ui.viewmodel.CartViewModel
import java.util.Locale

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private val viewModel: CartViewModel by viewModels()
    private lateinit var cartAdapter: CartAdapter

    private lateinit var upiLauncher: ActivityResultLauncher<Intent>

    // Replace with your actual details
    private val merchantUpiId = "7697093929@ptaxis"      // Admin / shop UPI ID
    private val merchantName = "Tanmay Deopurkar"    // Display name
    private val merchantNote = "FreshVeggies order payment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        observeViewModel()
        setupUpiLauncher()
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

    private fun setupUpiLauncher() {
        upiLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val data = result.data
            val response = data?.getStringExtra("response")
            val isSuccess = parseUpiResponse(response)

            if (isSuccess) {
                val paymentRef = extractTxnRef(response)
                Snackbar.make(binding.root, "Payment Successful!", Snackbar.LENGTH_LONG).show()
                paymentRef?.let { viewModel.onPaymentSuccess(it) }
                finish() // Close cart after successful payment (optional)
            } else {
                Snackbar.make(binding.root, "Payment Failed or Cancelled", Snackbar.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun launchPaytmUpiPayment(amount: Double) {
        val formattedAmount = String.format(Locale.US, "%.2f", amount)

        val uri = Uri.Builder()
            .scheme("upi")
            .authority("pay")
            .appendQueryParameter("pa", merchantUpiId)          // payee address (UPI ID)
            .appendQueryParameter("pn", merchantName)           // payee name
            .appendQueryParameter("tn", merchantNote)           // transaction note
            .appendQueryParameter("am", formattedAmount)        // amount
            .appendQueryParameter("cu", "INR")                  // currency
            .build()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = uri
            // Force Paytm app (if installed). If you want any UPI app, remove setPackage
            setPackage("net.one97.paytm")
        }

        try {
            upiLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            // Paytm not installed
            Toast.makeText(
                this,
                "Paytm app not found. Please install Paytm or use another UPI app.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun parseUpiResponse(response: String?): Boolean {
        if (response.isNullOrEmpty()) return false

        val pairs = response.split("&")
        var statusValue: String? = null

        for (pair in pairs) {
            val parts = pair.split("=")
            if (parts.size >= 2) {
                val key = parts[0].lowercase()
                val value = parts[1]
                if (key == "status") {
                    statusValue = value.lowercase()
                }
            }
        }
        return statusValue == "success"
    }
    private fun extractTxnRef(response: String?): String? {
        if (response.isNullOrEmpty()) return null

        val pairs = response.split("&")
        var txnRef: String? = null
        var txnId: String? = null

        for (pair in pairs) {
            val parts = pair.split("=")
            if (parts.size >= 2) {
                val key = parts[0].lowercase()
                val value = parts[1]
                when (key) {
                    "txnref" -> txnRef = value
                    "txnid" -> txnId = value
                }
            }
        }
        return txnRef ?: txnId
    }

    private fun setupClickListeners() {

        binding.buttonPay.setOnClickListener {
            val total = viewModel.cartTotal.value ?: 0.0
            if (total <= 0.0) {
                Toast.makeText(this, "Cart is empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            //launchPaytmUpiPayment(total)
            viewModel.onPaymentSuccess("123123")
        }

    }

}
