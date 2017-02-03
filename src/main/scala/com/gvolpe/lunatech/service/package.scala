package com.gvolpe.lunatech

package object service {

  object FileReaderOps {

    implicit class StringListCSVParser(list: List[String]) {
      def asOption(at: Int): Option[String] = list.lift(at).map(_.fromCSV)
    }

    implicit class StringCSVParser(value: String) {
      def fromCSV: String = value.drop(1).dropRight(1) // Removing the quotes ""
    }

  }

}
