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
import com.google.firebase.auth.FirebaseAuth
import com.trx.freshveggies.data.model.User
import com.trx.freshveggies.data.model.Address
import com.trx.freshveggies.data.model.Order
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import android.widget.ArrayAdapter
import com.trx.freshveggies.ui.activity.VeggiesListingActivity
import com.trx.freshveggies.utils.applySystemBarInsets
import androidx.activity.enableEdgeToEdge

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
        enableEdgeToEdge()
        binding.root.applySystemBarInsets(binding.bgStatusBar, binding.bgBottomBar)
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

    private var addressList: List<Address> = emptyList()

    private fun setupClickListeners() {
        
        fetchUserAddresses()

        binding.buttonPay.setOnClickListener {
            val total = viewModel.cartTotal.value ?: 0.0
            if (total <= 0.0) {
                Toast.makeText(this, "Cart is empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (addressList.isEmpty()) {
                Toast.makeText(this, "No address found. Please add an address.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            createOrderAndSave("SIMULATED_TXN_123")
        }

        /*binding.buttonPay.setOnClickListener {
            val total = viewModel.cartTotal.value ?: 0.0
            if (total <= 0.0) {
                Toast.makeText(this, "Cart is empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (binding.spinnerAddress.selectedItem == null) {
                Toast.makeText(this, "Please select a delivery address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            //launchPaytmUpiPayment(total)
            // Simulating payment success for now
            viewModel.onPaymentSuccess("123123")
             // Real logic should be called from onPaymentSuccess (which is handled by ActivityResult), 
             // but since I'm simulating it via button click as per existing code comment,
             // checking if onPaymentSuccess calls something? No, it just updates ViewModel.
             // I need to intercept the success or handle it.
             // The existing code has `paymentRef?.let { viewModel.onPaymentSuccess(it) }` in `setupUpiLauncher`.
             // And button click calls `viewModel.onPaymentSuccess("123123")` directly (commented out real launch).
             
             // I will override the button click to call my order placing logic directly for now as per the "Simulator" comment.
             // OR better, create a function createOrder(txnId)
             createOrderAndSave("SIMULATED_TXN_123")
        }*/

    }
    
    private fun fetchUserAddresses() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userModel = document.toObject(User::class.java)
                        if (userModel != null) {
                            addressList = userModel.addressList
                            setupAddressSpinner()
                        }
                    }
                }
        }
    }
    
    private fun setupAddressSpinner() {
        val addressStrings = addressList.map { "${it.fullName}, ${it.flatNo}, ${it.society}, ${it.phoneNumber}" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, addressStrings)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerAddress.adapter = adapter
    }

    private fun createOrderAndSave(txnId: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedAddressIndex = binding.spinnerAddress.selectedItemPosition
        if (selectedAddressIndex == -1 || addressList.isEmpty()) {
            Toast.makeText(this, "Please select an address", Toast.LENGTH_SHORT).show()
            return
        }

        val currentItems = viewModel.cartItems.value ?: emptyList()
        if (currentItems.isEmpty()) {
            Toast.makeText(this, "Cart became empty before placing order", Toast.LENGTH_SHORT).show()
            return
        }

        val totalAmount = viewModel.cartTotal.value ?: 0.0
        if (totalAmount <= 0.0) {
            Toast.makeText(this, "Total amount invalid", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedAddress = addressList[selectedAddressIndex]
        val orderId = java.util.UUID.randomUUID().toString()

        val order = Order(
            id = orderId,
            uid = user.uid,
            items = currentItems,
            totalAmount = totalAmount,
            paymentRefId = txnId,
            timeStamp = System.currentTimeMillis(),
            address = selectedAddress,
            phoneNumber = user.phoneNumber ?: "",
            status = "Pending"
        )

        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()

        val globalOrderRef = db.collection("orders").document(orderId)
        batch.set(globalOrderRef, order)

        val userRef = db.collection("users").document(user.uid)
        batch.update(userRef, "orderList", FieldValue.arrayUnion(order))

        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(this, "Order Placed Successfully!", Toast.LENGTH_SHORT).show()

                // clear cart AFTER successful save
                viewModel.processPayment()

                val intent = Intent(this, VeggiesListingActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to place order: ${e.message}", Toast.LENGTH_LONG).show()
                android.util.Log.e("ORDER_SAVE", "Batch commit failed", e)
            }
    }

}
