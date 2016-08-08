package com.example

import java.time.Instant

import akka.actor.ActorSystem

import scala.concurrent.Await
import scala.concurrent.duration._

object Entry extends App {

  implicit val system = ActorSystem("ActorSystem")
  implicit val ec = system.dispatcher


  val queue = system.actorOf(ConsumerActor.props, "queue")
  val worker = system.actorOf(Worker.props(queue))

  system.scheduler.schedule(0.seconds, 100.millisecond, new Runnable {
    def send(time: Instant) = {
      queue ! Put(Event(time, () => {
        Thread.sleep(1000)
        println(s"$time  completed")
      }))
    }

    def run() = {
      val time = Instant.now()
      send(time.plusSeconds(1))
      send(time.minusSeconds(1))
      send(time)
    }
  })

  Await.result(system.whenTerminated, Duration.Inf)
}






