package am.reachme.service

import akka.persistence.query.PersistenceQuery
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal

class Application {
  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    val readJournal = PersistenceQuery(system).readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)

    // readJournal.eventsByTag(tag, offset)
  }
}