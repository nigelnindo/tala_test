package com.nigelnindo.tala.db

import slick.jdbc.JdbcBackend

import scala.concurrent.Future

/**
  * Created by nigelnindo on 9/20/17.
  */

/**
  * Convenience trait to outline common behavior that Data Access Objects must implement/inherit.
  */

trait DAO {

  implicit val db: JdbcBackend.Database

}
