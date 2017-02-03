package com.gvolpe.lunatech.http

import com.gvolpe.lunatech.FileIndexingManager
import org.http4s._
import org.http4s.dsl._

object IndexHttpEndpoint {

  val service = HttpService {
    case POST -> Root / "index" =>
      FileIndexingManager.run()
      Ok("Files have been indexed...")
  }

}
