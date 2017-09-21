package com.nigelnindo.tala.api.validations

/**
  * Created by nigelnindo on 9/21/17.
  */
case class DepositTransactionValidator(implicit validationData: ValidationData ) extends TransactionValidator {
  override val validations = List(new NegativeAmounts, new MaxSingleTransactionValue(40000),
    new MaxDailyTotalTransactionValue(150000), new MaxDailyNumberOfTransactions(4))
}
