package cbt.scalatest

import org.scalatest._

import java.io.File

import scala.util.matching._

object Runner {
  def run( classpath: Array[File], classLoader: ClassLoader, args: Array[String] ): Unit = {
    val suiteNames = classpath.map( d => discoverSuites( d, classLoader, args ) ).flatten
    runSuites( suiteNames.map( loadSuite( _, classLoader ) ) )
  }

  def runSuites( suites: Seq[Suite] ) = {
    def color: Boolean = true
    def durations: Boolean = true
    def shortstacks: Boolean = true
    def fullstacks: Boolean = true
    def stats: Boolean = true
    def testName: String = null
    def configMap: ConfigMap = ConfigMap.empty
    suites.foreach {
      _.execute( testName, configMap, color, durations, shortstacks, fullstacks, stats )
    }
  }

  def discoverSuites( discoveryPath: File, classLoader: ClassLoader, args: Array[String] ): Seq[String] = {
    val set = classLoader
      .loadClass( "org.scalatest.tools.SuiteDiscoveryHelper" )
      .getMethod( "discoverSuiteNames", classOf[List[_]], classOf[ClassLoader], classOf[Option[_]] )
      .invoke( null, List( discoveryPath.toString ++ "/" ), classLoader, None )
      .asInstanceOf[Set[String]]

    val filtered = if ( args.size == 0 ) {
      set
    } else {
      val patterns = args.map { arg =>
        val res = ( "^" + Regex.quote( arg ).replace( "-", "\\E.*\\Q" ) + "$" ).r // '-' is the wildcard character, because bash globs '*'.
        println( res )
        res
      }
      set.filter { suite =>
        patterns.exists { pattern =>
          pattern.findFirstIn( suite ).isDefined
        }
      }
    }

    println( filtered )

    filtered.toVector
  }
  def loadSuite( name: String, classLoader: ClassLoader ) = {
    classLoader.loadClass( name ).getConstructor().newInstance().asInstanceOf[Suite]
  }
}
