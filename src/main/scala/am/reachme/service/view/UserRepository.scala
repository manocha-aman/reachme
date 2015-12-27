package am.reachme.service.view

import java.util.concurrent.ConcurrentHashMap

import scala.collection.concurrent
import scala.collection.convert.decorateAsScala.mapAsScalaConcurrentMapConverter

object UserRepository {
  val users: concurrent.Map[(String, String), ViewUser] = new ConcurrentHashMap().asScala
  def add(user: ViewUser) = {
    users += ((user.phoneNumber, "") -> user)
    addOldNumbersAsKeys(user)
  }

  def updateOldNumbers(phoneNumber: String, oldNumbers:List[String]) = {
    val user = users((phoneNumber, ""))

    removeOldNumbersAsKeys(user)
    user.oldPhoneNumbers = oldNumbers;
    addOldNumbersAsKeys(user)

    println(s"chnaged $users")
    println(users(("aman", "13128981341")))
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