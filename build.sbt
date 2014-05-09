import BuildProperties._
import scala.io.Source
import java.io.FileWriter
import scala.util.parsing.json._

name := "aem-factory"

version := "1.0"

//Custom settings for AEM
val aemPort = SettingKey[String]("aemPort", "Load the AEM port number.")

val aemHost = SettingKey[String]("aemHost", "Load the AEM host address.")

val aemRole = SettingKey[String]("aemRole", "Load the AEM role (author or publish).")

val aemAdminUser = SettingKey[String]("aemAdminUser", "AEM Admin user")

val aemAdminPassword = SettingKey[String]("aemAdminPassword", "AEM Admin user")

val aemNewPassword = SettingKey[String]("aemNewPassword", "AEM Admin user")

val aemXmx = SettingKey[String]("aemXmx", "specify the maximun size [bytes] of the memory allocation pool")

val replicationHost = SettingKey[String]("replicationHost", "transport agent author to publish: publish instance host")

val replicationPort = SettingKey[String]("replicationPort", "transport agent author to publish: publish instance port")

val replicationUser = SettingKey[String]("replicationUser", "transport agent author to publish: publish instance user")

val replicationPassword = SettingKey[String]("replicationPassword", "transport agent author to publish: publish instance password")

val reverseRepHost = SettingKey[String]("reverseRepHost", "reverse replication agent on author: publish instance host")

val reverseRepPort = SettingKey[String]("reverseRepPort", "reverse replication agent on author: publish instance port")

val reverseRepUser = SettingKey[String]("reverseRepUser", "reverse replication agent on author: publish instance user")

val reverseRepPassword = SettingKey[String]("reverseRepPassword", "reverse replication agent on author: publish instance password")

val dispatcherFlushHost = SettingKey[String]("dispatcherFlushHost", "dispatcher flush agent on author: dispatcher host")

val dispatcherFlushPort = SettingKey[String]("dispatcherFlushPort", "dispatcher flush agent on author: dispatcher port")

val dispatcherFlushIsEnabled = SettingKey[String]("dispatcherFlushIsEnabled", "dispatcher flush agent on author: activation")

//Custom tasks for AEM management
val aemRun = TaskKey[Unit]("aemRun", "run aem instance on port without role")

val aemChangeAdminPassword = TaskKey[Unit]("aemChangeAdminPassword", "Change the admin password")

val aemDisableCrxDeWebDav = TaskKey[Unit]("aemDisableCrxDeWebDav", "Disable CRXDE Support and WebDav, restart the AEM instance")

val aemUninstallGeometrixxPackage = TaskKey[Unit]("aemUninstallGeometrixxPackage", "Uninstall Geometrixx packages")

val aemInit = TaskKey[Unit]("aemInit", "clean AEM instance")

val aemConfigFile = TaskKey[Unit]("aemConfigFile", "change the config file for launching the AEM instance")

val aemGenericInstance = TaskKey[Unit]("aemGenericInstance", "create an AEM generic instance")

val aemAuthorInstance = TaskKey[Unit]("aemAuthorInstance", "create an AEM author instance")

val aemPublishInstance = TaskKey[Unit]("aemPublishInstance", "create an AEM publish instance")

val aemConfigureAgentAuthorToPublish = TaskKey[Unit]("aemConfigureAgentAuthorToPublish", "configure the replication agent Author to Publish")

val aemConfigureReverseReplicationAgent = TaskKey[Unit]("aemConfigureReverseReplicationAgent", "configure the reverse replication agent on Author")

val aemConfigureFlushAgent = TaskKey[Unit]("aemConfigureFlushAgent", "configure the dispatcher flush agent on Author or Publish instance")

val aemOsgiSettingsPublish = TaskKey[Unit]("aemOsgiSettingsPublish", "Install the OSGi configuration package for Publish instance")

aemPort := propertyOption("aem.port").getOrElse("4502")

aemHost := propertyOption("aem.host").getOrElse("localhost")

aemRole := propertyOption("aem.role").getOrElse("author")

aemAdminUser := propertyOption("aem.adminUser").getOrElse("admin")

aemAdminPassword := propertyOption("aem.adminPasword").getOrElse("admin")

aemNewPassword := propertyOption("aem.newPassword").getOrElse("1234")

aemXmx := propertyOption("aem.Xmx").getOrElse("3072m")

replicationHost := propertyOption("replicationAgent.host").getOrElse("localhost")

replicationPort := propertyOption("replicationAgent.port").getOrElse("4503")

replicationUser := propertyOption("replicationAgent.transportUser").getOrElse("admin")

replicationPassword := propertyOption("replicationAgent.transportPassword").getOrElse("admin")

reverseRepHost := propertyOption("reverseReplicationAgent.host").getOrElse("localhost")

reverseRepPort := propertyOption("reverseReplicationAgent.port").getOrElse("4503")

reverseRepUser := propertyOption("reverseReplicationAgent.transportUser").getOrElse("admin")

reverseRepPassword := propertyOption("reverseReplicationAgent.transportPassword").getOrElse("admin")

dispatcherFlushHost := propertyOption("dispatcherFlush.host").getOrElse("localhost")

dispatcherFlushPort :=propertyOption("dispatcherFlush.port").getOrElse("80")

dispatcherFlushIsEnabled := propertyOption("dispatcherFlush.isEnabled").getOrElse("true")

aemRun := {
    println("[RUN] Starting aemRun task ...")
	startAemInstance
}

/** Changing the AEM admin password task */
aemChangeAdminPassword := {
    Process("curl -u " + aemAdminUser.value + ":" + aemAdminPassword.value + " -F rep:password='secret' -F :currentPassword='admin' http://" + aemHost.value + ":" + aemPort.value +"/home/users/a/admin.rw.html") !
}

/** AEM initialization task. Execute the command java -jar aem-file.jar -unpack */
aemInit := {
    println("[RUN] Starting aemIni task ...")
	deleteUnpackFiles(file("crx-quickstart"))
    if (!isFileExists(file("license.properties")))
        throw new IllegalStateException("license.properties file isn't found")
    if (!isFileExists(file("aem-quickstart-5.6.1.jar")))
		throw new IllegalStateException("aem-quickstart-5.6.1.jar isn't found.")
	val options = ForkOptions()
	val arguments = Seq("-jar", "aem-quickstart-5.6.1.jar", "-unpack")
	Fork.java(options, arguments)
}

/** Disable task for disabling the AEM CRXDE Support and WebDav. Required only on publish AEM instance */
aemDisableCrxDeWebDav := {
    println("[RUN] Starting aemDisableCrxDeWebDav task ...")
	//Disabling bundles
    if(aemRole.value == "author")
		throw new IllegalStateException("This command will be executed only for publish instance")
	crxdeDisable(aemAdminUser.value, aemAdminPassword.value, aemHost.value, aemPort.value)
    webDavDisable(aemAdminUser.value, aemAdminPassword.value, aemHost.value, aemPort.value)
}

/** Uninstall and delete all Geometrixx demo content and users */
aemUninstallGeometrixxPackage := {
	println("[RUN] Starting aemUninstallGeometrixxPackage task ...")
    uninstallPackage("etc/packages/day/cq561/product/", "cq-geometrixx-all-pkg-5.6.12.zip", aemAdminUser.value, aemAdminPassword.value, aemHost.value, aemPort.value)
	deletePackage("etc/packages/day/cq561/product/", "cq-geometrixx-all-pkg-5.6.12.zip", aemAdminUser.value, aemAdminPassword.value, aemHost.value, aemPort.value)
}

/** Modification task to adapt the start/quickstart file to launch the AEM instance */
aemConfigFile := {
	println("[RUN] Starting aemConfigFile task ...")
	modifyAemConfigFile("quickstart.bat", aemPort.value, aemRole.value, aemXmx.value)
	modifyAemConfigFile("quickstart", aemPort.value, aemRole.value, aemXmx.value)
	modifyAemConfigFile("start.bat", aemPort.value, aemRole.value, aemXmx.value)
	modifyAemConfigFile("start", aemPort.value, aemRole.value, aemXmx.value)
}

/** Generate an generic AEM instance (without role) */
aemGenericInstance := {
	println("[RUN] Starting AEM generic instance creation")
}

/** Generate an author AEM instance (with role author) */
aemAuthorInstance := {
	println("[RUN] Starting AEM author instance creation")
}

/** Generate a pubish AEM instance (with role publish) */
aemPublishInstance := {
	println("[RUN] Starting AEM publish instance creation")
}

/** Configure the replication agent author to publish */
aemConfigureAgentAuthorToPublish := {
	//Only valid for author instance
	if (aemRole.value == "author") {
		var jcr_path = "/etc/replication/agents.author/publish/jcr:content"
		//update the transportUser
		updateJCRvalue(aemAdminUser.value, aemAdminPassword.value, "transportUser", replicationUser.value, "http://" + aemHost.value + ":" + aemPort.value + jcr_path)
		//update the transportPassword
		updateJCRvalue(aemAdminUser.value, aemAdminPassword.value, "transportPassword", replicationPassword.value, "http://" + aemHost.value + ":" + aemPort.value + jcr_path)
		//update the transportUri
		var uri = "http://" + replicationHost.value  + ":" + replicationPort.value  +"/bin/receive?sling:authRequestLogin=1"
		updateJCRvalue(aemAdminUser.value, aemAdminPassword.value, "transportUri", uri, "http://" + aemHost.value + ":" + aemPort.value + jcr_path)
	} else {
		throw new IllegalStateException("The configuration the replication agent is only valid for AEM author instance.")
	}
}

/** Configure the reverse replication agent on author */
aemConfigureReverseReplicationAgent := {
	//Only valid for author instance
	if (aemRole.value == "author") {
		var jcr_path = "/etc/replication/agents.author/publish_reverse/jcr:content"
		//update the transportUser
		updateJCRvalue(aemAdminUser.value, aemAdminPassword.value, "transportUser", reverseRepUser.value, "http://" + aemHost.value + ":" + aemPort.value + jcr_path)
		//update the transportPassword
		updateJCRvalue(aemAdminUser.value, aemAdminPassword.value, "transportPassword", reverseRepPassword.value, "http://" + aemHost.value + ":" + aemPort.value + jcr_path)
		//update the transportUri
		var uri = "http://" + reverseRepHost.value  + ":" + reverseRepPort.value  +"/bin/receive?sling:authRequestLogin=1"
		updateJCRvalue(aemAdminUser.value, aemAdminPassword.value, "transportUri", uri, "http://" + aemHost.value + ":" + aemPort.value + jcr_path)
	} else {
		throw new IllegalStateException("The configuration the reverse replication agent is only valid for AEM author instance.")
	}
}

/** Configure the flush agent on author or publish */
aemConfigureFlushAgent := {
	var jcr_path = "/etc/replication/agents.author/flush/jcr:content"
	if (aemRole.value == "publish")
		jcr_path = "/etc/replication/agents.publish/flush/jcr:content"
	//agent activation
	updateJCRvalue(aemAdminUser.value, aemAdminPassword.value, "enabled", "true", "http://" + aemHost.value + ":" + aemPort.value + jcr_path)
	//update the transportUri
	var uri = "http://" + dispatcherFlushHost.value  + ":" + dispatcherFlushPort.value  +"/dispatcher/invalidate.cache"
	updateJCRvalue(aemAdminUser.value, aemAdminPassword.value, "transportUri", uri, "http://" + aemHost.value + ":" + aemPort.value + jcr_path)
}

/** OSGi security package for publish isntance */
aemOsgiSettingsPublish := {
	if(aemRole.value == "author")
		throw new IllegalStateException("This command will be executed only for publish instance")
	uploadPackage("package/production-osgi-configuration.zip", aemAdminUser.value, aemAdminPassword.value, aemHost.value, aemPort.value)
	installPackage("etc/packages/config/", "production-osgi-configuration.zip", aemAdminUser.value, aemAdminPassword.value, aemHost.value, aemPort.value)
}

//Function definition
// Change the JCR property node or parameter
//
// @param host: host of the aem instance
// @param port: port of the aem instance
// @param user: admin username
// @param password: admin password
// @param parameter: property name to change
// @param value: new value of the property (@param parameter)
//
def updateJCRvalue(user:String, password:String, parameter:String, value:String, url:String) = {
	Process("curl -u " + user + ":" + password + " -X POST -F" + parameter + "=" + value + " " + url) !
}

// Check the status of a bundle given by the AEM status-Bundlelist (process line by line)
//
// @param line: a line from the AEM status-BundleList
// @return true if the bundle status equals to "active". If not return false
//
def isBundleActiveStatus(line:String) : Boolean = {
	var isActivated = false
	if (line contains "active")
		isActivated = true
	isActivated
}

// Replace some keys parameters for the aem configuration file (used for start/quickstart)
//
// @param target: filname for the target config file
// @param cqPort: port of the aem instance need to run
// @param cqRole: aem instance role (author/publish)
// @param cqXmx: maximum size [bytes] of the memory allocation pool
//
//
def modifyAemConfigFile(target:String, cqPort:String, cqRole:String, cqXmx:String) = {
	if (isFileExists(file(target)))
        IO.delete (file(target))
	Source.fromFile("crx-quickstart/bin/" + target)
		.getLines
		.map { line =>
			line.replace("CQ_PORT=4502", "CQ_PORT=" + cqPort)
		}
		.map { line =>
			line.replace(":: set CQ_RUNMODE=", "set CQ_RUNMODE=" + cqRole)
		}
		.map { line =>
			line.replace("#CQ_RUNMODE=''", "CQ_RUNMODE='" + cqRole + "'")
		}
		.map { line =>
			line.replace("CQ_RUNMODE=author", "set CQ_RUNMODE=" + cqRole)
		}
		.map { line =>
			line.replace("CQ_RUNMODE='author'", "CQ_RUNMODE='" + cqRole + "'")
		}
		.map { line =>
			line.replace("-Xmx1024m", cqXmx)
		}
		.foreach(writeConfigFile(_,target))
	IO.copyFile(file(target), file("crx-quickstart/bin/" + target))
	IO.delete (file(target))
}

// Write line on a target file
//
// @param line: line need to be inserted
// @param target: target filename
//
//
def writeConfigFile(line:String, target:String) = {
	val fw = new FileWriter(target, true)
	fw.write(line+"\r\n")
	fw.close()
}

// Upload (force) an AEM package
//
// @param pkg: package name (zip file)
// @param user: aem admin username
// @param password: aem admin password
// @param host: host of the aem instance
// @param port: port of the aem instance
//
//
def uploadPackage(pkg:String, user:String, password:String, host:String, port:String) = {
	Process("curl -u " + user + ":" + password + " -F package=@" + pkg + " http://" + host + ":" + port +"/crx/packmgr/service/.json/?cmd=upload") !
}

// Install an AEM package
//
// @param jcrPath: package path
// @param pkg: package name
// @param user: aem admin username
// @param password: aem admin password
// @param host: host of the aem instance
// @param port: port of the aem instance
//
//
def installPackage(jcrPath:String, pkg:String, user:String, password:String, host:String, port:String) = {
    Process("curl -u " + user + ":" + password + " -X POST http://" + host + ":" + port +"/crx/packmgr/service/.json/" + jcrPath + pkg + "?cmd=install") !
}

// Delete an AEM package
//
// @param jcrPath: package path
// @param pkg: package name
// @param user: aem admin username
// @param password: aem admin password
// @param host: host of the aem instance
// @param port: port of the aem instance
//
//
def deletePackage(jcrPath:String, pkg:String, user:String, password:String, host:String, port:String) = {
    Process("curl -u " + user + ":" + password + " -X POST http://" + host + ":" + port +"/crx/packmgr/service/.json/" + jcrPath + pkg + "?cmd=delete") !
}

// Uninstall an AEM package
//
// @param jcrPath: package path
// @param pkg: package name
// @param user: aem admin username
// @param password: aem admin password
// @param host: host of the aem instance
// @param port: port of the aem instance
//
//
def uninstallPackage(jcrPath:String, pkg:String, user:String, password:String, host:String, port:String) = {
    Process("curl -u " + user + ":" + password + " -X POST http://" + host + ":" + port +"/crx/packmgr/service/.json/" + jcrPath + pkg + "?cmd=uninstall") !
}

// Delete the crx-quickstart folder. This folder is created with the java command: java -jar file.jar -unpack
//
// @param target: file or folder path to delete
//
//
def deleteUnpackFiles(target: File) = {
    if (target.exists)
        IO.delete (target)
}

// Check if a folder or a file exists
//
// @param target: file or folder path to search/or the check
// @return true if the folder/file exists
//
//
def isFileExists(target: File) : Boolean = {
    if(target.exists)
        true
    else
        false
}

// Disable the CRXDE Support (normally on aem publish instance)
//
// @param user: aem admin username
// @param password: aem admin password
// @param host: host of the aem instance
// @param port: port of the aem instance
//
//
def crxdeDisable(user:String, password:String, host:String, port:String) = {
    bundleManagement(user, password, host, port, "stop", "com.day.crx.crxde-support")
}

// Disable the WebDav (normally on aem publish instance)
//
// @param user: aem admin username
// @param password: aem admin password
// @param host: host of the aem instance
// @param port: port of the aem instance
//
//
def webDavDisable(user:String, password:String, host:String, port:String) = {
    bundleManagement(user, password, host, port, "stop", "org.apache.sling.jcr.webdav")
}

// Manage an aem bundle
//
// @param user: aem admin username
// @param password: aem admin password
// @param host: host of the aem instance
// @param port: port of the aem instance
// @param action: start, stop, update, refresh, uninstall, install
// @param symbolicName: symbolic name of the bundle
//
//
def bundleManagement(user:String, password:String, host:String, port:String, action:String, symbolicName:String) = {
	Process("curl -u " + user + ":" + password + " -F action=" + action + " http://" + host + ":" + port + "/system/console/bundles/$bundle/" + symbolicName) !
}

// Starting the AEM instance. Run the script quickstart.bat (win)/quickstart (mac/linux)
//
def startAemInstance = {
    if (getOsType == "win"){
		if (!isFileExists(file("crx-quickstart/bin/quickstart.bat")))
			throw new IllegalStateException("crx-quickstart/bin/quickstart.bat isn't found")
		Process("./crx-quickstart/bin/quickstart.bat") !
	}
	else if (getOsType == "mac" || getOsType == "linux"){
		if (!isFileExists(file("crx-quickstart/bin/quicksart")))
			throw new IllegalStateException("crx-quickstart/bin/quicksart isn't found")
		Process("./crx-quickstart/bin/quicksart") !
	}
	else
		throw new IllegalStateException("Please check the script for starting the aem instance")
}

// Stopping the AEM instance. Run the script stop.bat (win)/stop (mac/linux)
//
def stopAemInstance = {
	if (getOsType == "win")
		if (!isFileExists(file("crx-quickstart/bin/stop.bat"))){
			throw new IllegalStateException("crx-quickstart/bin/stop.bat isn't found")
		Process("./crx-quickstart/bin/stop.bat") !
	}
	else if (getOsType == "mac" || getOsType == "linux"){
		if (!isFileExists(file("crx-quickstart/bin/stop")))
			throw new IllegalStateException("crx-quickstart/bin/stop isn't found")
		Process("./crx-quickstart/bin/stop") !
	}
	else
		throw new IllegalStateException("Please check the script for stoping the aem instance")
}

// Return the OS name
//
// @return the os name. "win" for windows, "mac" for mac os, "linux" for linux distribution and "none"
//         the script doesn't match the OS.
//
//
def getOsType : String = {
	var typ = "none"
	if (System.getProperty("os.name").toLowerCase() contains "windows" )
		typ = "win"
	else if (System.getProperty("os.name").toLowerCase() contains "mac")
		typ = "mac"
	else if (System.getProperty("os.name").toLowerCase() contains "linux")
		typ = "linux"
	typ
}
