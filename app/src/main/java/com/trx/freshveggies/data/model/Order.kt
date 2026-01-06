package com.trx.freshveggies.data.model

data class Order(
    val id: String = "",
    val uid: String = "",
    val items: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val paymentRefId: String = "",
    val timeStamp: Long = 0L,
    val address: Address? = null,
    val phoneNumber: String = "",
    val status: String = "Pending"
)
