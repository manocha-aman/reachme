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
  case class NonEmptyUser(phoneNumber: String, firstName: String, lastName: String, oldPhoneNumbers: List[String]) extends User
  case class UserRegistered(phoneNumber: String, firstName: String, lastName: String, oldPhoneNumbers: List[String]) extends Event
  case class OldNumbersChanged(phoneNumber: String, oldPhoneNumbers: List[String]) extends Event

  def props(id: String): Props = Props(new UserAggregate(id))
}

class UserAggregate(id: String) extends Aggregate {

  override def persistenceId: String =  { id}

  override def domainEventClassTag: ClassTag[Aggregate.Event] = classTag[Aggregate.Event]

  startWith(NeverCreated, EmptyUser)

  when(NeverCreated) {
    case Event(RegisterUser(phoneNumber, firstName, lastName, oldPhoneNumbers), user) => {
      val originalSender = sender
      goto(Created) applying UserRegistered(phoneNumber, firstName, lastName, oldPhoneNumbers) andThen {
        case _ => originalSender ! stateData
      }
    }
    case Event(GetUser, user) ⇒
      stay replying user
  }

  when(Created) {
    case Event(GetUser(phoneNumber), data) =>
      stay replying data
    case Event(ChangeOldNumbers(phoneNumber, oldPhoneNumbers), user) => {
      val originalSender = sender
      stay applying OldNumbersChanged(phoneNumber, oldPhoneNumbers) andThen {
        case _ => originalSender ! stateData
      }
    }
    case _ =>
      stay replying "unhandled event + "
  }

  def applyEvent(domainEvent: Aggregate.Event, currentUser: Aggregate.User): Aggregate.User = {
    domainEvent match {
      case UserRegistered(phoneNumber, firstName, lastName, oldPhoneNumbers) =>
        NonEmptyUser(phoneNumber, firstName, lastName, oldPhoneNumbers)

      case OldNumbersChanged(phoneNumber, oldNumbers) => {
        currentUser match {
          case oldUser: NonEmptyUser =>
            oldUser.copy(oldPhoneNumbers = oldNumbers)

        }
      }
    }
  }

}
