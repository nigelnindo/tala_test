package com.nigelnindo.tala.api

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.nigelnindo.tala.db.{TransactionsDAO}
import slick.jdbc.JdbcBackend

import scala.concurrent.ExecutionContext

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

  }

  object processDeposit {

  }

  object processWithdrawal {

  }

}
