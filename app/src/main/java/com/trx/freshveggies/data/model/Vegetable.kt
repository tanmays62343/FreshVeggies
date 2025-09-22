package com.trx.freshveggies.data.model

import androidx.annotation.DrawableRes

data class Vegetable(
    val id: Int,
    val name: String,
    val price: Double,
    @DrawableRes val imageRes: Int
)
