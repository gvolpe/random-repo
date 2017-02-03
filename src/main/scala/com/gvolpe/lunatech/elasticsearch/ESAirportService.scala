package com.gvolpe.lunatech.elasticsearch

import com.sksamuel.elastic4s.{ElasticClient, ElasticsearchClientUri}
import com.sksamuel.elastic4s.ElasticDsl._
import com.gvolpe.lunatech.FS2Utils._
import com.gvolpe.lunatech.model.{HighestLowestAirportsPerCountry, RunwaysPerAirport}

import scalaz.concurrent.{Task => Tazk}

object ESAirportService {

  implicit val S = fs2.Strategy.fromFixedDaemonPool(4, "es-airport-service")

  private val client = ElasticClient.transport(ElasticsearchClientUri("localhost", 9300))

  private def airportsPerCountryQuery() =
    client.execute {
      search("lunatech" / "airports") size 1 aggs {
        termsAggregation("group_by_country") field "country_code" size 300
      }
    }.asScalazTask

  private def runwaysPerCountryQuery(field: String, name: String) =
    client.execute {
      search("lunatech" / "runways") size 10000 query
        matchQuery (field, name)
    }.asScalazTask

  def highestLowestAirportsPerCountry(): Tazk[HighestLowestAirportsPerCountry] =
    airportsPerCountryQuery().map(_.asHighestLowestAirportsPerCountry)

  def findAirportsAndRunwaysByCode(code: String): Tazk[List[RunwaysPerAirport]] =
    runwaysPerCountryQuery("country_code", code).map(_.asRunwaysPerAirport)

  def findAirportsAndRunwaysByName(name: String): Tazk[List[RunwaysPerAirport]] =
    runwaysPerCountryQuery("country_name", name).map(_.asRunwaysPerAirport)

}
