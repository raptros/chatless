resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

resolvers += "jgit-repo" at "http://download.eclipse.org/jgit/maven"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.4.0")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.6.2")
