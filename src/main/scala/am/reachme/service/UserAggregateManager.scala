package am.reachme.service

import akka.actor.ActorLogging
import akka.actor.Props
import am.reachme.service AggregateManager.Command
import am.reachme.service.UserAggregateManager.RegisterUser
import am.reachme.service.UserAggregateManager.ChangePassword
import am.reachme.service.UserAggregateManager.GetUser

object UserAggregateManager {
  case class RegisterUser(firstName: String, secondName: String, phoneNumber: String, userName: String, password: String) extends Command
  case class ChangePassword(userName:String, newPassword:String) extends Command
  case class GetUser(userName:String) extends Command
  
  def props: Props = Props(new UserAggregateManager)
}

class UserAggregateManager extends AggregateManager {
  def processCommand = {
    case RegisterUser(firstName, lastName, phoneNumber, userName, password) => {
      val aggregateId =  userName
      processAggregateCommand(aggregateId, RegisterUser(firstName, lastName, phoneNumber, userName, password))
    }
    case ChangePassword(userName, newPassword) => {
      val aggregateId =  userName
      processAggregateCommand(userName, ChangePassword(userName, newPassword))
    }
    case GetUser(userName) => {
      val aggregateId =  userName
      processAggregateCommand(userName, GetUser(userName))
    }
  }

  def processAggregateCommand(aggregateId: String, command: Command) = {
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
    agg
  }

  override def aggregateProps(id: String) = am.reachme.service.UserAggregate.props(id)

}