package com.nigelnindo.tala.db

import java.sql.Timestamp

import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcBackend._
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global

import com.nigelnindo.tala.utils.Constants
import slick.jdbc.JdbcBackend
import slick.lifted.ProvenShape

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

  val dbs = Database.forConfig("productionDb")

  val transactions = TableQuery[AccountTable]

  // create the table
  val setup = dbs.run(transactions.schema.create)

  // We treat the database as a log and only allow inserts.

  def insert(transaction: Transaction) = ???

  // Get all transaction and replay them. This is how we compute the current balance.

  def getAllTransactions() = db.run(transactions.result)

  /**
    * We will also need to get transactions that have occurred for a particular date
    * in order to enforce transaction limits. We will filter out deposits & withdrawals
    * at the controller level
    */

  def getTransactionsByDate() = ???

}
