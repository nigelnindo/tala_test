package com.nigelnindo.tala.api

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.nigelnindo.tala.api.model.{ErrorResponse, BalanceResponse}

import com.nigelnindo.tala.api.model.Protocols._
import com.github.nscala_time.time.Imports._
import com.nigelnindo.tala.utils.{UUID, DateUtils, Constants}
import com.nigelnindo.tala.utils.Constants.TransactionTypes

import slick.jdbc.JdbcBackend

import com.nigelnindo.tala.db.{Transaction, TransactionsDAO}
//import com.nigelnindo.tala.api.model.Protocols._ // JSON marshalling

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
        case scala.util.Success(transactions) =>
          // filter transaction to so that you only remain with deposits

          val deposit = Transaction(UUID.generate, TransactionTypes.DEPOSIT,
            DateUtils.dateTimeToTimestamp(DateTime.now()), amount)

          onComplete{
            transactionsDAO.insert(deposit)
          } {
            case scala.util.Success(numInserted) =>
              complete(ToResponseMarshallable(Created, deposit))
            case Failure(ex) =>
              complete(ToResponseMarshallable(InternalServerError,
                ErrorResponse(s"An error occured while processing deposit: ${ex.getMessage}")))
          }

          complete((OK,"Deposit success"))
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
      // TODO: handle negative amounts and maximum thresholds (amount & daily transaction liit)
      onComplete{
        transactionsDAO.getAllTransactions
      } {
        case scala.util.Success(transactions) =>
          val balance: Double = computeBalanceFromTransactions(transactions)

          /**
            * If amount is greater than the current balance, we cannot fulfill the request.
            * Else, go on and process the transaction.
            */

          if (amount > balance){
            complete(ToResponseMarshallable((BadRequest,
              ErrorResponse(s"You don't have enough balance left available to to withdraw $amount. Current balance is $balance.") )))
          } else{
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
