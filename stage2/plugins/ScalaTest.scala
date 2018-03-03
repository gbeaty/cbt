package cbt
import java.io.File

trait TestBuild extends BaseBuild {
  def mainDirectory = projectDirectory.getParentFile
  override def dependencies: Seq[Dependency] = Seq( DirectoryDependency( mainDirectory ) )
}

trait ScalaTestBuild extends TestBuild {
  def scalaTestDependency = Resolver( mavenCentral ).bind( ScalaDependency( "org.scalatest", "scalatest", if ( scalaMajorVersion == "2.12" ) "3.0.1" else "2.2.6" ) )
  override def dependencies: Seq[Dependency] = ( super.dependencies :+ libraries.cbt.scalatestRunner ) ++ scalaTestDependency
  override def run: ExitCode = {
    classLoader.loadClass( "cbt.scalatest.Runner" ).method(
      "run", classOf[Array[File]], classOf[ClassLoader], classOf[Array[String]]
    ).invoke( null, exportedClasspath.files.toArray, classLoader, context.args.toArray )
    ExitCode.Success
  }
}
