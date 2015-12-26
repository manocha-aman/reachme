package am.reachme.service

import java.util.concurrent.ConcurrentHashMap
import scala.collection.concurrent
import scala.collection.convert.decorateAsScala.mapAsScalaConcurrentMapConverter
import akka.actor.ActorSystem
import akka.persistence.query.PersistenceQuery
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.stream.ActorMaterializer
import am.reachme.service.UserAggregate.OldNumbersChanged
import am.reachme.service.UserAggregate.UserRegistered
import am.reachme.service.view.UserRepository
case class ViewUser(phoneNumber: String, firstName: String, lastName: String, var oldPhoneNumbers: List[String])

object View {

  val usersRepository = UserRepository

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    startView(system, materializer)
  }

  def startView(system: ActorSystem, materializer: ActorMaterializer) {
    val readJournal = PersistenceQuery(system).readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)
    readJournal.eventsByTag("UserEvents", 0L)
      .map(_.event)
      .runForeach { x => update(x) }(materializer)
  }

  def update(event: Any) = {
    event match {
      case UserRegistered(phoneNumber, firstName, lastName, oldPhoneNumbers) => {
        val user = new ViewUser(phoneNumber, firstName, lastName, oldPhoneNumbers)
        usersRepository.add(user)
      }
      case OldNumbersChanged(phoneNumber, oldNumbers) => {
        usersRepository.updateOldNumbers(phoneNumber, oldNumbers)
      }
      case _ =>
    }
  }

}