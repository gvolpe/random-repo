package com.gvolpe.lunatech

import com.gvolpe.lunatech.service.{AirportService, FileReaderService}
import fs2.Stream

object FileIndexingManager {

  implicit val S = fs2.Strategy.fromFixedDaemonPool(4, "file-indexing-manager")

  private def indexProcess = fs2.concurrent.join(3)(
    Stream(
      FileReaderService.readCountries  to AirportService.indexCountry,
      FileReaderService.readAirports   to AirportService.indexAirport,
      FileReaderService.readRunways    to AirportService.indexRunway
    )
  )

  def run() = {
    println(">>>> Starting indexing of files in memory <<<<")
    indexProcess.run.unsafeRun()
    println(">>>> Index Finished <<<<")
  }

}
