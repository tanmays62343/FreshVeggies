package com.trx.freshveggies.ui.activity

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.trx.freshveggies.R
import com.trx.freshveggies.data.model.User
import com.trx.freshveggies.databinding.ActivityGoogleLoginBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GoogleLoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityGoogleLoginBinding

    private lateinit var credentialManager : CredentialManager
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityGoogleLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        credentialManager = CredentialManager.create(this)

        binding.btnGoogleLogin.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val result = buildCredentialRequest()
                    if (handelGoogleSignIn(result)) {
                        Log.d("BRB", "Valid Google Account")
                    } else {
                        runOnUiThread {
                            Toast.makeText(
                                this@GoogleLoginActivity,
                                "Sign-in failed. Try again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: NoCredentialException) {
                    // No Google account present on device → guide user to add one
                    Log.d("BRB", "No Google account on device: $e")
                    runOnUiThread {
                        openAddGoogleAccountScreen()
                    }
                } catch (e: GetCredentialCancellationException) {
                    // User cancelled the account picker / one-tap
                    Log.d("BRB", "User cancelled Google sign-in: $e")
                    runOnUiThread {
                        Toast.makeText(
                            this@GoogleLoginActivity,
                            "Sign-in cancelled.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: GetCredentialException) {
                    // Other credential-specific issues
                    Log.d("BRB", "Credential error: $e")
                    runOnUiThread {
                        Toast.makeText(
                            this@GoogleLoginActivity,
                            "Couldn’t get Google credentials. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.d("BRB", "Error in log In $e")
                    runOnUiThread {
                        Toast.makeText(
                            this@GoogleLoginActivity,
                            "Please try after some time",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        if(auth.currentUser != null){
            navigateToVeggiesListing()
        }

    }

    private suspend fun buildCredentialRequest() : GetCredentialResponse {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("341645535094-bhqt745glrfop7gcc023t9hq8port9kh.apps.googleusercontent.com")
                    .build()
            )
            .build()
        return credentialManager.getCredential(
            this@GoogleLoginActivity,request
        )
    }
    private suspend fun handelGoogleSignIn(result : GetCredentialResponse) : Boolean{
        val credential = result.credential
        if(credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL){
            try {
                val tokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val authCredential = GoogleAuthProvider.getCredential(tokenCredential.idToken,null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user
                if(user != null){
                    onFirebaseUserSignedIn(user)
                }
                return user != null
            } catch (e : Exception){
                Log.d("BRB", "Exception in logging in the google user $e")
                return false
            }
        } else{
            return false
        }
    }

    private fun openAddGoogleAccountScreen() {
        // Opens Settings → Add account with Google filtered
        val intent = Intent(Settings.ACTION_ADD_ACCOUNT).apply {
            putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
        }
        startActivity(intent)
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