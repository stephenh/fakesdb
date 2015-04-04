Buildr.settings.build['scala.version'] = '2.10.4'

require 'buildr/ivy_extension'
require 'buildr/scala'
require './fakesdb.rb'

VERSION_NUMBER = ENV['version'] || 'SNAPSHOT'

# to resolve the ${version} in the ivy.xml
Java.java.lang.System.setProperty("version", VERSION_NUMBER)

repositories.remote << "http://mirrors.ibiblio.org/maven2"
repositories.release_to = 'sftp://joist.ws/var/www/joist.repo'
repositories.release_to[:permissions] = 0644

define FakeSDB::fakesdb do
  extend FakeSDB # project-specific extensions

  project.version = VERSION_NUMBER
  project.group = 'com.bizo'
  ivy.compile_conf(['servlet', 'war', 'buildtime']).test_conf('test')

  test.using :junit

  task "retrieve" do
    ivy.ivy4r.retrieve
  end

  package_with_sources

  file 'target/pom.xml' => task('ivy:makepom')
  package(:jar).pom.tap do |pom|
    pom.from 'target/pom.xml'
  end

  all_in_one_jar :id   => "standalone",
                 :libs => ["aws-java-sdk", "jetty", "servlet-api", "scala-library", "scala-reflect"]

  # without scala-library
  all_in_one_jar :id => "testing",
                 :libs => ["aws-java-sdk", "jetty", "servlet-api"]

  # fakesdb-servlet is just fakesdb so you can set it up with your own web.xml
  package(:jar, :id => fakesdb("servlet")).tap do |pkg|
    pkg.exclude Dir[_(:target, :classes, "fakesdb") + "/Jetty*"]
  end
end

