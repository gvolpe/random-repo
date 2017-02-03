package com.gvolpe.lunatech.service

import java.io.{File => JFile}
import java.nio.file.Paths

import com.gvolpe.lunatech.FS2Utils._
import com.gvolpe.lunatech.model.{Airport, Country, Runway}
import fs2.{Strategy, Task, io, text}
import FileReaderOps._

object FileReaderService {

  private lazy val currentDir = new JFile(".").getAbsolutePath.dropRight(1) + "resources"

  def readCountries(implicit S: Strategy): StreamT[Country] =
    readingFlow(s"$currentDir/countries.csv")
      .map(_.take(3))
      .map(l => Country(l.head.toLong, l(1).fromCSV, l(2).fromCSV))

  def readAirports(implicit S: Strategy): StreamT[Airport] =
    readingFlow(s"$currentDir/airports.csv")
      .map(_.take(9))
      .map(l => Airport(l.head.toLong, l(3).fromCSV, l(8).fromCSV))

  def readRunways(implicit S: Strategy): StreamT[Runway] =
    readingFlow(s"$currentDir/runways.csv")
      .map(_.take(9))
      .map(l => Runway(l.head.toLong, l(1).toLong, l(5).fromCSV, l.asOption(8)))

  private def readingFlow(filename: String)(implicit S: Strategy): StreamT[List[String]] =
    io.file.readAll[Task](Paths.get(filename), 4096)
      .through(text.utf8Decode)
      .through(text.lines)
      .filter(_.nonEmpty)
      .drop(1)
      .map(_.split(",").toList)

}
