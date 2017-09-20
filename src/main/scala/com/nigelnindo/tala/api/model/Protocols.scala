package com.nigelnindo.tala.api.model

import java.sql.Timestamp

import com.nigelnindo.tala.utils.DateUtils
import spray.json._

import com.nigelnindo.tala.db.Transaction


/**
  * Created by nigelnindo on 9/20/17.
  */
object Protocols extends DefaultJsonProtocol {

  /**
    * `java.sql.TimeStamp` cannot be serialized/deserialized directly by Spray JSON. So, we
    * define a custom serializer.
    */
  implicit object SqlTimestampFormat extends RootJsonFormat[Timestamp] {
    override def write(value: Timestamp): JsValue = JsNumber(value.getTime)
    override def read(json: JsValue): Timestamp = json match {
      case JsNumber(value) => new Timestamp(value.toLong)
      case _ => throw new DeserializationException("Expected timestamp as a number")
    }
  }

  implicit val transactionRequestFormat = jsonFormat1(TransactionRequest)
  implicit val transactionResponseFormat = jsonFormat4(Transaction)
  implicit val balanceResponseFormat = jsonFormat1(BalanceResponse)
  implicit val errorResponseFormat = jsonFormat1(ErrorResponse)
  implicit val errorsResponseFormat = jsonFormat1(ErrorsResponse)

}

case class TransactionRequest(amount: Double)
case class BalanceResponse(balance: Double)
case class ErrorResponse(errorMessage: String)
case class ErrorsResponse(errors: List[ErrorResponse])
