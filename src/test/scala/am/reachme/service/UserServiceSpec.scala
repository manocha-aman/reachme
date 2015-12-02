package am.reachme.service

import am.reachme.service.UserAggregateManager.RegisterUser
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.TestActorRef
import akka.util.Timeout
import org.scalatest.FlatSpec
import org.scalatest.BeforeAndAfterAll
import scala.concurrent.duration._
import scala.concurrent.{ Future, Await }
import scala.language.postfixOps
import am.reachme.service.Aggregate.User
import am.reachme.service.Aggregate.NeverCreated
import am.reachme.service.UserAggregateManager.ChangePassword
import am.reachme.service.UserAggregate.NonEmptyUser
import am.reachme.service.UserAggregateManager.GetUser

class UserServiceSpec extends FlatSpec with BeforeAndAfterAll {

  implicit val actorSystem = ActorSystem("userServiceSpec-system")

  implicit val timeout = Timeout(10 seconds)

  implicit val executionContext = actorSystem.dispatcher

  override def afterAll = {
    actorSystem.terminate()
  }

  "UserService" should "create new user" in {
    val manager = TestActorRef(UserAggregateManager.props, "UserService-test-actor")

    manager ! RegisterUser(firstName = "aman", secondName = "Manocha", phoneNumber = "9899901287", userName = "amanmanocha", password = "abc")
    
    val future = (manager ? GetUser("amanmanocha")).mapTo[am.reachme.service.Aggregate.User];

    val NonEmptyUser(_, _, _, usrName, _) = Await.result(future,  5 seconds)

    assert(usrName == "amanmanocha")
  }

//  it should "update password of user" in {
//    val manager = TestActorRef(UserAggregateManager.props)
//
//    val future = (manager ? ChangePassword("amanmanocha", "new_password")).mapTo[am.reachme.service.Aggregate.User];
//
//    val NonEmptyUser(_, _, _, usrName, password) = Await.result(future, 2 seconds)
//
//    assert(usrName == "amanmanocha")
//    assert(password == "new_password")
//  }
//
//  it should "return never created if user is not created" in {
//    val manager = TestActorRef(UserAggregateManager.props)
//
//    val future = (manager ? ChangePassword("amanmanocha_1", "new_password"))
//
//    val NeverCreated = Await.result(future, 2 seconds)
//  }

}