package am.reachme.service

import akka.actor.ActorLogging
import akka.actor.Props
import am.reachme.service.UserAggregateManager.RegisterUser
import am.reachme.service.UserAggregateManager.GetUser
import am.reachme.service.AggregateManager.UserCommand
import am.reachme.service.AggregateManager.UserCommand

object UserAggregateManager {
  case class RegisterUser(override val userName: String, firstName: String, secondName: String, phoneNumber: String, oldPhoneNumbers:List[String]) extends UserCommand
  case class ChangeOldNumbers(override val userName: String, phoneNumber: String, oldPhoneNumbers:List[String]) extends UserCommand

  case class GetUser(override val userName: String) extends UserCommand

  def props: Props = Props(new UserAggregateManager)
  var id = "";
}

class UserAggregateManager extends AggregateManager {
  def processCommand = {
    case userCommand: UserCommand => processAggregateCommand(userCommand.userName, userCommand)
  }

  def processAggregateCommand(aggregateId: String, command: UserCommand) = {
    val maybeChild = context child aggregateId
    maybeChild match {
      case Some(child) =>
        child forward command
      case None => {
        val child = create(aggregateId)
        child forward command
      }
    }
  }

  def create(id: String) = {
    val agg = context.actorOf(aggregateProps(id), id)
    context watch agg
    println(agg)
    var a  = ("" + agg)
    UserAggregateManager.id = a.split("#")(1)  
    agg
  }

  override def aggregateProps(id: String) = am.reachme.service.UserAggregate.props(id)

}