package am.reachme.service.view

import akka.actor.Actor
import am.reachme.service.UserAggregateManager
import akka.actor.Props

class ReachmeActor extends Actor with ReachmeService {
  def actorRefFactory = context
  
  def receive = runRoute(rootRoute)
  
}