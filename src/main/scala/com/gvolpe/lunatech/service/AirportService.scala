package com.gvolpe.lunatech.service

import com.gvolpe.lunatech.FS2Utils._
import com.gvolpe.lunatech.model._
import fs2.{ Task, Stream }

import scala.collection.mutable

object AirportService {

  private lazy val _reportCache   = mutable.ListBuffer.empty[Report]

  private lazy val _countryIndex  = mutable.ListBuffer.empty[Country]
  private lazy val _airportIndex  = mutable.ListBuffer.empty[Airport]
  private lazy val _runwayIndex   = mutable.ListBuffer.empty[Runway]

  private lazy val countriesIndexSink  = liftSink[Country] { c => Task.delay { _countryIndex += c } }
  private lazy val airportsIndexSink   = liftSink[Airport] { a => Task.delay { _airportIndex += a } }
  private lazy val runwaysIndexSink    = liftSink[Runway] { r => Task.delay { _runwayIndex += r } }

  def indexCountry: SinkT[Country]  = _ to countriesIndexSink
  def indexAirport: SinkT[Airport]  = _ to airportsIndexSink
  def indexRunway: SinkT[Runway]    = _ to runwaysIndexSink

  def allCountries: StreamT[Country]  = Stream.emits(_countryIndex).covary[Task]
  def allAirports: StreamT[Airport]   = Stream.emits(_airportIndex).covary[Task]
  def allRunways: StreamT[Runway]     = Stream.emits(_runwayIndex).covary[Task]

  private def groupByAirport(result: mutable.ListBuffer[(Airport, Runway)]): List[RunwaysPerAirport] =
    result.groupBy(_._1).map(kv => RunwaysPerAirport(kv._1, kv._2.map(_._2).toVector)).toList

  def findAirportsAndRunwaysByName(countryName: String): List[RunwaysPerAirport] = {
    val result = for {
      country <- _countryIndex.filter(_.name.toLowerCase.contains(countryName.toLowerCase))
      airport <- _airportIndex.filter(_.countryCode == country.code)
      runway  <- _runwayIndex.filter(_.airportId == airport.id)
    } yield (airport, runway)
    groupByAirport(result)
  }

  def findAirportsAndRunwaysByCode(countryCode: String): List[RunwaysPerAirport] = {
    val result = for {
      airport <- _airportIndex.filter(_.countryCode == countryCode.toUpperCase)
      runway  <- _runwayIndex.filter(_.airportId == airport.id)
    } yield (airport, runway)
    groupByAirport(result)
  }

  ////////////////// REPORTS ///////////////////////////

  def airportsCountPerCountry: List[AirportsCountPerCountry] = {
    val result = for {
      country <- _countryIndex
      airport <- _airportIndex.filter(_.countryCode == country.code)
    } yield (country.name, airport)

    result.groupBy(_._1)
      .mapValues(_.size)
      .map(kv => AirportsCountPerCountry(kv._1, kv._2))
      .toList
      .sortBy(_.count)
  }

  def highestAndLowestNumberOfAirports: HighestLowestAirportsPerCountry = {
    val result = airportsCountPerCountry
    HighestLowestAirportsPerCountry(
      lowest = result.take(10).reverse,
      highest = result.takeRight(10).reverse
    )
  }

  def runwaySurfacesPerCountry: List[RunwaySurfacePerCountry] = {
    val result = for {
      country <- _countryIndex
      airport <- _airportIndex.filter(_.countryCode == country.code)
      runway  <- _runwayIndex.filter(_.airportId == airport.id)
    } yield (country.name, runway)

    result.groupBy(_._1)
      .mapValues(_.map(_._2.surface).toList.distinct)
      .map(kv => RunwaySurfacePerCountry(kv._1, kv._2))
      .toList
  }

  def mostCommonRunwayIdentifications: List[String] =
    _runwayIndex
      .filter(_.leIdent.isDefined)
      .groupBy(_.leIdent.get)
      .mapValues(_.size)
      .toList.sortBy(_._2)
      .reverse
      .take(10)
      .map(_._1)

  def fullReport: Report = _reportCache.headOption match {
    case Some(value) => value
    case None        =>
      val result = createReport
      _reportCache += result
      result
  }

  private def createReport = Report(
    highestLowestAirportsPerCountry = highestAndLowestNumberOfAirports,
    runwaySurfacesPerCountry        = runwaySurfacesPerCountry,
    mostCommonRunwayIdentifications = mostCommonRunwayIdentifications
  )

}
