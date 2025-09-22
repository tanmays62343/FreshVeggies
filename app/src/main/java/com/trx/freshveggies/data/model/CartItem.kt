package com.trx.freshveggies.data.model

data class CartItem(
    val vegetable: Vegetable,
    var quantity: Int = 1
) {
    val lineTotal: Double
        get() = vegetable.price * quantity
}
