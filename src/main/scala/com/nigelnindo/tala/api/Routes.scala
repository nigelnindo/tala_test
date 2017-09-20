package com.nigelnindo.tala.api

import akka.actor.ActorSystem
import akka.stream.Materializer

import akka.http.scaladsl.server.Directives._

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
        complete("You have reached the balance endpoint")
      }
    } ~
    pathPrefix("deposit"){
      post {
        complete("You have reached the deposit endpoint")
      }
    } ~
    pathPrefix("withdraw"){
      post {
        complete("You have reached the withdraw endpoint")
      }
    }
  }
}
