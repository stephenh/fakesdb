
# Returns scala version (from settings or else from $SCALA_HOME)
def scala_version
  Buildr.settings.build['scala.version'] || begin
    fail "Unable to infer scala version" unless ENV["SCALA_HOME"] =~ /scala-([^\/]*)/
    version = Buildr.settings.build['scala.version'] = $1
    puts "Inferred Scala version #{version}"
    return version
  end
end

# Project-specific extensions
module FakeSDB
  extend self

  # Return fakesdb artifact name embedding scala version, with optional suffix
  # e.g. fakesdb               => "fakesdb_2.9.1"
  #      fakesdb("standalone") => "fakesdb-standalone_2.9.1"
  def fakesdb(suffix = nil)
    short_scala_version = (scala_version =~ /^(\d+)\.(\d+).*/) ? "#{$1}.#{$2}" : fail # strip .RC suffix (if any)
    "fakesdb#{suffix ? "-" + suffix : ""}_#{short_scala_version}"
  end

  # Create all-in-one jar by merging dependencies from `lib` using prefixes
  #
  # e.g.  all_in_one_jar :id => "standlone", :libs => ["aws-java-sdk", "jetty", ...]
  #
  def all_in_one_jar(options)
    name = options[:id] || fail("Missing :id")
    libs = options[:libs] || fail("Missing :id")
    package(:jar, :id => fakesdb(name)).tap do |pkg|
      pkg.enhance [task(:retrieve)]
      # double-enhance so merge happens after base jar is created
      pkg.enhance do
        pkg.enhance do
          libs.each do |prefix|
            Dir[_("lib") + "/#{prefix}-*.jar"].each do |dep|
              fast_merge_jar pkg, dep if dep !~ /-sources/
            end
          end
        end
      end
    end
  end

  # Merge jars using `jar` tool since rubyzip is kinda slow
  #
  # e.g.  fast_merge_jar("my-super.jar, "some-dependency.jar")
  #
  def fast_merge_jar(target_jar, merge_jar)
    info "merging #{merge_jar}"
    tmp = _(:target, "tmp")
    sh "rm -rf #{tmp}"
    sh "mkdir -p #{tmp}; cd #{tmp}; jar -xf #{merge_jar}; jar -uf #{target_jar} *"
    sh "rm -rf #{tmp}"
  end
end


