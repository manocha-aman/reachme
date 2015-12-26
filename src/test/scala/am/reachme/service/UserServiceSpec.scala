package am.reachme.service

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FlatSpec
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.TestActorRef
import akka.util.Timeout
import am.reachme.service.Aggregate.User
import am.reachme.service.UserAggregate.NonEmptyUser
import am.reachme.service.UserAggregateManager.RegisterUser
import am.reachme.service.UserAggregateManager.ChangeOldNumbers
import scala.reflect.io.Path
import akka.persistence.query.PersistenceQuery
import akka.stream.scaladsl.Source
import akka.stream.ActorMaterializer
import akka.persistence.query.EventEnvelope
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import am.reachme.service.UserAggregate.UserRegistered
import scala.collection._
import scala.collection.convert.decorateAsScala._
import java.util.concurrent.ConcurrentHashMap
import am.reachme.service.UserAggregate.OldNumbersChanged

class UserServiceSpec extends FlatSpec with BeforeAndAfterAll {

  val users: concurrent.Map[(String, String), ViewUser] = new ConcurrentHashMap().asScala

  implicit val actorSystem = ActorSystem("userServiceSpec-system")

  implicit val timeout = Timeout(10 seconds)

  implicit val executionContext = actorSystem.dispatcher
  implicit val mat = ActorMaterializer()(actorSystem)
  override def afterAll = {
    actorSystem.terminate()
  }

  override def beforeAll = {
    val path: Path = Path(".\\target\\example\\journal")
    path.toAbsolute.deleteRecursively()
    view()
  }

  "UserService" should "create new user" in {
    val manager = TestActorRef(UserAggregateManager.props)

    val registerUserCommand = RegisterUser(userName = "amanmanocha", firstName = "aman", secondName = "Manocha", phoneNumber = "9899901287", oldPhoneNumbers = List("931215035"))

    val future = (manager ? registerUserCommand).mapTo[am.reachme.service.Aggregate.User]

    val NonEmptyUser(usrName, _, _, _, _) = Await.result(future, 5 seconds)

    assert(usrName == "amanmanocha")

  }

  "it" should "change old numbers" in {

    val manager = TestActorRef(UserAggregateManager.props, "UserService-test-actor")

    val addOldNumberCommand = ChangeOldNumbers(userName = "amanmanocha", phoneNumber = "9899901287", oldPhoneNumbers = List("931215035", "13128981341"))

    val future = (manager ? addOldNumberCommand).mapTo[am.reachme.service.Aggregate.User]

    val NonEmptyUser(usrName, _, _, _, oldNumbers) = Await.result(future, 5 seconds)
  }

  def view(): Unit = {

    val readJournal = PersistenceQuery(actorSystem).readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)

    readJournal.eventsByPersistenceId("amanmanocha", 0L, 100L)
      .map(_.event)
      .runForeach { x => update(x) }
  }

  def update(event: Any) = {

    event match {
      case UserRegistered(userName, firstName, lastName, phoneNumber, oldPhoneNumbers) => {
        val user = new ViewUser(userName, firstName, lastName, phoneNumber, oldPhoneNumbers)
        users += ((userName, "") -> user)
        addOldNumbersAsKeys(user)
      }
      case OldNumbersChanged(userName, oldNumbers) => {
        val user = users((userName, ""))

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