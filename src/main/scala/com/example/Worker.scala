package com.example

import java.time.Instant

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
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
        queue ! FetchNext(Instant.now, demand)

    case FetchNextResult(events) =>
      val actor = self
      processing = processing + events.length
      events.foreach { e => Future(e.action()).onComplete(_ => actor ! ItemCompleted) }

    case ItemCompleted =>
      processing = processing - 1
      queue ! FetchNext(Instant.now, 1)
  }
}

object Worker {
  def props(queue: ActorRef) = Props(new Worker(queue))

  case object Tick
  case object ItemCompleted
}