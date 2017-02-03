package com.gvolpe.lunatech.http

import com.gvolpe.lunatech.elasticsearch.ESAirportService
import com.gvolpe.lunatech.model.RunwaysPerAirport
import com.gvolpe.lunatech.service.AirportService
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._

object SearchHttpEndpoint {

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]) = jsonOf[A]
  implicit def circeJsonEncoder[A](implicit encoder: Encoder[A]) = jsonEncoderOf[A]

  object NameQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("name")
  object CodeQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("code")

  implicit class RunwaysPerAirportOps(list: List[RunwaysPerAirport]) {
    def handleResponse = list match {
      case Nil    => NoContent()
      case result => Ok(result)
    }
  }

  val service = HttpService {
    case GET -> Root / "search" :? NameQueryParamMatcher(name) :? CodeQueryParamMatcher(code) =>
      (name, code) match {
        case (Some(n), _) => AirportService.findAirportsAndRunwaysByName(n).handleResponse
        case (_, Some(c)) => AirportService.findAirportsAndRunwaysByCode(c).handleResponse
        case (_, _)       => BadRequest()
      }
    case GET -> Root / "es" / "search" :? NameQueryParamMatcher(name) :? CodeQueryParamMatcher(code) =>
      (name, code) match {
        case (Some(n), _) => ESAirportService.findAirportsAndRunwaysByName(n).flatMap(_.handleResponse)
        case (_, Some(c)) => ESAirportService.findAirportsAndRunwaysByCode(c).flatMap(_.handleResponse)
        case (_, _)       => BadRequest()
      }
  }

}
