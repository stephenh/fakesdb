
require 'buildr/ivy_extension'
require 'buildr/scala'

VERSION_NUMBER = ENV['version'] || 'SNAPSHOT'

repositories.remote << "http://mirrors.ibiblio.org/maven2"
repositories.release_to = 'sftp://joist.ws/var/www/joist.repo'
repositories.release_to[:permissions] = 0644

# to resolve the ${version} in the ivy.xml
Java.java.lang.System.setProperty("version", VERSION_NUMBER)

Buildr.settings.build['scala.version'] = '2.9.1'

define "fakesdb" do
  project.version = VERSION_NUMBER
  project.group = 'com.bizo'
  ivy.compile_conf(['servlet', 'war', 'buildtime']).test_conf('test')

  package_with_sources

  package(:jar).pom.tap do |pom|
    pom.enhance [task('ivy:makepom')]
    pom.from 'target/pom.xml'
  end

  test.using :junit

  task "retrieve" do
    ivy.ivy4r.retrieve
  end
end

