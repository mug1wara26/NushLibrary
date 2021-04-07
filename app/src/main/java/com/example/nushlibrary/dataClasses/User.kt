package com.example.nushlibrary.dataClasses

data class User(
    val id: String = "",
    val email: String = "",
    var displayName: String = "",
    val booksBorrowedTimeStamp: ArrayList<Long> = arrayListOf(),
    val booksBorrowed: ArrayList<String> = arrayListOf(),
    val toReadList: ArrayList<String> = arrayListOf(),
    val admin: Boolean = false) {
}