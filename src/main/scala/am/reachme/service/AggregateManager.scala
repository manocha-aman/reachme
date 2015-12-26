package am.reachme.service

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props

object AggregateManager {
  trait  UserCommand {
    def phoneNumber:String
  }
}

abstract class AggregateManager extends Actor with ActorLogging {
  def processCommand: Receive

  def aggregateProps(id: String): Props

  def receive = processCommand
}