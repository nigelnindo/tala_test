package com.nigelnindo.tala.utils

/**
  * Created by nigelnindo on 9/20/17.
  */
object Constants {

  type TransactionType = String

  object TransactionTypes {
    val DEPOSIT: TransactionType = "deposit"
    val WITHDRAWAL: TransactionType = "withdrawal"
  }

}
