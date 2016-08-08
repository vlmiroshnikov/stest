package com.example

import java.time.Instant

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.collection.immutable._

class ConsumerActorSpec extends TestKit(ActorSystem("TestActorSystem", ConfigFactory.defaultApplication())) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val emptyCallback = () => ()

  "Actor" should {
    "put single message" in {
      val consumer = system.actorOf(Props[ConsumerActor])
      consumer ! Put(Event(Instant.now(), emptyCallback))
      consumer ! TotalEvents
      expectMsg(TotalEventsResult(1))
    }

    "return events ordered by time and nature order" in {
      val consumer = system.actorOf(Props[ConsumerActor])

      val e1 = Event(Instant.now(), emptyCallback)
      val e2 = Event(e1.when, emptyCallback)
      val e3 = Event(e1.when, emptyCallback)

      consumer ! Put(e1)
      consumer ! Put(e3)
      consumer ! Put(e2)

      consumer ! FetchNext(Instant.MAX)
      expectMsg(FetchNextResult(Seq(e1, e3, e2)))
    }

    "return events ordered by time" in {
      val consumer = system.actorOf(Props[ConsumerActor])

      val e1 = Event(Instant.now(), emptyCallback)
      val e2 = Event(e1.when.plusSeconds(1), emptyCallback)
      val e3 = Event(e1.when.minusSeconds(1), emptyCallback)

      consumer ! Put(e1)
      consumer ! Put(e2)
      consumer ! Put(e3)

      consumer ! FetchNext(Instant.MAX)
      expectMsg(FetchNextResult(Seq(e3, e1, e2)))
    }
  }
}
