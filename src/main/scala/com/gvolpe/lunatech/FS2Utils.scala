package com.gvolpe.lunatech

import fs2._
import fs2.async.mutable.{Queue, Signal}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scalaz.{-\/, \/-}
import scalaz.concurrent.{Task => Tazk}

object FS2Utils {

  type StreamT[A]   = Stream[Task, A]
  type PipeT[A, B]  = Pipe[Task, A, B]
  type SinkT[A]     = Sink[Task, A]
  type SignalT[A]   = Signal[Task, A]
  type QueueT[A]    = Queue[Task, A]
  type TopicT[A]    = Queue[Task, A]

  def loggerSink[A] = liftSink[A](t => Task.delay(println(t)))

  def liftSink[A](f: A => Task[Unit]): SinkT[A] = liftPipe[A, Unit](f)

  def liftPipe[A, B](f: A => Task[B]): PipeT[A, B] = _.evalMap (f(_))

  implicit class FutureOps[T](f: => Future[T]) {

    def asTask(implicit S: Strategy): Task[T] = Task.async { cb =>
      f.onComplete {
        case Success(value) => cb(Right(value))
        case Failure(err)   => cb(Left(err))
      }
    }

    def asScalazTask: Tazk[T] = Tazk.async { cb =>
      f.onComplete {
        case Success(value) => cb(\/-(value))
        case Failure(err)   => cb(-\/(err))
      }
    }

  }



}
