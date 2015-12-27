package am.reachme.service.view

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.io.IO
import spray.can.Http
import am.reachme.service.View
import akka.stream.ActorMaterializer

object Boot {
  val view = View

  def main(args: Array[String]) {
    implicit val system = ActorSystem()

    //create our service actor
    val service = system.actorOf(Props[ReachmeActor], "reachme-service")

    val materializer = ActorMaterializer()
    view.startView(system, materializer)
    
    IO(Http) ! Http.Bind(service, interface = "localhost", port = 8000)
  }
}