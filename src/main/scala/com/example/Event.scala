package com.example

import java.time.Instant

import com.example.ConsumerActor.Action

case class Event(when: Instant, action: Action)
