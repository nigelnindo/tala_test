package com.nigelnindo.tala

import com.nigelnindo.tala.utils.{Config, UUID, StringUtils}
import org.scalatest.{Matchers, FlatSpec}

/**
  * Created by nigelnindo on 9/21/17.
  */
class UnitSpec extends FlatSpec with Matchers {

  "StringUtils" should "format a double into a currency friendly string" in {
    assert(StringUtils.formatCurrency(40000) == "$40,000.00")
  }

  "UUID" should "be non-deterministic" in {
    val id1 = UUID.generate
    val id2 = UUID.generate
    assert(id1 != id2)
  }

  "Config" should "get interface(address) and port from TypeSafe config" in {
    assert(!Config.server.getString("interface").isEmpty)
    assert(Config.server.getInt("port").isInstanceOf[Int])
  }

}
