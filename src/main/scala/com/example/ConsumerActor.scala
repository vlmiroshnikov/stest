package com.example

import akka.actor.{Actor, ActorLogging, Props}
import akka.event.LoggingReceive
import com.example.ConsumerActor._

import scala.collection.mutable


class ConsumerActor extends Actor with ActorLogging {

  val queueReverseOrdering = new Ordering[StoredEvent] {
    def compare(x: StoredEvent, y: StoredEvent): Int =
      y.origin.when.compareTo(x.origin.when)
  }

  val queue = mutable.PriorityQueue.empty[StoredEvent](queueReverseOrdering)

  implicit val eventOrdering = new Ordering[StoredEvent] {
    override def compare(x: StoredEvent, y: StoredEvent): Int = {
      x.origin.when.compareTo(y.origin.when) match  {
        case 0 => x.storedOrder.compareTo(y.storedOrder)
        case other => other
      }
    }
  }

  var order = 0

  def receive = LoggingReceive {
    case Put(e: Event) =>
      order = order + 1
      queue += StoredEvent(e, order)

    case FetchNext(when, top) =>
      val result = queue.orderedIter
        .filter(_.origin.when.isBefore(when))
        .take(top)
        .toList
        .sorted
      sender ! FetchNextResult(result.map(_.origin))

    case TotalEvents =>
      sender ! TotalEventsResult(queue.length)
  }
}


object ConsumerActor {
  val props = Props[ConsumerActor]

  implicit class FilterOps(queue: mutable.PriorityQueue[StoredEvent]) {
    def orderedIter = new Iterable[StoredEvent] {
      def iterator = new Iterator[StoredEvent] {
        def hasNext: Boolean = queue.nonEmpty
        def next() = queue.dequeue()
      }
    }
  }

  type Action = () => Unit
}

case class StoredEvent(origin: Event, storedOrder: Int)




