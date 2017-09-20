package com.nigelnindo.tala.db

import java.sql.Timestamp

import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcBackend
import slick.lifted.ProvenShape

import com.github.nscala_time.time.Imports._

import com.nigelnindo.tala.utils.{Constants,DateUtils}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global // execution context to run queries


/**
  * Created by nigelnindo on 9/20/17.
  */

case class Transaction(refNo: String, transactionType: Constants.TransactionType,
                        timestamp: Timestamp, amount: Double )

/**
  *
  * `AccountTable` defines the schema that we will use to store transactions.
  */

class AccountTable(tag: Tag)extends Table[Transaction](tag,"account"){

  def refNo = column[String]("refNo", O.PrimaryKey)
  def transactionType = column[String]("transactionType")
  def timestamp = column[Timestamp]("timestamp")
  def amount = column[Double]("amount")

  override def * : ProvenShape[Transaction] = (refNo,transactionType,timestamp,amount) <> (Transaction.tupled, Transaction.unapply)

}

/**
  *
  * @param db implicitly passed from a calling scope, since we need database definition as
  *           required by our data access object.
  */

class TransactionsDAO(implicit val db: JdbcBackend.Database) extends DAO {

  val transactions = TableQuery[AccountTable]

  // create the table
  val setup = db.run(transactions.schema.create)

  /**
    *
    * We treat the database as a log and only allow inserts.
    *
    * Let the DAO only be responsible for getting and inserting transaction data, no
    * business logic. That will be performed at the controller level.
    */

  def insert(transaction: Transaction) = db.run(transactions += transaction)

  /**
    *
    * @param transactions takes in a `Query` from the account table
    * @return a `Query` from the account table
    */
  // We need to enforce ordering of transactions from the earliest to the latest
  def orderTransactions(transactions: Query[AccountTable, Transaction, Seq]) = {
    transactions.sortBy(_.timestamp.asc)
  }

  // Get all transaction and replay them. This is how we compute the current balance.
  def getAllTransactions = db.run(orderTransactions(transactions).result)

  /**
    * We will also need to get transactions that have occurred for a particular date
    * in order to enforce transaction limits. We will filter out deposits & withdrawals
    * at the controller level.
    *
    * @param dateTime the date to get all transactions for.
    */

  def getTransactionsByDate(dateTime: DateTime): Future[Seq[Transaction]] = {

    db.run(orderTransactions(transactions).result).map{ orderedTransactions =>
      orderedTransactions.filter(t => DateUtils.isDateTimesOnSameDay(DateUtils.timestampToDateTime(t.timestamp), dateTime))
    }

  }

  /**
    * This function takes a range of timestamps and returns all the transactions that
    * have occurred between them.
    */

  def getTransactionsByRange(start: Timestamp, end: Timestamp): Future[Seq[Transaction]] = {
    val transactionsWithinRange = for {
      t <- transactions
      if t.timestamp >= start && t.timestamp <= end // filter within timestamp range
    } yield t

    db.run(orderTransactions(transactionsWithinRange).result)
  }

}
