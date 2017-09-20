package com.nigelnindo.tala.api

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import akka.http.scaladsl.model.StatusCodes._

import com.github.nscala_time.time.Imports._

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

  object getBalance {
    /**
      * Get all the transactions from the database. Then replay all events to get the
      * account balance.
      */
    def apply(): Route = {
      onComplete{
        transactionsDAO.getAllTransactions
      } {
        case scala.util.Success(transactions) =>
          val balance = transactions.foldLeft(0.0)((x: Double, y: Transaction) => x + y.amount)
          complete((OK, balance.toString))
        case Failure(ex) =>
          complete((InternalServerError, s"An error occurred while getting account balance: ${ex.getMessage}"))
      }
    }
  }

  object processDeposit {
    onComplete{
      transactionsDAO.getTransactionsByDate(DateTime.now)
    } {
      case scala.util.Success(transactions) =>
        // filter transaction to so that you only remain with deposits

        complete((OK,"Deposit success"))
      case Failure(ex) =>
        complete((InternalServerError, s"An error occurred while processing deposit: ${ex.getMessage}"))
    }
  }

  object processWithdrawal {

  }

}
