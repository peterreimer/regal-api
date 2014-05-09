import com.github.play2war.plugin._

name := "regal-api"

version := "0.5.0-SNAPSHOT"

Play2WarPlugin.play2WarSettings

Play2WarKeys.servletVersion := "2.5"

libraryDependencies ++= Seq(
  cache,
  "de.nrw.hbz.regal" % "regal-archive" % "0.5.0-SNAPSHOT" ,
  "de.nrw.hbz.regal" % "regal-mabconverter" % "0.5.0-SNAPSHOT" ,
  "org.marc4j" % "marc4j" % "2.4", 
  "junit" % "junit" % "4.10", 
  "org.lobid" % "lodmill-rd" % "regal-0.1.0", 
  "org.apache.pdfbox" % "pdfbox" % "1.8.0",
  "org.bouncycastle" % "bcprov-jdk15" % "1.44",
  "org.bouncycastle" % "bcmail-jdk15" % "1.44", 
  "com.ibm.icu" % "icu4j" % "3.8",
  "com.itextpdf" % "itextpdf" % "5.4.1", 
  "org.antlr" % "antlr4" % "4.0", 
  "ch.qos.logback" % "logback-core" % "0.9.30", 
  "ch.qos.logback" % "logback-classic" % "0.9.30", 
  "org.slf4j" % "slf4j-api" % "1.6.2", 
  "commons-io" % "commons-io" % "2.4",
  "org.codehaus.jackson" % "jackson-core-lgpl" % "1.9.13",
  "org.codehaus.jackson" % "jackson-mapper-lgpl" % "1.9.13" ,
  "com.github.jsonld-java" % "jsonld-java" % "0.3",
  "com.sun.jersey" % "jersey-core" % "1.17.1" ,
  "org.openrdf.sesame" % "sesame-repository-api" % "2.7.10" ,
  "org.openrdf.sesame" % "sesame-core" % "2.7.10",
  "org.openrdf.sesame" % "sesame-rio" % "2.7.10",
  "org.openrdf.sesame" % "sesame-sail" % "2.7.10",
  "org.openrdf.sesame" % "sesame" % "2.7.10",
  "org.openrdf.sesame" % "sesame-http-client" % "2.7.10",
  "org.openrdf.sesame" % "sesame-rio-ntriples" % "2.7.10",
  "org.openrdf.sesame" % "sesame-rio-api" % "2.7.10",
  "org.openrdf.sesame" % "sesame-rio-rdfxml" % "2.7.10",
  "org.openrdf.sesame" % "sesame-rio-n3" % "2.7.10",
  "org.openrdf.sesame" % "sesame-rio-turtle" % "2.7.10",
  "org.openrdf.sesame" % "sesame-queryresultio-api" % "2.7.10",
  "org.openrdf.sesame" % "sesame-queryresultio" % "2.7.10",
  "org.openrdf.sesame" % "sesame-query" % "2.7.10",
  "org.openrdf.sesame" % "sesame-model" % "2.7.10",
  "org.openrdf.sesame" % "sesame-http-protocol" % "2.7.10",
  "org.openrdf.sesame" % "sesame-http" % "2.7.10",
  "org.openrdf.sesame" % "sesame-repository-sail" % "2.7.10",
  "org.openrdf.sesame" % "sesame-sail-memory" % "2.7.10",
  "org.openrdf.sesame" % "sesame-sail-nativerdf" % "2.7.10",
  "com.wordnik" %% "swagger-play2" % "1.3.5"
)

play.Project.playJavaSettings

resolvers := Seq("hypnoticocelot" at "https://oss.sonatype.org/content/repositories/releases/", "aduna" at "http://maven.ontotext.com/content/repositories/aduna/" ,
Resolver.mavenLocal,"Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/","Play2war plugins release" at "http://repository-play-war.forge.cloudbees.com/release/"
)

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")