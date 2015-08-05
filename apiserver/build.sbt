lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .enablePlugins(SbtWeb)
  .settings(
    name := """apiserver""",
    version := "0.1",
    scalaVersion := "2.11.6",
    resolvers += "sonatype releases" at "http://oss.sonatype.org/content/repositories/releases",
    libraryDependencies ++= Seq(
      "org.scalikejdbc"      %% "scalikejdbc"                     % scalikejdbcVersion,
      "org.scalikejdbc"      %% "scalikejdbc-config"              % scalikejdbcVersion,
      "org.scalikejdbc"      %% "scalikejdbc-play-plugin"         % scalikejdbcPlayVersion,
      "org.scalikejdbc"      %% "scalikejdbc-play-fixture-plugin" % scalikejdbcPlayVersion,
      "mysql"                % "mysql-connector-java"             % "5.1.32",
      "org.hibernate"        %  "hibernate-core"                  % "4.3.7.Final",
      "org.json4s"           %% "json4s-ext"                      % "3.2.11",
      "com.github.tototoshi" %% "play-json4s-native"              % "0.3.0",
      "com.github.tototoshi" %% "play-flyway"                     % "1.2.0",
      "org.scalikejdbc"      %% "scalikejdbc-test"                % scalikejdbcVersion  % "test",
      "com.typesafe.akka"    %% "akka-actor"                      % akkaPlayVersion,
      "com.typesafe.akka"    %% "akka-testkit"                    % akkaPlayVersion % "test",
      "com.enragedginger"    %% "akka-quartz-scheduler"           % "1.3.0-akka-2.3.x",
      jdbc,
      cache,
      ws
    ),
    initialCommands := """
      import scalikejdbc._, config._
      import models._, utils._
      DBs.setupAll
      DBInitializer.run()
      implicit val autoSession = AutoSession
      val (p, c, s, ps, sc) = (Programmer.syntax("p"), Company.syntax("c"), Skill.syntax("s"), ProgrammerSkill.syntax("ps"), Subscriber.syntax("sc"))
    """
  )
  .settings(scalikejdbcSettings:_*) // http://scalikejdbc.org/documentation/setup.html

lazy val scalikejdbcVersion = "2.2.+"
lazy val scalikejdbcPlayVersion = "2.3.+"
lazy val akkaPlayVersion = "2.3.+"


