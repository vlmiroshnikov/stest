package com.example

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.example.Worker.{ItemCompleted, Tick}

import scala.concurrent.Future
import scala.concurrent.duration._

class Worker(queue: ActorRef) extends Actor with ActorLogging {

  implicit val ec = context.dispatcher

  context.system.scheduler.schedule(1.second, 1.second, self, Tick)

  val maxWorkItems = 10
  var processing = 0

  def receive: Receive = {
    case Tick =>
      val demand = maxWorkItems - processing
      if (demand > 0)
        queue ! FetchNext(demand)

    case FetchNextResult(events) =>
      val actor = self
      processing = processing + events.length
      events.foreach { e => Future(e.action()).onComplete(_ => actor ! ItemCompleted) }

    case ItemCompleted =>
      processing = processing - 1
      queue ! FetchNext(1)
  }
}

object Worker {
  case object Tick
  case object ItemCompleted
}