package com.nigelnindo.tala.api.validations

import com.nigelnindo.tala.db.Transaction
import com.nigelnindo.tala.utils.Constants.TransactionType

/**
  * Created by nigelnindo on 9/20/17.
  *
  * This case class encapsulates the data that will help make validations and return
  * appropriate error messages.
  */

case class ValidationData(transactionType: TransactionType, amount: Double, transactionsForDay: List[Transaction], accountBalance: Option[Double])
