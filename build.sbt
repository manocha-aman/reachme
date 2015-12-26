name := "reachme"

version := "1.0.0-SNAPSHOT"

organization := "am.reachme"

scalaVersion := "2.11.7"

resolvers ++= Seq(
  "snapshots"           at "http://oss.sonatype.org/content/repositories/snapshots",
  "releases"            at "http://oss.sonatype.org/content/repositories/releases",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
)

seq(Revolver.settings: _*)

scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation", "-encoding", "utf8")

javaOptions := Seq("-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000")

parallelExecution in Test := false

libraryDependencies ++= {
  val sprayVersion = "1.3.1"
  val akkaVersion = "2.4-SNAPSHOT"
  Seq(
  	"org.iq80.leveldb" 		  %    "leveldb" % "0.7",
  	"org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",
 	"commons-validator" % "commons-validator" % "1.4.0",
    "org.slf4j"               %   "slf4j-api"       % "1.7.7",
    "ch.qos.logback"          %   "logback-core"    % "1.1.2",
    "ch.qos.logback"          %   "logback-classic" % "1.1.2",
    "org.json4s"              %%  "json4s-native"   % "3.2.10",
    "joda-time"               %   "joda-time"       % "2.4",
    "org.joda"                %   "joda-convert"    % "1.7",
    "com.typesafe.akka"       %%  "akka-actor"      % akkaVersion,
    "com.typesafe.akka"       %%  "akka-slf4j"      % akkaVersion,
    "com.typesafe.akka"       %%  "akka-testkit"    % akkaVersion % "test",
    "com.typesafe.akka" % "akka-stream-experimental_2.11" % "1.0-M4",
    "com.typesafe.akka"       %% "akka-persistence" % akkaVersion,
    "com.typesafe.akka" 	  %% "akka-persistence-query-experimental" % akkaVersion,
    "org.scalatest"           %%  "scalatest"       % "2.2.1" % "test",
    "com.github.t3hnar"       %%  "scala-bcrypt"    % "2.4"
  )
}
