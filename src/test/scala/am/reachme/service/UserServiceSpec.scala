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
import am.reachme.service.Aggregate.User

class UserServiceSpec extends FlatSpec with BeforeAndAfterAll {

  implicit val actorSystem = ActorSystem("userServiceSpec-system")

  implicit val timeout = Timeout(2 seconds)

  implicit val executionContext = actorSystem.dispatcher
  implicit val mat = ActorMaterializer()(actorSystem)
  val manager = TestActorRef(UserAggregateManager.props)

  override def afterAll = {
    actorSystem.terminate()
  }

  override def beforeAll = {
    val path: Path = Path(".\\target\\example\\journal")
    path.toAbsolute.deleteRecursively()
    val view = View
    view.startView(actorSystem, mat)
  }

  "UserService" should "create new user" in {
    val registerUserCommand: RegisterUser = RegisterUser(phoneNumber = "9899901287", firstName = "aman", secondName = "Manocha", oldPhoneNumbers = List("931215035"))

    val future = (manager ? registerUserCommand).mapTo[User]

    val NonEmptyUser(phoneNumber, _, _, _) = Await.result(future, 5 seconds)

    assert(phoneNumber == "9899901287")
  }

  "it" should "change old numbers" in {
    val addOldNumberCommand: ChangeOldNumbers = ChangeOldNumbers(phoneNumber = "9899901287", oldPhoneNumbers = List("931215035", "13128981341"))

    val future = (manager ? addOldNumberCommand).mapTo[User]

    val NonEmptyUser(phoneNumber, _, _, oldNumbers) = Await.result(future, 5 seconds)
    
    assert(phoneNumber == "9899901287")
    assert(oldNumbers == List("931215035", "13128981341"))
  }

}