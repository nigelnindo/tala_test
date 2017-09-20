package com.nigelnindo.tala

import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import com.nigelnindo.tala.api.model.Protocols._
import com.nigelnindo.tala.api.Routes
import com.nigelnindo.tala.api.model.{TransactionRequest, ErrorResponse, BalanceResponse}
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

  implicit lazy val db = Database.forConfig("testDb")

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
      responseAs[ErrorResponse] should be (ErrorResponse("You don't have enough balance left available to to withdraw 3000.0. Current balance is 0.0."))
    }
  }

}
