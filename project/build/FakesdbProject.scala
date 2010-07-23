import sbt._

class FakesdbProject(info: ProjectInfo) extends DefaultWebProject(info) {
  override def compileClasspath = super.compileClasspath +++ managedClasspath(config("buildtime"))
}
