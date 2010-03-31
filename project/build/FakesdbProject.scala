import sbt._

class TestProject(info: ProjectInfo) extends DefaultWebProject(info) {
  override def compileClasspath = super.compileClasspath +++ managedClasspath(config("buildtime"))
}
