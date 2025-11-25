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

        binding.btnSave.setOnClickListener {
            validateAndSaveAddress()
        }
    }

    fun validateAndSaveAddress(){
        val fullName = binding.etFullname.text.toString().trim()
        val street = binding.etStreet.text.toString().trim()
        val city = binding.etCity.text.toString().trim()
        val state = binding.etState.text.toString().trim()
        val zip = binding.etZip.text.toString().trim()
        if (fullName.isEmpty()) {
            Toast.makeText(this, "Please enter Full Name", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (street.isEmpty()) {
            Toast.makeText(this, "Please enter Street Address", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (city.isEmpty()) {
            Toast.makeText(this, "Please enter City", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (state.isEmpty()) {
            Toast.makeText(this, "Please enter State", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (zip.isEmpty()) {
            Toast.makeText(this, "Please enter Zip Code", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        val addressString = "$fullName,\n$street,\n$city,$state,\n$zip"
        Log.d("BRB", "Address: $addressString")
    }

}