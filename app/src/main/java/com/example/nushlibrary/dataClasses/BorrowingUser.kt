package com.example.nushlibrary.dataClasses

data class BorrowingUser(
    val userId: String = "",
    val bookId: String = "",
    val displayName: String = "",
    val bookTitle: String = ""
) {
}