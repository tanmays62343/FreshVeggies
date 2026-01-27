package com.trx.freshveggies.ui.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.trx.freshveggies.R
import com.trx.freshveggies.databinding.ActivityAddAddressBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.trx.freshveggies.data.model.Address
import com.trx.freshveggies.ui.activity.VeggiesListingActivity
import android.content.Intent

class AddAddressActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAddAddressBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        setupSpinner()

        binding.btnSave.setOnClickListener {
            validateAndSaveAddress()
        }
    }

    private fun setupSpinner() {
        val societies = listOf("Select Society", "Green Valley", "Blue Heights", "Sunny Side", "Royal Gardens")
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, societies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSociety.adapter = adapter
    }

    fun validateAndSaveAddress(){
        val phoneNumber = binding.etPhoneNumber.text.toString().trim()
        val fullName = binding.etFullname.text.toString().trim()
        val flatNo = binding.etFlatNo.text.toString().trim()
        val society = binding.spinnerSociety.selectedItem.toString()

        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "Please enter your mobile number", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (fullName.isEmpty()) {
            Toast.makeText(this, "Please enter Full Name", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (flatNo.isEmpty()) {
            Toast.makeText(this, "Please enter Flat Number", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (society == "Select Society") {
            Toast.makeText(this, "Please select a Society", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val address = Address(
            id = java.util.UUID.randomUUID().toString(),
            phoneNumber = phoneNumber,
            fullName = fullName,
            flatNo = flatNo,
            society = society
        )

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(user.uid)
            
            val updates = hashMapOf<String, Any>(
                "name" to fullName,
                "addressList" to FieldValue.arrayUnion(address)
            )

            userRef.update(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Address Saved", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, VeggiesListingActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save address: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

}