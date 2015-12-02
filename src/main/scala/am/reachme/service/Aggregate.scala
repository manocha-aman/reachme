package am.reachme.service

import am.reachme.service.Aggregate.Event
import akka.persistence.PersistentActor
import akka.actor.ActorLogging
import akka.persistence.fsm.PersistentFSM.FSMState
import akka.persistence.fsm.PersistentFSM
import akka.persistence.Persistence
import am.reachme.service.Aggregate.User

object Aggregate {
  trait User 

  case object NeverCreated extends FSMState {
    override def identifier: String = "Never Created"
  }

  case object Created extends FSMState {
    override def identifier: String = "Created"
  }

  case object Removed extends FSMState {
    override def identifier: String = "Removed"
  }

  trait Event
}
abstract class Aggregate extends PersistentFSM[FSMState, User, Event]
		