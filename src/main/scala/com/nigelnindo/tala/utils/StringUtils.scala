package com.nigelnindo.tala.utils

/**
  * Created by nigelnindo on 9/21/17.
  */
object StringUtils {

  private val currencyFormatter = java.text.NumberFormat.getCurrencyInstance

  def formatCurrency(amount: Double): String = currencyFormatter.format(amount)

}
