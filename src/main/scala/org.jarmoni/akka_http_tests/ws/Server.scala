package org.jarmoni.akka_http_tests.ws

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.http.scaladsl.server.Directives._

import scala.io.StdIn

object Server extends App  {

  // https://blog.scalac.io/2015/07/30/websockets-server-with-akka-http.html

  implicit val actorSystem = ActorSystem("akka-system")
  implicit val flowMaterializer = ActorMaterializer()

  val interface = "localhost"
  val port = 8080


  val route = get {
    pathEndOrSingleSlash {
      complete("Welcome to websocket server")
    }
  } ~
    path("ws-echo") {
      get {
        handleWebSocketMessages(echoService)
      }
    }

  val echoService: Flow[Message, Message, _] = Flow[Message].map {
    case TextMessage.Strict(txt) => TextMessage("ECHO: " + txt)
    case _ => TextMessage("Message type unsupported")
  }

  val bindingFuture = Http().bindAndHandle(route, interface, port)

  println(s"Server is now online at http://$interface:$port\nPress RETURN to stop...")
  StdIn.readLine()

  import actorSystem.dispatcher

  bindingFuture.flatMap(_.unbind()).onComplete(_ => actorSystem.terminate())
  println("Server is down...")

}
