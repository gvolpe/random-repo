package com.gvolpe.lunatech.elasticsearch

import java.util.Calendar

import com.sksamuel.elastic4s.{ElasticClient, ElasticsearchClientUri}
import com.sksamuel.elastic4s.ElasticDsl._
import com.gvolpe.lunatech.FS2Utils._
import com.gvolpe.lunatech.FileIndexingManager
import com.gvolpe.lunatech.model._
import com.gvolpe.lunatech.service.AirportService
import com.sksamuel.elastic4s.indexes.RichIndexResponse
import fs2.{Stream, Task, async}

object ESIndexingManager extends App {

  implicit val S = fs2.Strategy.fromFixedDaemonPool(4, "es-manager")

  private val client = ElasticClient.transport(ElasticsearchClientUri("localhost", 9300))

  private val responseLogger = loggerSink[RichIndexResponse]

  private val esAirportsPipe: PipeT[ESAirport, RichIndexResponse] = esAirport =>
    esAirport.flatMap { a =>
      Stream.eval {
        client.execute {
          indexInto("lunatech" / "airports") fields ("airport_id" -> a.id, "airport_name" -> a.name, "country_id" -> a.country.id, "country_code" -> a.country.code, "country_name" -> a.country.name)
        }.asTask
      }
    }

  private val esRunwaysPipe: PipeT[ESRunway, RichIndexResponse] = esRunway =>
    esRunway.flatMap { r =>
      Stream.eval {
        client.execute {
          indexInto("lunatech" / "runways") fields ("runway_id" -> r.id, "airport_id" -> r.airport.id, "airport_name" -> r.airport.name, "surface" -> r.surface, "ident" -> r.leIdent, "country_id" -> r.airport.country.id, "country_code" -> r.airport.country.code, "country_name" -> r.airport.country.name)
        }.asTask
      }
    }

  private def runwaysWithCountry: PipeT[ESAirport, ESRunway] = esAirports =>
    for {
      airport <- esAirports
      runway  <- AirportService.allRunways.filter(_.airportId == airport.id)
    } yield ESRunway.fromRunway(runway, airport)

  private def airportsWithCountry: PipeT[Country, ESAirport] = countries =>
    for {
      country <- countries
      airport <- AirportService.allAirports.filter(_.countryCode == country.code)
    } yield ESAirport.fromAirport(airport, country)

  private def indexProcess(countryQ: QueueT[Country], airportQ: QueueT[ESAirport]) =
    fs2.concurrent.join(3)(
      Stream(
        AirportService.allCountries to countryQ.enqueue,
        countryQ.dequeue through airportsWithCountry observe airportQ.enqueue through esAirportsPipe to responseLogger,
        airportQ.dequeue through runwaysWithCountry  through esRunwaysPipe to responseLogger
      )
    )

  private def now(): Long = Calendar.getInstance().getTimeInMillis

  val start = now()

  FileIndexingManager.run() // Indexing all the files in memory first

  println(">>>> Starting indexing of files on elasticsearch <<<<")

  val process = for {
    countryQ    <- Stream.eval(async.boundedQueue[Task, Country](100))
    airportQ    <- Stream.eval(async.boundedQueue[Task, ESAirport](500))
    program     <- indexProcess(countryQ, airportQ)
  } yield program

  process.run.unsafeRun()

  val seconds = (now() - start) / 1000
  println(s">>>> Index finished after $seconds seconds. <<<<")

}
