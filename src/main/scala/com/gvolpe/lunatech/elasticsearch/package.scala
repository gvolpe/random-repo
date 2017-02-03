package com.gvolpe.lunatech

import com.gvolpe.lunatech.model._
import com.sksamuel.elastic4s.searches.RichSearchResponse
import com.sksamuel.elastic4s.{Hit, HitReader}

import scala.collection.JavaConverters._

package object elasticsearch {

  case class AirportAndRunway(ar: (Airport, Runway))

  implicit object AirportAndRunwayHitReader extends HitReader[AirportAndRunway] {
    override def read(hit: Hit): Either[Throwable, AirportAndRunway] = {
      val src         = hit.sourceAsMap
      val id          = src.getOrElse("runway_id", "0").toString.toLong
      val airportId   = src.getOrElse("airport_id", "0").toString.toLong
      val airportName = src.getOrElse("airport_name", "").toString
      val surface     = src.getOrElse("surface", "").toString
      val countryCode = src.getOrElse("country_code", "").toString
      val ident       = src.get("ident").map(_.toString)

      val airport = Airport(airportId, airportName, countryCode)
      val runway  = Runway(id, airportId, surface, ident)

      Right(AirportAndRunway((airport, runway)))
    }
  }

  implicit class RichSearchResponseOps(response: RichSearchResponse) {

    def asRunwaysPerAirport: List[RunwaysPerAirport] = {
      response.to[AirportAndRunway]
        .toVector
        .groupBy(_.ar._1)
        .mapValues(_.map(_.ar._2))
        .map(kv => RunwaysPerAirport(kv._1, kv._2))
        .toList
    }

    def asHighestLowestAirportsPerCountry: HighestLowestAirportsPerCountry = {
      val buckets = response.aggregations.termsResult("group_by_country").getBuckets.asScala
      val highest = buckets.take(10).map( b => AirportsCountPerCountry(b.getKeyAsString, b.getDocCount.toInt))
      val lowest  = buckets.takeRight(10).map( b => AirportsCountPerCountry(b.getKeyAsString, b.getDocCount.toInt))
      HighestLowestAirportsPerCountry(lowest.toList, highest.toList)
    }

  }

}
