package com.nigelnindo.tala.utils

import com.typesafe.config.ConfigFactory

/**
  * Created by nigelnindo on 9/20/17.
  */
object Config {
  val factory = ConfigFactory.load()
  val server = factory.getConfig("server")
}
