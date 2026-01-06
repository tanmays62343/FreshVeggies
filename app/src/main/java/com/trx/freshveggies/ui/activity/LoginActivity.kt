package com.trx.freshveggies.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.trx.freshveggies.databinding.ActivityLoginBinding
import java.util.concurrent.TimeUnit
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import com.trx.freshveggies.data.model.User
import com.trx.freshveggies.ui.activity.AddAddressActivity
import com.trx.freshveggies.ui.activity.VeggiesListingActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding

    //Authentication
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var verificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.cardContainer) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, systemBars.bottom + v.paddingBottom)
            insets
        }
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }


        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.btnSendCode.setOnClickListener {
            val phoneNumber = "+91" + binding.etPhoneNumber.text.toString()
            if(phoneNumber.isEmpty()){
                Toast.makeText(this, "Please enter the phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startPhoneNumberVerification(phoneNumber)
        }

        binding.btnVerifyOtp.setOnClickListener {
            val otp = binding.editTextOtp.text.toString()
            if (otp.length != 6) {
                Toast.makeText(this, "Enter valid 6-digit OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            verifyPhoneNumberWithCode(otp)
        }

        if(auth.currentUser != null){
            navigateToVeggiesListing()
        }

    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // Auto-retrieval or instant verification
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // Show error to user
            Log.d("BRB", "Verification error - $e")
            Toast.makeText(this@LoginActivity, "Please check the entered mobile number",
                Toast.LENGTH_SHORT).show()
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            this@LoginActivity.verificationId = verificationId

            // Switch UI to OTP screen
            binding.layoutPhoneInput.visibility = View.GONE
            binding.layoutOtpInput.visibility = View.VISIBLE

            binding.btnSendCode.isEnabled = false
        }
    }

    private fun verifyPhoneNumberWithCode(code: String) {
        val verId = verificationId ?: return
        val credential = PhoneAuthProvider.getCredential(verId, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        onFirebaseUserSignedIn(user)
                    }
                } else {
                    // Show error
                }
            }
    }

    private fun onFirebaseUserSignedIn(firebaseUser: FirebaseUser) {
        val uid = firebaseUser.uid
        val phoneNumber = firebaseUser.phoneNumber ?: ""

        val userRef = firestore.collection("users").document(uid)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val user = document.toObject(User::class.java)
                if (user != null && user.addressList.isNotEmpty()) {
                    navigateToVeggiesListing()
                } else {
                    navigateToAddAddress()
                }
            } else {
                val newUser = User(
                    uid = uid,
                    phoneNumber = phoneNumber,
                    addressList = emptyList(),
                    orderList = emptyList()
                )
                userRef.set(newUser).addOnSuccessListener {
                    navigateToAddAddress()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error creating user: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        .addOnFailureListener { e ->
            Toast.makeText(this, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToAddAddress() {
        val intent = Intent(this, AddAddressActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToVeggiesListing() {
        val intent = Intent(this, VeggiesListingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }


}