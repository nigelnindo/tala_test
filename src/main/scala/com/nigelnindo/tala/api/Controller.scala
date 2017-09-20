package com.nigelnindo.tala.api

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.nigelnindo.tala.api.model.{ErrorsResponse, ErrorResponse, BalanceResponse}

import com.nigelnindo.tala.api.model.Protocols._
import com.github.nscala_time.time.Imports._
import com.nigelnindo.tala.api.validations.{DepositTransactionValidator, WithdrawalTransactionValidator, ValidationData}
import com.nigelnindo.tala.utils.{UUID, DateUtils, Constants}
import com.nigelnindo.tala.utils.Constants.TransactionTypes

import slick.jdbc.JdbcBackend

import com.nigelnindo.tala.db.{Transaction, TransactionsDAO}

import scala.concurrent.ExecutionContext
import scala.util.Failure

/**
  * Created by nigelnindo on 9/20/17.
  */
trait Controller {

  implicit val system: ActorSystem
  implicit val materializer: Materializer
  implicit def ec: ExecutionContext = system.dispatcher

  implicit val db: JdbcBackend.Database

  val transactionsDAO = new TransactionsDAO

  def computeBalanceFromTransactions(transactions: Seq[Transaction]): Double = {
    transactions.foldLeft(0.0)((x: Double, y: Transaction) => {
      if (y.transactionType == Constants.TransactionTypes.DEPOSIT){
        x + y.amount
      } else {
        // withdrawal
        x - y.amount
      }
    })
  }

  object getBalance {
    /**
      * Get all the transactions from the database (in order). Then replay all events to get the
      * account balance.
      */
    def apply(): Route = {
      onComplete{
        transactionsDAO.getAllTransactions
      } {
        case scala.util.Success(transactions) =>
          complete(ToResponseMarshallable(OK, BalanceResponse(computeBalanceFromTransactions(transactions))))
        case Failure(ex) =>
          complete(ToResponseMarshallable((InternalServerError, ErrorResponse(s"An error occurred while getting account balance: ${ex.getMessage}") )))
      }
    }
  }

  object processDeposit {
    // TODO: handle negative amounts and maximum thresholds (amount & daily transaction limit)
    def apply(amount: Double): Route = {
      onComplete{
        transactionsDAO.getTransactionsByDate(DateTime.now)
      } {
        case scala.util.Success(transactionsToday) =>

          val deposit = Transaction(UUID.generate, TransactionTypes.DEPOSIT,
            DateUtils.dateTimeToTimestamp(DateTime.now()), amount)

          val depositsToday = transactionsToday.filter( t => t.transactionType == TransactionTypes.DEPOSIT)
          implicit val validationData: ValidationData = ValidationData(TransactionTypes.DEPOSIT, amount, depositsToday.toList, None)

          val failedValidationReasons = DepositTransactionValidator().runValidations()

          if (failedValidationReasons.nonEmpty){

            complete(ToResponseMarshallable((BadRequest,
              ErrorsResponse(failedValidationReasons.map( r => ErrorResponse(r))) )))

          } else {
            onComplete{
              transactionsDAO.insert(deposit)
            } {
              case scala.util.Success(numInserted) =>
                complete(ToResponseMarshallable(Created, deposit))
              case Failure(ex) =>
                complete(ToResponseMarshallable(InternalServerError,
                  ErrorResponse(s"An error occurred while processing deposit: ${ex.getMessage}")))
            }
          }

        case Failure(ex) =>
          complete(ToResponseMarshallable((InternalServerError,
            ErrorResponse(s"An error occurred while processing deposit: ${ex.getMessage}") )))
      }
    }
  }

  object processWithdrawal {
    /**
      * Before we can process any withdrawals, we need to ensure that the user has
      * enough balance left to complete the request.
      */
    def apply(amount: Double): Route = {
      onComplete{
        transactionsDAO.getAllTransactions
      } {
        case scala.util.Success(transactions) =>

          val balance: Double = computeBalanceFromTransactions(transactions)
          val withdrawalsToday = transactions
            .filter(t => DateUtils.isDateTimesOnSameDay(DateUtils.timestampToDateTime(t.timestamp),DateTime.now()))
            .filter(t => t.transactionType == TransactionTypes.WITHDRAWAL)
          implicit val validationData: ValidationData = ValidationData(TransactionTypes.WITHDRAWAL, amount, withdrawalsToday.toList, Some(balance))

          val failedValidationReasons = WithdrawalTransactionValidator().runValidations()

          if (failedValidationReasons.nonEmpty){

            complete(ToResponseMarshallable((BadRequest,
              ErrorsResponse(failedValidationReasons.map( r => ErrorResponse(r))) )))

          } else{

            // all validations passed, process withdrawal

            val withdrawal = Transaction(UUID.generate, TransactionTypes.WITHDRAWAL,
              DateUtils.dateTimeToTimestamp(DateTime.now()), amount)
            onComplete{
              transactionsDAO.insert(withdrawal)
            } {
              case scala.util.Success(numInserted) =>
                complete(ToResponseMarshallable(Created, withdrawal))
              case Failure(ex) =>
                complete(ToResponseMarshallable((InternalServerError,
                  ErrorResponse(s"An error occurred while processing withdrawal: ${ex.getMessage}"))))
            }

          }
        case Failure(ex) =>
          complete(ToResponseMarshallable((InternalServerError,
            ErrorResponse(s"An error occurred while getting account balance: ${ex.getMessage}"))))
      }
    }
  }

}
