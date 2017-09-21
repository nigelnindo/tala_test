package com.nigelnindo.tala.api.validations

/**
  * `TransactionValidator` will be used by both deposits and withdrawals. They will extend
  * the trait and provide a list of validations they with to run.
  */
trait TransactionValidator{
  val validations: List[Validation[Any]]

  /**
    *
    * @return a list of messages containing the reason why some validations failed. An empty
    *         list means that all of the validations passed.
    */
  def runValidations(): List[String] = {
    val validationsToOption: List[Option[Validation[Any]]] = validations.map( validation => {
      if (validation.validate()) None else Some(validation)
    })

    /**
      * Validations that passed have a `None` and those that failed have a Some(Validator[Any])
      * We can now flatten this list to remain with only failed validation
      */

    val failedValidations: List[Validation[Any]] = validationsToOption.flatten

    /**
      * Return the reason as to why the Validations failed so that they can be relayed via th API
      */

    failedValidations.map( validation => validation.reason)

  }
}
