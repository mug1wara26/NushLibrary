package com.example.nushlibrary

data class Book(
    val id: String,
    val authors: ArrayList<*>,
    val title: String?,
    val description: String?,
    val publisher: String?,
    val genre: ArrayList<*>,
    val thumbnail: String?,
    var number: Int
)