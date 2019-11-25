
# CorDapp Template - Java

Welcome to the Java CorDapp template. The CorDapp template is a stubbed-out CorDapp that you can use to bootstrap 
your own CorDapps.

**This is the Java version of the CorDapp template taken from [here](https://github.com/corda/cordapp-template-java). 
The Kotlin equivalent is [here](https://github.com/corda/cordapp-template-kotlin/).**

# Pre-Requisites

See https://docs.corda.net/getting-set-up.html.

# Usage

## Running tests inside IntelliJ
	
We recommend editing your IntelliJ preferences so that you use the Gradle runner - this means that the quasar utils
plugin will make sure that some flags (like ``-javaagent`` - see below) are
set for you.

To switch to using the Gradle runner:

* Navigate to ``Build, Execution, Deployment -> Build Tools -> Gradle -> Runner`` (or search for `runner`)
  * Windows: this is in "Settings"
  * MacOS: this is in "Preferences"
* Set "Delegate IDE build/run actions to gradle" to true
* Set "Run test using:" to "Gradle Test Runner"

If you would prefer to use the built in IntelliJ JUnit test runner, you can run ``gradlew installQuasar`` which will
copy your quasar JAR file to the lib directory. You will then need to specify ``-javaagent:lib/quasar.jar``
and set the run directory to the project root directory for each test.

## Deploying our CorDapp and running the nodes

See https://docs.corda.net/tutorial-cordapp.html#running-the-example-cordapp for details.

In short: we can run this `deployNodes` task using Gradle. For each node definition, Gradle will:

- Package the project’s source files into a CorDapp jar
- Create a new node in `build/nodes` with our CorDapp already installed

We can do that now by running the following commands from the root of the project:

`./gradlew clean deployNodes`

Running `deployNodes` will build the nodes under `build/nodes`. If we navigate to one of these folders, 
we’ll see the three node folders. Each node folder has the following structure:
<pre>
    .
    |____corda.jar                     // The runnable node
    |____corda-webserver.jar           // The node's webserver (The notary doesn't need a web server)
    |____node.conf                     // The node's configuration file
    |____cordapps
    |____java/kotlin-source-0.1.jar    // Our IOU CorDapp
</pre>
Let’s start the nodes by running the following commands from the root of the project:  
<pre>
build/nodes/runnodes
</pre>

## Interacting with the nodes

### Shell

When started via the command line, each node will display an interactive shell:

    Welcome to the Corda interactive shell.
    Useful commands include 'help' to see what is available, and 'bye' to shut down the node.
    
    Tue Nov 06 11:58:13 GMT 2018>>>

You can use this shell to interact with your node. For example, enter `run networkMapSnapshot` to see a list of 
the other nodes on the network:

    Tue Nov 06 11:58:13 GMT 2018>>> run networkMapSnapshot
    [
      {
      "addresses" : [ "localhost:10002" ],
      "legalIdentitiesAndCerts" : [ "O=Notary, L=London, C=GB" ],
      "platformVersion" : 3,
      "serial" : 1541505484825
    },
      {
      "addresses" : [ "localhost:10005" ],
      "legalIdentitiesAndCerts" : [ "O=PartyA, L=London, C=GB" ],
      "platformVersion" : 3,
      "serial" : 1541505382560
    },
      {
      "addresses" : [ "localhost:10008" ],
      "legalIdentitiesAndCerts" : [ "O=PartyB, L=New York, C=US" ],
      "platformVersion" : 3,
      "serial" : 1541505384742
    }
    ]
    
    Tue Nov 06 12:30:11 GMT 2018>>> 

`start IOUFlow iouValue: 99, otherParty: "O=PartyB,L=New York,C=US"`

Inspect status of transaction
 
`run vaultQuery contractStateType: com.template.states.IOUState`

You can find out more about the node shell [here](https://docs.corda.net/shell.html).

### Client

`clients/src/main/java/com/template/Client.java` defines a simple command-line client that connects to a node via RPC 
and prints a list of the other nodes on the network.

#### Running the client

##### Via the command line

Run the `runTemplateClient` Gradle task. By default, it connects to the node with RPC address `localhost:10006` with 
the username `user1` and the password `test`.

##### Via IntelliJ

Run the `Run Template Client` run configuration. By default, it connects to the node with RPC address `localhost:10006` 
with the username `user1` and the password `test`.

### Webserver

`clients/src/main/java/com/template/webserver/` defines a simple Spring webserver that connects to a node via RPC and 
allows you to interact with the node over HTTP.

The API endpoints are defined here:

     clients/src/main/java/com/template/webserver/Controller.java

And a static webpage is defined here:

     clients/src/main/resources/static/

#### Running the webserver

##### Via the command line

Run the `runTemplateServer` Gradle task. By default, it connects to the node with RPC address `localhost:10006` with 
the username `user1` and the password `test`, and serves the webserver on port `localhost:10050`.

##### Via IntelliJ

Run the `Run Template Server` run configuration. By default, it connects to the node with RPC address `localhost:10006` 
with the username `user1` and the password `test`, and serves the webserver on port `localhost:10050`.

#### Interacting with the webserver

To start webserver use `runPartyAServer` or `runPartyBServer` tasks

List of available actions taken from [here](https://docs.corda.net/tutorial-cordapp.html):

- Returns the node's name:  
`http://localhost:10050/api/example/me`

- Returns all parties registered with the network map service:  
`http://localhost:10050/api/example/peers`

- Displays all IOU states that exist in the node's vault:  
`http://localhost:10050/api/example/ious`

- To create an IOU between PartyA and PartyB, run the following command from the command line:  
`curl -i -X POST 'http://localhost:10050/api/example/create-iou?iouValue=8&partyName=O=PartyB,+L=New+York,+C=US' -H 'Content-Type: application/x-www-form-urlencoded'`

- Check IOU states again to see added transaction:  
`http://localhost:10050/api/example/ious`
    
# Extending the template

You should extend this template as follows:

* Add your own state and contract definitions under `contracts/src/main/java/`
* Add your own flow definitions under `workflows/src/main/java/`
* Extend or replace the client and webserver under `clients/src/main/java/`

For a guided example of how to extend this template, see the Hello, World! tutorial 
[here](https://docs.corda.net/hello-world-introduction.html).
