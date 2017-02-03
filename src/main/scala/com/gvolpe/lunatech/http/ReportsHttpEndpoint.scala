package com.gvolpe.lunatech.http

import com.gvolpe.lunatech.elasticsearch.ESAirportService
import com.gvolpe.lunatech.service.AirportService
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._

object ReportsHttpEndpoint {

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]) = org.http4s.circe.jsonOf[A]
  implicit def circeJsonEncoder[A](implicit encoder: Encoder[A]) = org.http4s.circe.jsonEncoderOf[A]

  val service = HttpService {
    case GET -> Root / "report" =>
      Ok(AirportService.fullReport)
    case GET -> Root / "es" / "report" =>
      ESAirportService.highestLowestAirportsPerCountry().flatMap(v => Ok(v))
  }

}
