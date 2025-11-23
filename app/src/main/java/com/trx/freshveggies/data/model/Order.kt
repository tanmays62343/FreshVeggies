package com.trx.freshveggies.data.model

data class Order(
    val id : String,
    val items : List<CartItem>,
    val totalAmount : Double,
    val paymentRefId : String,
    val timeStamp : Long
)
