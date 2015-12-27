package am.reachme.service.view

import spray.json.DefaultJsonProtocol

case class ViewUser(phoneNumber: String, firstName: String, lastName: String, var oldPhoneNumbers: List[String])