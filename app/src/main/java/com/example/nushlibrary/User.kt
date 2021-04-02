package com.example.nushlibrary

data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val booksBorrowedTimeStamp: ArrayList<Long> = arrayListOf(),
    val booksBorrowed: ArrayList<String> = arrayListOf(),
    val toReadList: ArrayList<String> = arrayListOf(),
    val admin: Boolean = false) {
}