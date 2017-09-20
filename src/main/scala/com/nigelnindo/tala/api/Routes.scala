package com.nigelnindo.tala.api

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import com.nigelnindo.tala.api.model.Protocols._
import com.nigelnindo.tala.api.model.TransactionRequest

import scala.concurrent.ExecutionContextExecutor

/**
  * Created by nigelnindo on 9/20/17.
  */
trait Routes extends Controller {

  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: Materializer

  val routes = {
    pathPrefix("balance"){
      get {
        getBalance()
      }
    } ~
    pathPrefix("deposit"){
      (post & entity(as[TransactionRequest])) { transactionRequest: TransactionRequest =>
        processDeposit(transactionRequest.amount)
      }
    } ~
    pathPrefix("withdraw"){
      (post & entity(as[TransactionRequest])) { transactionRequest: TransactionRequest =>
        processWithdrawal(transactionRequest.amount)
      }
    }
  }
}
