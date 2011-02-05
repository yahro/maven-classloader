include Java
require "../target/maven-classloader-1.0-SNAPSHOT.jar"
include_class Java::com.bigfatgun.MavenClassLoader

@cl = MavenClassLoader.forGAV("joda-time:joda-time:1.6.2");

def dateTime()
  newInstanceOf("org.joda.time.DateTime")
end

def newInstanceOf(clsName)
  @cl.loadClass(clsName).newInstance()
end

# ruby date
ruby_now = Time.now
joda_now = dateTime()

puts "Ruby says it is #{ruby_now} and joda-time says it is #{joda_now}"
