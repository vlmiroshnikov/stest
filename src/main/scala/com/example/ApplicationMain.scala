package com.example

import java.time.Instant

import akka.actor.{ActorSystem, Props}

import scala.concurrent.Await
import scala.concurrent.duration._

object ApplicationMain extends App {

  implicit val system = ActorSystem("ActorSystem")
  implicit val ec = system.dispatcher


  val queue = system.actorOf(Props[ConsumerActor], "queue")
  val worker = system.actorOf(Props(new Worker(queue)))

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






