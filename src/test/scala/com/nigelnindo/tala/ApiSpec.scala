package com.nigelnindo.tala

import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import com.nigelnindo.tala.api.model.Protocols._
import com.nigelnindo.tala.api.Routes
import com.nigelnindo.tala.api.model.{ErrorsResponse, TransactionRequest, ErrorResponse, BalanceResponse}
import com.nigelnindo.tala.db.AccountTable

import org.scalatest.{BeforeAndAfterAll, Matchers, FlatSpec}

import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcBackend.Database
import slick.lifted.TableQuery

/**
  * Created by nigelnindo on 9/20/17.
  */
class ApiSpec extends FlatSpec with Matchers
  with ScalatestRouteTest with Routes with BeforeAndAfterAll {

  implicit lazy val db = Database.forConfig("testDbApi")

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

  "API" should "return a balance of zero before processing any transactions[deposits]" in {
    Get("/balance") ~> routes ~> check {
      status should be (OK)
      responseAs[BalanceResponse] should be (BalanceResponse(0))
    }
  }

  it should "not allow any withdrawals before processing any transaction[deposits]" in {
    Post("/withdraw", TransactionRequest(3000)) ~> routes ~> check {
      status should be (BadRequest)
      responseAs[ErrorsResponse] should be (ErrorsResponse(List(ErrorResponse("You have insufficient funds to process withdraw."))))
    }
  }

  it should "not allow deposits larger than the maximum deposit per transaction and return appropriate error responses" in {
    Post("/deposit",TransactionRequest(60000)) ~> routes ~> check {
      status should be (BadRequest)
      responseAs[ErrorsResponse] should be (ErrorsResponse(List(
        ErrorResponse("A single deposit cannot exceed $40,000.00 .")
      )))
    }
  }

  it should "return multiple error messages when the deposit value exceeds per transaction and daily limit thresholds" in {
    Post("/deposit", TransactionRequest(150001)) ~> routes ~> check {
      status should be (BadRequest)
      responseAs[ErrorsResponse] should be (ErrorsResponse(List(
        ErrorResponse("A single deposit cannot exceed $40,000.00 ."),
        ErrorResponse("You cannot deposit more than $150,000.00 in one day."))
      ))
    }
  }

  it should "not accept negative or zero amounts for withdrawals & deposits" in {
    Post("/deposit", TransactionRequest(0)) ~> routes ~> check {
      status should be (BadRequest)
      responseAs[ErrorsResponse] should be (ErrorsResponse(List(
        ErrorResponse("Transaction amount must be greater than $0.00 .")
      )))
    }

    Post("/withdraw", TransactionRequest(-200)) ~> routes ~> check {
      status should be (BadRequest)
      responseAs[ErrorsResponse] should be (ErrorsResponse(List(
        ErrorResponse("Transaction amount must be greater than $0.00 .")
      )))
    }
  }

  it should "process a valid deposit and reflect new balance" in {
    Post("/deposit", TransactionRequest(40000)) ~> routes ~> check {
      status should be (Created)
    }
    Get("/balance") ~> routes ~> check {
      responseAs[BalanceResponse] should be (BalanceResponse(40000))
    }
  }

  it should "not allow withdrawals greater than the current balance" in {
    Post("/withdraw", TransactionRequest(50001)) ~> routes ~> check {
      status should be (BadRequest)
      responseAs[ErrorsResponse] should be (ErrorsResponse(List(
        ErrorResponse("You have insufficient funds to process withdraw."),
        ErrorResponse("A single withdraw cannot exceed $20,000.00 ."),
        ErrorResponse("You cannot withdraw more than $50,000.00 in one day.")
      )))
    }
  }

  it should "process a valid withdrawal and update the balance" in {
    Post("/withdraw", TransactionRequest(20000)) ~> routes ~> check {
      status should be (Created)
    }
    Get("/balance") ~> routes ~> check {
      status should be (OK)
      responseAs[BalanceResponse] should be (BalanceResponse(20000))
    }

  }

  it should "enforce daily deposit limit" in {
    /**
      * Make 3 more small deposits. The next deposit after these three should fail.
      */
    Post("/deposit", TransactionRequest(1000)) ~> routes ~> check {
      status should be (Created)
    }
    Post("/deposit", TransactionRequest(1000)) ~> routes ~> check {
      status should be (Created)
    }
    Post("/deposit", TransactionRequest(1000)) ~> routes ~> check {
      status should be (Created)
    }
    Post("/deposit", TransactionRequest(1000)) ~> routes ~> check {
      status should be (BadRequest)
      responseAs[ErrorsResponse] should be (ErrorsResponse(List(
        ErrorResponse("Daily number of deposits cannot exceed 4. You have reached this limit.")
      )))
    }
  }

  it should "enforce daily withdrawal limit" in {
    /**
      * Make 2 more small withdrawals. The next withdrawals after the two should fail.
      */
    Post("/withdraw", TransactionRequest(1000)) ~> routes ~> check {
      status should be (Created)
    }
    Post("/withdraw", TransactionRequest(1000)) ~> routes ~> check {
      status should be (Created)
    }
    Post("/withdraw", TransactionRequest(1000)) ~> routes ~> check {
      status should be (BadRequest)
      ErrorResponse("Daily number of withdraws cannot exceed 3. You have reached this limit.")
    }

  }

}
