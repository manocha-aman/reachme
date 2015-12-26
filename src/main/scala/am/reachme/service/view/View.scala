package am.reachme.service

import scala.collection.mutable.HashMap
import akka.actor.ActorSystem
import akka.persistence.query.PersistenceQuery
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.stream.ActorMaterializer
import am.reachme.service.UserAggregate.OldNumbersChanged
import am.reachme.service.UserAggregate.UserRegistered
import scala.collection.concurrent.Map
import scala.collection._
import scala.collection.convert.decorateAsScala._
import java.util.concurrent.ConcurrentHashMap

case class ViewUser(phoneNumber: String, firstName: String, lastName: String, var oldPhoneNumbers: List[String])

object StreamingJournalQueries {
  val users: concurrent.Map[(String, String), ViewUser] = new ConcurrentHashMap().asScala

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    val readJournal = PersistenceQuery(system).readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)

    readJournal.eventsByTag("UserEvents", 0L)
      .map(_.event)
      .runForeach { x => update(x) }
  }

  def update(event: Any) = {
    event match {
      case UserRegistered(phoneNumber, firstName, lastName, oldPhoneNumbers) => {
        val user = new ViewUser(phoneNumber, firstName, lastName, oldPhoneNumbers)
        users += ((phoneNumber, "") -> user)
        addOldNumbersAsKeys(user)
      }
      case OldNumbersChanged(phoneNumber, oldNumbers) => {
        val user = users((phoneNumber, ""))

        removeOldNumbersAsKeys(user)
        user.oldPhoneNumbers = oldNumbers;
        addOldNumbersAsKeys(user)

        println(s"chnaged $users")
        println(users(("aman", "13128981341")))
      }
      case _ =>
    }
  }

  def addOldNumbersAsKeys(user: ViewUser) = {
    user.oldPhoneNumbers.foreach {
      number =>
        {
          users += ((user.firstName, number) -> user)
          users += ((user.lastName, number) -> user)
        }
    }
  }

  def removeOldNumbersAsKeys(user: ViewUser) = {
    user.oldPhoneNumbers.foreach { number =>
      users -= ((user.firstName, number))
      users -= ((user.lastName, number))
    }
  }
  
}