package am.reachme.service;

import scala.concurrent.duration._
import akka.persistence.query.PersistenceQuery
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.persistence.query.scaladsl.AllPersistenceIdsQuery
import akka.stream.ActorMaterializer
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FlatSpec

object AllPersistenceIdsSpec {
  val config = """
    akka.loglevel = INFO
    akka.persistence.journal.plugin = "akka.persistence.journal.leveldb"
    akka.persistence.journal.leveldb.dir = "target/journal-AllPersistenceIdsSpec"
    akka.test.single-expect-default = 10s
    """
}

class AllPersistenceIdsSpec extends FlatSpec with BeforeAndAfterAll {
}
