package am.reachme.service

import scala.reflect.ClassTag
import scala.reflect.classTag
import akka.actor.Props
import am.reachme.service.Aggregate.Event
import am.reachme.service.Aggregate.Created
import am.reachme.service.Aggregate.NeverCreated
import am.reachme.service.Aggregate.User
import am.reachme.service.UserAggregate.UserRegistered
import am.reachme.service.UserAggregateManager.RegisterUser
import am.reachme.service.UserAggregateManager.ChangePassword
import am.reachme.service.UserAggregate.PasswordChanged
import am.reachme.service.UserAggregate.EmptyUser
import am.reachme.service.UserAggregate.NonEmptyUser
import am.reachme.service.UserAggregateManager.GetUser

object UserAggregate {

  object EmptyUser extends User
  case class NonEmptyUser(firstName: String, lastName: String, phoneNumber: String, userName: String, password: String) extends User
  case class UserRegistered(firstName: String, secondName: String, phoneNumber: String, userName: String, password: String) extends Event
  case class PasswordChanged(userName: String, newPassword: String) extends Event

  def props(id: String): Props = Props(new UserAggregate(id))

}

class UserAggregate(id: String) extends Aggregate {

  override def persistenceId: String = id

  override def domainEventClassTag: ClassTag[Aggregate.Event] = classTag[Aggregate.Event]

  startWith(NeverCreated, EmptyUser)

  when(NeverCreated) {
    case Event(RegisterUser(firstName, lastName, phoneNumber, userName, password), _) =>
      goto(Created) applying UserRegistered(firstName, lastName, phoneNumber, userName, password)
    case Event(GetUser, user) ⇒
      stay replying user
  }

  when(Created) {
    case Event(ChangePassword(userName, newPassword), _) =>
      stay applying PasswordChanged(userName, newPassword)
    case Event(GetUser(userName), data) ⇒
      stay replying data
  }

  def applyEvent(domainEvent: Aggregate.Event, currentUser: Aggregate.User): Aggregate.User = {
    domainEvent match {
      case UserRegistered(firstName, lastName, phoneNumber, userName, password) =>
        NonEmptyUser(firstName, lastName, phoneNumber, userName, password)

      case PasswordChanged(userName, newPassword) =>
        currentUser match {
          case user: NonEmptyUser =>
            user.copy(password = newPassword)
        }
    }
  }

}
