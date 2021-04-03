package com.example.nushlibrary

data class Book(
    val id: String = "",
    val authors: ArrayList<*> = arrayListOf<String>(),
    val title: String? = null,
    val description: String? = null,
    val publisher: String? = null,
    val genre: ArrayList<*> = arrayListOf<String>(),
    val thumbnail: String? = null,
    var number: Int = 0,
    var borrowedTime: Long? = null,
    val borrowedBy: ArrayList<String> = arrayListOf()
)