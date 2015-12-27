package am.reachme.service.view

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import org.json4s.DefaultFormats
import org.json4s.Formats
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import am.reachme.service.UserAggregateManager
import am.reachme.service.UserAggregateManager.RegisterUser
import spray.httpx.Json4sSupport
import spray.httpx.marshalling.ToResponseMarshallable.isMarshallable
import spray.routing.Directive.pimpApply
import spray.routing.HttpService
import am.reachme.service.Aggregate.User
import scala.concurrent.Await

object Json4sProtocol extends Json4sSupport {
  implicit def json4sFormats: Formats = DefaultFormats
}

trait ReachmeService extends HttpService {
  import Json4sProtocol._
  implicit def executionContext = actorRefFactory.dispatcher
  implicit val timeout = Timeout(5 seconds)

  val userManager = actorRefFactory.actorOf(Props[UserAggregateManager])

  val usersRoute = {
    path("users") {
      post {
        entity(as[ViewUser]) { viewUser =>

          doCreate(viewUser)

          //complete(savedUser)
        }
      }
    }
  }

  def rootRoute = usersRoute

  def doCreate[T](viewUser: ViewUser) = {

    complete {
      implicit val timeout = Timeout(2 seconds)
      val registerUserCommand: RegisterUser = RegisterUser(phoneNumber = viewUser.phoneNumber, firstName = viewUser.firstName, secondName = viewUser.lastName, oldPhoneNumbers = viewUser.oldPhoneNumbers)

      val future = (userManager ? registerUserCommand).mapTo[User]

      val user = Await.result(future, 5 seconds)
      user
    }

  }
}