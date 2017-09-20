package com.nigelnindo.tala

import com.nigelnindo.tala.db.{AccountTable, Transaction, TransactionsDAO}
import com.nigelnindo.tala.utils.DateUtils
import com.nigelnindo.tala.utils.Constants.TransactionTypes

import org.scalatest.{AsyncFlatSpec, BeforeAndAfterAll, Matchers, FlatSpec}

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.H2Profile.api._

import com.github.nscala_time.time.Imports._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationLong


/**
  * Created by nigelnindo on 9/20/17.
  */
class DaoSpec extends FlatSpec with Matchers with BeforeAndAfterAll{

 implicit lazy val db = Database.forConfig("testDb")

 val transactionsDAO = new TransactionsDAO

 override def beforeAll: Unit = {
  /**
    * Ensure that we are working with an empty database
    */
  val transactions = TableQuery[AccountTable]
  db.run(transactions.delete)

 }

 override def afterAll: Unit = {
  /**
    * Clear all transactions in test db and close the database.
    */
   val transactions = TableQuery[AccountTable]
   db.run(transactions.delete)

   db.close()

 }

 "The Account transactions DAO" should "Add initial test data" in {

  println("Inserting initial test data")

  val previousDayTransaction1 = transactionsDAO.insert(Transaction("ref1", TransactionTypes.DEPOSIT,
   DateUtils.dateTimeToTimestamp(DateTime.yesterday + 2.hours), 3000))
  val previousDayTransaction2 = transactionsDAO.insert(Transaction("ref2", TransactionTypes.DEPOSIT,
   DateUtils.dateTimeToTimestamp(DateTime.yesterday + 2.hours), 2000))
  val previousDayTransaction3 = transactionsDAO.insert(Transaction("ref3", TransactionTypes.WITHDRAWAL,
   DateUtils.dateTimeToTimestamp(DateTime.yesterday + 2.hours), 1000))

  val parallelFutures = for {
   t1 <- previousDayTransaction1
   t2 <- previousDayTransaction2
   t3 <- previousDayTransaction3
  } yield (t1,t2,t3)

  val blockingFutureResult = Await.result(parallelFutures, scala.concurrent.duration.Duration(2,"s"))

  assert(blockingFutureResult == (1,1,1))

 }

 it should "Add new payments" in {

  println("Adding new payments")

  val futureInsert = transactionsDAO.insert(Transaction("ref4", TransactionTypes.DEPOSIT,
   DateUtils.dateTimeToTimestamp(DateTime.now), 3000))

  val blockingFutureResult = Await.result(futureInsert, scala.concurrent.duration.Duration(2,"s"))

  assert(blockingFutureResult == 1)
 }

 it should "correctly return transactions related to particular day" in {

  val yesterdaysTransactions = transactionsDAO.getTransactionsByDate(DateTime.yesterday)

  // We created 3 transaction for the previous day
  val blockingFutureResult = Await.result(yesterdaysTransactions, scala.concurrent.duration.Duration(2,"s"))
  assert(blockingFutureResult.size == 3)

  val transactionsToday = transactionsDAO.getTransactionsByDate(DateTime.now)

  // We have only created 1 transaction for today. Transactions size should be equal 1.
  val blockingFutureResult2 = Await.result(transactionsToday, scala.concurrent.duration.Duration(2,"s"))
  assert(blockingFutureResult2.size == 1)

 }

}
