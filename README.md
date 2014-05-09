CQ Tasks Automation
================

## Introduction

This is a SBT script to automatize the most popular processes to configure an AEM Instance (CQ5). This script can configure bot instances (author and publish).

## Features

With this script you can:
* Start an AEM instance (unpacking, configure files and starting your instance)
* Uninstall the Geometrixx demo content
* Disabling the CRXDE support
* Disabling the WebDav
* Configuring the replication agent from author to publish instance
* Configuring the reverse replication agent
* Configuring the dispatcher flush agent
* Configuring the OSGi setting for a production instance
* Changing the admin password

## Requirements
The requirements for the use of this script, you need to install:

* the AEM quickstart java archive (v5.6.1)
* [Maven 2](http://maven.apache.org/)
* [SBT](http://www.scala-sbt.org/)
* [curl](http://curl.haxx.se/)

## Processes
### Starting an AEM instance
The following process will configure/starting your instance by defining a role, a host and a port.

1. modify the configuration file `config/eam.properties` to define the instance parameter (host, port, role and admin user information)
2. run the sbt console with `sbt`
3. run the command `aemInit` and `aemConfigFile`  to unpack, configure the instance parameter file
4. run the command `aemRun` to start the AEM instance

### Configuring an AEM instance
Before starting the following process, be sure you have modified the `config/aem.properties` configuration file and your instance is running.

1. run the sbt console with `sbt`
2. run the command `aemUninstallGeometrixxPackage` for deleting all Geometrixx demo content and user.
3. run the command `aemDisableCrxDeWebDav` for disabling the CRXDE Support and WebDav on the publish AEM instance
4. run the command `aemConfigureAgentAuthorToPublish` for configuring the replication agent from author to publish AEM instance (on author instance only)
5. run the command `aemConfigureReverseReplicationAgent` for configuring the reverse replication agent on author AEM instance (on author instance only)
6. run the command `aemConfigureFlushAgent` for configuring the dispatcher flush agent on author or publish AEM instance
7. run the command `aemOsgiSettingsPublish` for configuring the OSGi settings for publish instance
8. run the command `aemChangeAdminPassword` for changing the admin password

## SBT tasks description
All tasks for managing AEM instance are describe with this list:

### aemInit

Clean crx-quickstart folder and unpack aem-quickstart-5.6.1.jar (the aem quickstart jar file must be present)

** Process:**

1. check if the `license.properties` and the `aem-quickstart-5.6.1.jar` files are existing
2. deleting the `crx-quickstart` folder to clean the last installation
3. execute the command: `java -jar aem-quickstart-5.6.1.jar -unpack`

### aemRun

Launch the AEM instance with the default properties / these properties as describe on the file crx-quickstart/bin/quickstart

**Process:**

1. find the os type (windows, mac os or linux distribution)
2. running the `bin/quickstart.bat` or `bin/quickstart` file to launch the AEM instance

### aemChangeAdminPassword

Change the password for admin user

**Process:**

1. run the command 
> `curl -u {user}:{actualPassword} -F rep:password={newPaswsord} -F :currentPassword={actualPassword} http://{host}:{port}/home/users/a/admin.rw.html`

* {user}: username for the admin user
* {actualPassword}: actual password for the admin user
* {newPassword}: new password
* {host}: host of the AEM instance
* {port}: port of the AEM instance

### aemUninstallGeometrixxPackage

Uninstall and deleting all Geometrixx example content and users

**Process:**

1. uninstall the package named `cq-geometrixx-all-pkg-5.6.12.zip` located with the JCR path `etc/packages/day/cq561/product`
> `curl -u {user}:{password} -X POST http://{host}:{port}/crx/packmgr/service/.json/{jcr_path}?cmd={command}`

* {user}: username for the admin user
* {password}: password
* {host}: host of the AEM instance
* {port}: port of the AEM instance
* {jcr_path}: path and the filename of the package
* {command}: the API command to uninstall the package, in this case, uninstall

2. delete the package name describe in 1)
> `curl -u {user}:{password} -X POST http://{host}:{port}/crx/packmgr/service/.json/{jcr_path}?cmd={delete}`

* {user}: username for the admin user
* {password}: password
* {host}: host of the AEM instance
* {port}: port of the AEM instance
* {jcr_path}: path and the filename of the package
* {command}: describes command to delete the package, in this case, delete

### aemDisableCrxDeWebDav

Disable CRXDE Support and WebDav bundles on publish instance only

**Process:**

1. disabling the bundle named `com.day.crx.crxde-support`
2. disabling the bundle named `org.apache.sling.jcr.webdav`
For disabling a bundle with curl, you can execute this command:
> `curl -u {user}:{password} -F action={action} http://{host}:{port}/system/console/bundles/$bundle/{symbolicName}`

* {user}: username for the admin user
* {password}: password
* {action}: describes an action to managing bundle, in this case, stop
* {host}: host of the AEM instance
* {port}: port of the AEM instance
* {symbolicName}: the symbolic name of the bundle

### aemConfigFile

Create AEM configuration files

**Process:**

1. replace all AEM parameters on the AEM configuration file at the location `bin`

### aemConfigureAgentAuthorToPublish

Configure the replication agent from author to publish AEM instance (on publish instance)

**Process:**

1. use the replication agent parameters on the configuration file `config/aem.properties`
2. change the replication parameters on the [AEM configuration page](http://localhost:4502/etc/replication/agents.author/publish.html)
> `curl -u {user}:{password} -X POST -F{properties}={newValue} http://{host}:{port}/{jcr_path}`

* {user}: username for the admin user
* {password}: password
* {host}: host of the AEM instance
* {port}: port of the AEM instance
* {properties}: node property to modify
* {jcr_path}: jcr node path

For example: `curl -u admin:admin -X POST -FtransportUser=administrator http://localhost:4502/etc/replication/agents.author/publish/jcr:content`

### aemConfigureReverseReplicationAgent

Configure the reverse replication agent on author instance

**Process:**

1. use the replication agent parameters on the configuration file `config/aem.properties`
2. change the replication parameters on the [AEM configuration page](http://localhost:4502/etc/replication/agents.author/publish_reverse.html)

> `curl -u {user}:{password} -X POST -F{properties}={newValue} http://{host}:{port}/{jcr_path}`

* {user}: username for the admin user
* {password}: password
* {host}: host of the AEM instance
* {port}: port of the AEM instance
* {properties}: node property to modify
* {jcr_path}: jcr node path

The {jcr_path} parameter will take in this case the value: `/etc/replication/agents.author/publish_reverse/jcr:content`

### aemConfigureFlushAgent

Configure the dispatcher flush agent on author or publish instance

> `curl -u {user}:{password} -X POST -F{properties}={newValue} http://{host}:{port}/{jcr_path}`

* {user}: username for the admin user
* {password}: password
* {host}: host of the AEM instance
* {port}: port of the AEM instance
* {properties}: node property to modify
* {jcr_path}: jcr node path

The {jcr_path} paramter will take in this case these values: 

* if the mode is author: `/etc/replication/agents.author/flush/jcr:content`
* if the mode is publish: `/etc/replication/agents.publish/flush/jcr:content`

### aemOsgiSettingsPublish

Load the OSGi configuration package to configure the OSGi configuration for a publish instance.

This package is named `package/publish-osgi-configuration.zip`. This package will create a folder on `/apps/config.publish` with the complete OSGi's configuration
for a production isntance. The JCR package location will be `etc/packages/config/publish-osgi-configuration.zip`.

**Configuration:**

1. Day CQ HTML Library Manager
	* PID: com.day.cq.widget.impl.HtmlLibraryManagerImpl (type sling:OsgiConfig)
	* htmllibmanager.minify (type Boolean) = `true`
	* htmllibmanager.gzip (type Boolean) = `true`
	* htmllibmanager.debug (type Boolean) = `false`
	* htmllibmanager.timing (type Boolean) = `false`

2. Day CQ WCM Debug Filter
	* PID: com.day.cq.wcm.core.impl.WCMDebugFilter (type sling:OsgiConfig)
	* wcmdbgfilter.enabled (type Boolean): `false`

3. Day CQ WCM Filter
	* PID: com.day.cq.wcm.core.WCMRequestFilter (type sling:OsgiConfig)
	* wcmfilter.mode (type String) = `disabled`

4. Apache Sling Java Script Handler
	* PID: org.apache.sling.scripting.java.impl.JavaScriptEngineFactory (type sling:OsgiConfig)
	* java.classdebuginfo (type Boolean) = `false`

5. Apache Sling JSP Script Handler
	* PID: org.apache.sling.scripting.jsp.JspScriptEngineFactory (type sling:OsgiConfig)
	* jasper.classdebuginfo (type Boolean) = `false`
	* jasper.mappedfile (type Boolean) = `false`
	