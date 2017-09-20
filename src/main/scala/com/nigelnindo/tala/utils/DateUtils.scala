package com.nigelnindo.tala.utils

import java.sql.Timestamp

import com.github.nscala_time.time.Imports._

/**
  * Created by nigelnindo on 9/20/17.
  */
object DateUtils {

  def isToday(dateTime: DateTime) = LocalDate.now().compareTo(new LocalDate(dateTime)) == 0

  def dateTimeToTimestamp(dateTime: DateTime): Timestamp = new Timestamp(dateTime.getMillis)

  def timestampToDateTime(timestamp: Timestamp): DateTime = new DateTime(timestamp.getTime)

  def isDateTimesOnSameDay(dateTime1: DateTime, dateTime2: DateTime) = new LocalDate(dateTime1).compareTo(new LocalDate(dateTime2)) == 0

}
