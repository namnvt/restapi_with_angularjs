resolvers ++= Seq(
  "sonatype releases" at "http://oss.sonatype.org/content/repositories/releases",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
)

addSbtPlugin("org.scalikejdbc"   % "scalikejdbc-mapper-generator" % "2.2.1")
addSbtPlugin("com.typesafe.play" % "sbt-plugin"                   % "2.3.9")
addSbtPlugin("com.typesafe.sbt"  % "sbt-coffeescript"             % "1.0.0")
addSbtPlugin("com.timushev.sbt"  % "sbt-updates"                  % "0.1.7")

