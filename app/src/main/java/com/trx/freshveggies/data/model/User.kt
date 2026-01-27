package com.trx.freshveggies.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val addressList: List<Address> = emptyList(),
    val checkList: List<String> = emptyList(), // Placeholder for orderList or similar if needed, keeping it flexible
    val orderList: List<Order> = emptyList() // Assuming Order model exists or will be used
)

data class Address(
    val id: String = "",
    val phoneNumber: String = "",
    val fullName: String = "",
    val flatNo: String = "",
    val society: String = ""
)
