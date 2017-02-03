package com.gvolpe.lunatech.service

import com.gvolpe.lunatech.model._
import org.scalatest.{FlatSpec, Matchers}
import fs2.Stream
import org.scalatest.prop.PropertyChecks

class AirportServiceSpec extends FlatSpec with Matchers with IndexingFixture {

  forAll(byNameExample){ (name, expected) =>
    it should s"find the airports and runways by country name: $name" in {
      val result = AirportService.findAirportsAndRunwaysByName(name)
      result foreach { v =>
        expected.contains(v) should be (true)
      }
    }
  }

  forAll(byCodeExample){ (code, expected) =>
    it should s"find the airports and runways by country code: $code" in {
      val result = AirportService.findAirportsAndRunwaysByCode(code)
      result foreach { v =>
        expected.contains(v) should be (true)
      }
    }
  }

  it should "retrieve the airports count per country [REPORT]" in {
    val result = AirportService.airportsCountPerCountry

    val expected = List(
      AirportsCountPerCountry("Vietnam",2),
      AirportsCountPerCountry("Argentina",3),
      AirportsCountPerCountry("Myanmar",3),
      AirportsCountPerCountry("Ireland",3),
      AirportsCountPerCountry("Uruguay",4),
      AirportsCountPerCountry("Colombia",5)
    )

    result should be (expected)
  }

  // In this case does not have much sense because there's not enough information but just for coverage
  it should "retrieve the highest and lowest amount of airports per country [REPORT]" in {
    val result = AirportService.highestAndLowestNumberOfAirports

    val highestAndLowest = List(
      AirportsCountPerCountry("Vietnam",2),
      AirportsCountPerCountry("Argentina",3),
      AirportsCountPerCountry("Myanmar",3),
      AirportsCountPerCountry("Ireland",3),
      AirportsCountPerCountry("Uruguay",4),
      AirportsCountPerCountry("Colombia",5)
    ).reverse

    val expected = HighestLowestAirportsPerCountry(lowest = highestAndLowest, highest = highestAndLowest)

    result should be (expected)
  }

  it should "retrieve the runway surfaces per country [REPORT]" in {
    val result = AirportService.runwaySurfacesPerCountry

    val expected = List(
      RunwaySurfacePerCountry("Argentina",List("GRE", "CON")),
      RunwaySurfacePerCountry("Myanmar",List("CON", "GRE", "ASP")),
      RunwaySurfacePerCountry("Vietnam",List("ASP", "CON")),
      RunwaySurfacePerCountry("Uruguay",List("ASP", "GRE", "CON")),
      RunwaySurfacePerCountry("Ireland",List("ASP", "GRE", "CON")),
      RunwaySurfacePerCountry("Colombia",List("ASP", "GRE", "CON"))
    )

    result should be (expected)
  }

  it should "retrieve the most common runway identifications per country [REPORT]" in {
    val result = AirportService.mostCommonRunwayIdentifications
    result should be (List("A", "B", "C"))
  }

}

trait IndexingFixture extends AirportServiceFixture {

  implicit val S = fs2.Strategy.fromFixedDaemonPool(4, "airport-service-spec")

  lazy val index = fs2.concurrent.join(3)(
    Stream(
      Stream.emits(countries) to AirportService.indexCountry,
      Stream.emits(airports)  to AirportService.indexAirport,
      Stream.emits(runways)   to AirportService.indexRunway
    )
  )
  index.run.unsafeRun()

}

trait AirportServiceFixture extends PropertyChecks {

  val byNameExample = Table(
    ("name", "expected"),
    ("argentina", List(
      RunwaysPerAirport(Airport(11, "eleven", "AR"), Vector(Runway(104, 11, "GRE", None))),
      RunwaysPerAirport(Airport(18, "eighteen", "AR"), Vector(Runway(107, 18, "GRE", None))),
      RunwaysPerAirport(Airport(27, "twenty seven", "AR"), Vector(Runway(114, 27, "CON", None)))
    )),
    ("Viet", List(
      RunwaysPerAirport(Airport(10, "ten", "VN"), Vector(Runway(100, 10, "ASP", None), Runway(123, 10, "CON", None))),
      RunwaysPerAirport(Airport(14, "fourteen", "VN"), Vector(Runway(120, 14, "CON", None)))
    )),
    ("Thailand", List.empty[RunwaysPerAirport])
  )

  val byCodeExample = Table(
    ("code", "expected"),
    ("ie", List(
      RunwaysPerAirport(Airport(21, "twenty one", "IE"), Vector(Runway(102, 21, "CON", None))),
      RunwaysPerAirport(Airport(16, "sixteen", "IE"), Vector(Runway(121, 16, "ASP", None), Runway(122, 16, "GRE", None))),
      RunwaysPerAirport(Airport(22, "twenty two", "IE"), Vector(Runway(108, 22, "CON", None)))
    )),
    ("VN", List(
      RunwaysPerAirport(Airport(10, "ten", "VN"), Vector(Runway(100, 10, "ASP", None), Runway(123, 10, "CON", None))),
      RunwaysPerAirport(Airport(14, "fourteen", "VN"), Vector(Runway(120, 14, "CON", None)))
    )),
    ("US", List.empty[RunwaysPerAirport])
  )

  val countries = Seq(
    Country(1, "AR", "Argentina"),
    Country(2, "IE" , "Ireland"),
    Country(3, "VN", "Vietnam"),
    Country(4, "MM", "Myanmar"),
    Country(5, "UY", "Uruguay"),
    Country(6, "CO", "Colombia")
  )

  val airports = Seq(
    Airport(10, "ten", "VN"),
    Airport(11, "eleven", "AR"),
    Airport(12, "twelve", "CO"),
    Airport(13, "thirteen", "MM"),
    Airport(14, "fourteen", "VN"),
    Airport(15, "fifteen", "UY"),
    Airport(16, "sixteen", "IE"),
    Airport(17, "seventeen", "CO"),
    Airport(18, "eighteen", "AR"),
    Airport(19, "nineteen", "CO"),
    Airport(20, "twenty", "MM"),
    Airport(21, "twenty one", "IE"),
    Airport(22, "twenty two", "IE"),
    Airport(23, "twenty three", "UY"),
    Airport(24, "twenty four", "UY"),
    Airport(25, "twenty five", "MM"),
    Airport(26, "twenty six", "UY"),
    Airport(27, "twenty seven", "AR"),
    Airport(28, "twenty eight", "CO"),
    Airport(29, "twenty nine", "CO")
  )

  val runways = Seq(
    Runway(100, 10, "ASP", None),
    Runway(101, 20, "GRE", None),
    Runway(102, 21, "CON", None),
    Runway(103, 12, "ASP", Some("C")),
    Runway(104, 11, "GRE", None),
    Runway(105, 17, "CON", Some("A")),
    Runway(106, 19, "ASP", None),
    Runway(107, 18, "GRE", None),
    Runway(108, 22, "CON", None),
    Runway(109, 28, "ASP", Some("A")),
    Runway(110, 23, "GRE", None),
    Runway(111, 24, "CON", None),
    Runway(112, 25, "ASP", Some("B")),
    Runway(113, 26, "GRE", None),
    Runway(114, 27, "CON", None),
    Runway(115, 29, "ASP", Some("B")),
    Runway(116, 12, "GRE", None),
    Runway(117, 13, "CON", None),
    Runway(118, 15, "ASP", Some("A")),
    Runway(119, 15, "GRE", None),
    Runway(120, 14, "CON", None),
    Runway(121, 16, "ASP", None),
    Runway(122, 16, "GRE", None),
    Runway(123, 10, "CON", None)
  )

}
