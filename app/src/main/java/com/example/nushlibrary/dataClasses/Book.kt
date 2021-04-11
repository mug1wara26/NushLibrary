package com.example.nushlibrary.dataClasses

data class Book(
    val id: String = "",
    val authors: ArrayList<*> = arrayListOf<String>(),
    val title: String = "",
    val description: String? = null,
    val publisher: String? = null,
    val genre: ArrayList<*> = arrayListOf<String>(),
    val thumbnail: String? = null,
    var number: Int = 0,
    var borrowedUsers: ArrayList<BorrowedUser> = arrayListOf()
)