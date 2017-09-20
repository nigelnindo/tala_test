package com.nigelnindo.tala.api.validations

import com.nigelnindo.tala.db.Transaction
import com.nigelnindo.tala.utils.Constants.TransactionType

/**
  * Created by nigelnindo on 9/20/17.
  */

case class ValidationData(transactionType: TransactionType, amount: Double, transactionsForDay: List[Transaction], accountBalance: Option[Double])

/**
  * We create different validators that extend the `Validator` trait.
  * They have to give a reason for the failed Validation, and implement
  * a validate function.
  * Validations that pass return a true.
  */

//filter validations and only remain with the failing ones

/**
  * To be used for both deposits and withdrawals
  */
trait Validator[+A] {
  def reason: String
  def validate(): Boolean
}

trait TransactionValidator{
  val validations: List[Validator[Any]]

  /**
    *
    * @return a list of messages containing the reason why some validations failed. An empty
    *         list means that all of the validations passed.
    */
  def runValidations(): List[String] = {
    val validationsToOption: List[Option[Validator[Any]]] = validations.map( validation => {
      if (validation.validate()) None else Some(validation)
    })

    /**
      * Validations that passed have a `None` and those that failed have a Some(Validator[Any])
      * We can now flatten this list to remain with only failed validation
      */

    val failedValidations: List[Validator[Any]] = validationsToOption.flatten

    /**
      * Return the reason as to why the Validations failed so that they can be relayed via th API
      */

    failedValidations.map( validation => validation.reason)

  }
}

case class DepositTransactionValidator(implicit validationData: ValidationData ) extends TransactionValidator {
  override val validations = List(new NegativeAmounts, new MaxSingleTransactionValue(40000),
    new MaxDailyTotalTransactionValue(150000), new MaxDailyNumberOfTransactions(4))
}

case class WithdrawalTransactionValidator(implicit validationData: ValidationData ) extends TransactionValidator {
  override val validations = List(new OverdraftGuard,new NegativeAmounts, new MaxSingleTransactionValue(20000),
    new MaxDailyTotalTransactionValue(50000), new MaxDailyNumberOfTransactions(3))
}

/**
  * Validators are defined below
  */

class OverdraftGuard(implicit validationData: ValidationData ) extends Validator[OverdraftGuard]{
  override def reason: String = s"You have insufficient funds to process withdrawal."
  // adding `=` to `<=` allows the user to withdraw all their money
  override def validate(): Boolean = validationData.amount <= validationData.accountBalance.get
}

class NegativeAmounts(implicit validationData: ValidationData ) extends Validator[NegativeAmounts]{
  override def reason: String = "Transaction amount must be greater than 0"
  override def validate(): Boolean = validationData.amount > 0
}

class MaxSingleTransactionValue(maxAllowed: Double)
                               (implicit validationData: ValidationData ) extends Validator[MaxSingleTransactionValue] {
  override def reason: String = s"A single transaction cannot be more that $maxAllowed"
  override def validate(): Boolean = validationData.amount <= maxAllowed
}

class MaxDailyTotalTransactionValue(maxAllowed: Double)
                                   (implicit validationData: ValidationData ) extends Validator[MaxDailyTotalTransactionValue] {
  override def reason: String = s"Daily transaction value cannot be greater than $maxAllowed"
  override def validate(): Boolean =
    // factor in transaction being currently processed too
    validationData.transactionsForDay.foldLeft(0.0)((accu, t) => accu + t.amount) + validationData.amount <= maxAllowed
}

class MaxDailyNumberOfTransactions(maxAllowed: Int)
                                  (implicit validationData: ValidationData ) extends Validator[MaxDailyNumberOfTransactions] {
  override def reason: String = s"Daily number of transactions cannot exceed $maxAllowed"
  override def validate(): Boolean =
    // + 1 to once again factor transaction being currently processed
    validationData.transactionsForDay.size + 1 <= maxAllowed
}