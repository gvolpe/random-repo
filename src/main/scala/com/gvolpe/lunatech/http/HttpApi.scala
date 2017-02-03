package com.gvolpe.lunatech.http

import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.{Server, ServerApp}

import scalaz.concurrent.Task

object HttpApi extends ServerApp {

  override def server(args: List[String]): Task[Server] = {
    BlazeBuilder
      .bindHttp(8080, "localhost")
      .mountService(IndexHttpEndpoint.service)
      .mountService(SearchHttpEndpoint.service)
      .mountService(ReportsHttpEndpoint.service)
      .start
  }

}
