package com.gvolpe.lunatech

object model {

  case class Country(id: Long, code: String, name: String)
  case class Airport(id: Long, name: String, countryCode: String)
  case class Runway(id: Long, airportId: Long, surface: String, leIdent: Option[String])

  case class RunwaysPerAirport(airport: Airport, runways: Vector[Runway])

  case class AirportsCountPerCountry(name: String, count: Int)
  case class HighestLowestAirportsPerCountry(lowest: List[AirportsCountPerCountry],
                                             highest: List[AirportsCountPerCountry])

  case class RunwaySurfacePerCountry(name: String, surfaces: List[String])

  case class Report(highestLowestAirportsPerCountry: HighestLowestAirportsPerCountry,
                    runwaySurfacesPerCountry: List[RunwaySurfacePerCountry],
                    mostCommonRunwayIdentifications: List[String])

  case class ESAirport(id: Long, name: String, country: Country)
  case class ESRunway(id: Long, surface: String, leIdent: Option[String], airport: ESAirport)

  object ESAirport {
    def fromAirport(airport: Airport, country: Country) = ESAirport(
      airport.id, airport.name, country
    )
  }

  object ESRunway {
    def fromRunway(runway: Runway, airport: ESAirport) = ESRunway(
      runway.id, runway.surface, runway.leIdent, airport
    )
  }

}
