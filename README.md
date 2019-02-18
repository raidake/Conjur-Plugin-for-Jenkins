# Conjur-Plugin-for-Jenkins

The plugin allows Jenkins to retrieve the secrets from Conjur and allow the users to use the secrets by referencing the environment variable that was injected with the secrets.

## How to use

Run

	mvn clean package

to create the plugin .hpi file.

To install:

1. copy the resulting ./target/credentials.hpi file to the $JENKINS_HOME/plugins directory. Don't forget to restart Jenkins afterwards.

2. or use the plugin management console (http://example.com:8080/pluginManager/advanced) to upload the hpi file. You have to restart Jenkins in order to find the plugin in the installed plugins list.


