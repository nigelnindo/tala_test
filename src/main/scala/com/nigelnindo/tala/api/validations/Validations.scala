package com.nigelnindo.tala.api.validations

import com.nigelnindo.tala.utils.StringUtils

/**
  * We create different validators that extend the `Validator` trait.
  * They have to give a reason for the failed Validation, and implement
  * a validate function.
  * Validations that pass the validation test return a true.
  */

trait Validation[+A] {
  def reason: String
  def validate(): Boolean
}

/**
  * Validators are defined below
  */

class OverdraftGuard(implicit validationData: ValidationData ) extends Validation[OverdraftGuard]{
  override def reason: String = s"You have insufficient funds to process ${validationData.transactionType}."
  // adding `=` to `<=` allows the user to withdraw all their money
  override def validate(): Boolean = validationData.amount <= validationData.accountBalance.get
}

class NegativeAmounts(implicit validationData: ValidationData ) extends Validation[NegativeAmounts]{
  override def reason: String = s"Transaction amount must be greater than ${StringUtils.formatCurrency(0)} ."
  override def validate(): Boolean = validationData.amount > 0
}

class MaxSingleTransactionValue(maxAllowed: Double)
                               (implicit validationData: ValidationData ) extends Validation[MaxSingleTransactionValue] {
  override def reason: String = s"A single ${validationData.transactionType} cannot exceed ${StringUtils.formatCurrency(maxAllowed)} ."
  override def validate(): Boolean = validationData.amount <= maxAllowed
}

class MaxDailyTotalTransactionValue(maxAllowed: Double)
                                   (implicit validationData: ValidationData ) extends Validation[MaxDailyTotalTransactionValue] {
  override def reason: String = s"You cannot ${validationData.transactionType} more than ${StringUtils.formatCurrency(maxAllowed)} in one day."
  override def validate(): Boolean =
    // factor in transaction being currently processed too
    validationData.transactionsForDay.foldLeft(0.0)((accu, t) => accu + t.amount) + validationData.amount <= maxAllowed
}

class MaxDailyNumberOfTransactions(maxAllowed: Int)
                                  (implicit validationData: ValidationData ) extends Validation[MaxDailyNumberOfTransactions] {
  override def reason: String = s"Daily number of ${validationData.transactionType}s cannot exceed $maxAllowed. You have reached this limit."
  override def validate(): Boolean =
    // + 1 to once again factor transaction being currently processed
    validationData.transactionsForDay.size + 1 <= maxAllowed
}