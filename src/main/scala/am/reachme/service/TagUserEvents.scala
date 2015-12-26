package am.reachme.service
import akka.persistence.journal.Tagged
import akka.persistence.journal.WriteEventAdapter
import akka.persistence.journal.EventSeq
import akka.persistence.journal.EventAdapter
import akka.actor.ExtendedActorSystem

class TagUserEvents(system: ExtendedActorSystem) extends EventAdapter {
  override def toJournal(event: Any): Any = {
    println("Tagging")
    Tagged(event, Set("UserEvents"))
  }

  override def fromJournal(event: Any, manifest: String): EventSeq =
    EventSeq.single(event) // identity

  override def manifest(event: Any): String = ""

}