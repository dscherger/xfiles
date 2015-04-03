BUILDING
========

The ant `build.xml` requires a `build.properties` file that must be created with the
following properties, set appropriately for your environment.

	idea.dir=$IDEA_HOME
	plugin.dir=$HOME/config/plugins
	jdk.dir=$JAVA_HOME

With these properties set appropriately, the "all" target will build and install the
plugin, which will be active after restarting intellij (but see the LOGGING section
below before restarting).

The plugin.dir is separated out as plugins may be installed on a per-user basis
in their local configuration directories.

On OSX the `build.properties` file looks like:

	idea.dir=/Applications/IntelliJ\ IDEA\ 14.app/Contents
	plugin.dir=/Users/derek/Library/Application Support/IntelliJIdea14
	jdk.dir=/Library/Java/JavaVirtualMachines/jdk1.8.0_25.jdk/Contents/Home

LOGGING
=======

IntelliJ uses log4j internally which is rather convenient for logging things from
plugins under development!

Logging is configured in `$IDEA_HOME/bin/log.xml` using the log4j xml configuration.
Log files are written to the IntelliJ `$SYSTEM_DIR/log/idea.log[.n]`

The `$IDEA_HOME/bin/idea.lax` file might indicate where the SYSTEM_DIR is.

Adding the following to the log.xml configuration file will configure log4j to log
messages from the xfiles plugin to the xfiles.log file in the system log dir.

	<appender name="XFILES" class="org.apache.log4j.RollingFileAppender">
		<param name="MaxFileSize" value="1Mb"/>
		<param name="MaxBackupIndex" value="3"/>
		<param name="file" value="$SYSTEM_DIR$/log/xfiles.log"/>
		<layout class="org.apache.log4j.PatternLayout">
			<param name="ConversionPattern" value="%d %-5p %t %c %m%n"/>
		</layout>
	</appender>

	<category name=="com.echologic.xfiles" >
		<appender-ref ref="XFILES"/>
		<priority value="DEBUG"/>
	</category>

On my setup, the log4j.xml configuration seems to use ' \n' to end message lines
but this looks like a bug. Standard log4j configuration uses %n to issue a platform
specific line ending. Also, the PatternLayout above is different than the one
intellij's configuration uses. Set it to something that makes you happy.
