import java.io.FileInputStream
import java.util.Properties

object BuildProperties {
  def readProperties(fileName: String): Option[Properties] = {
    try {
      val prop = new Properties()
      prop.load(new FileInputStream("config/"+fileName))
      println("Using properties from: "+fileName)
      Some(prop)
    }catch{
      case e: Exception =>
        //println("Exception reading: "+fileName + ": " + e.getMessage)
        None
    }
  }

  val properties = readProperties("aem.properties")

  //if you want to set the defaults in this file...
  val projectNameProperty: String = properties.flatMap( props => Option(props.getProperty("project.name"))).getOrElse("myproject")

  //if you want to set the defaults in .sbt file...
  def propertyOption(key: String): Option[String] = properties.flatMap(props => Option(props.getProperty(key)))
}
