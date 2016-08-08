package com.example

import java.time.Instant

import scala.collection.immutable

sealed trait Request


case class Put(e: Event) extends Request

case class FetchNext(when: Instant, top: Int = 10) extends Request

case object TotalEvents extends Request

case class FetchNextResult(events: immutable.Seq[Event])

case class TotalEventsResult(count: Int)
