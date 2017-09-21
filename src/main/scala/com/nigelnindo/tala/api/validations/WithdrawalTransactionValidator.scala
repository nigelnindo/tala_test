package com.nigelnindo.tala.api.validations

/**
  * Created by nigelnindo on 9/21/17.
  */
case class WithdrawalTransactionValidator(implicit validationData: ValidationData ) extends TransactionValidator {
  override val validations = List(new OverdraftGuard,new NegativeAmounts, new MaxSingleTransactionValue(20000),
    new MaxDailyTotalTransactionValue(50000), new MaxDailyNumberOfTransactions(3))
}
