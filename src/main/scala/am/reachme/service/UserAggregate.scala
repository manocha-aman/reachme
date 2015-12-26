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
import am.reachme.service.UserAggregateManager.ChangeOldNumbers
import am.reachme.service.UserAggregate.EmptyUser
import am.reachme.service.UserAggregate.NonEmptyUser
import am.reachme.service.UserAggregate.OldNumbersChanged
import am.reachme.service.UserAggregateManager.GetUser
import am.reachme.service.UserAggregate.NonEmptyUser

object UserAggregate {

  object EmptyUser extends User
  case class NonEmptyUser(userName: String, firstName: String, lastName: String, phoneNumber: String, oldPhoneNumbers: List[String]) extends User
  case class UserRegistered(userName: String, firstName: String, secondName: String, phoneNumber: String, oldPhoneNumbers: List[String]) extends Event
  case class OldNumbersChanged(userName: String, oldPhoneNumbers: List[String]) extends Event

  def props(id: String): Props = Props(new UserAggregate(id))

}

class UserAggregate(id: String) extends Aggregate {

  override def persistenceId: String =  { id}

  override def domainEventClassTag: ClassTag[Aggregate.Event] = classTag[Aggregate.Event]

  startWith(NeverCreated, EmptyUser)

  when(NeverCreated) {
    case Event(RegisterUser(userName, firstName, lastName, phoneNumber, oldPhoneNumbers), user) => {
      val originalSender = sender
      goto(Created) applying UserRegistered(userName, firstName, lastName, phoneNumber, oldPhoneNumbers) andThen {
        case _ => originalSender ! stateData
      }
    }
    case Event(GetUser, user) ⇒
      stay replying user
  }

  when(Created) {
    case Event(GetUser(userName), data) =>
      stay replying data
    case Event(ChangeOldNumbers(userName, phoneNumber, oldPhoneNumbers), user) => {
      val originalSender = sender
      stay applying OldNumbersChanged(userName, oldPhoneNumbers) andThen {
        case _ => originalSender ! stateData
      }
    }
    case _ =>
      stay replying "unhandled event + "
  }

  def applyEvent(domainEvent: Aggregate.Event, currentUser: Aggregate.User): Aggregate.User = {
    domainEvent match {
      case UserRegistered(userName, firstName, lastName, phoneNumber, oldPhoneNumbers) =>
        NonEmptyUser(userName, firstName, lastName, phoneNumber, oldPhoneNumbers)

      case OldNumbersChanged(userName, oldNumbers) => {
        currentUser match {
          case oldUser: NonEmptyUser =>
            oldUser.copy(oldPhoneNumbers = oldNumbers)

        }
      }
    }
  }

}
