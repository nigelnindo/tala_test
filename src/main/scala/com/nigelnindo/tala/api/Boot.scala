package com.nigelnindo.tala.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import slick.jdbc.JdbcBackend.Database

import com.nigelnindo.tala.utils.Config

import scala.concurrent.ExecutionContextExecutor

/**
  * Created by nigelnindo on 9/20/17.
  */
object Boot extends App with Routes {

  override implicit val system: ActorSystem = ActorSystem()
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override implicit val executor: ExecutionContextExecutor = system.dispatcher

  /**
    * Use "production" settings for database.
    * Must be defined as lazy val or with you'll end up wit a null pointer exception.
    */
  lazy val db = Database.forConfig("productionDb")

  Http().bindAndHandle(routes, Config.server.getString("interface"), Config.server.getInt("port"))

}
