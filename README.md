
# "I owe you" (IoU) blockchain app example using Corda platform 

**Based on Java version of the CorDapp template taken from [here](https://github.com/corda/cordapp-template-java). 
The Kotlin template situated [here](https://github.com/corda/cordapp-template-kotlin/).**

###### CorDapp (Corda Distributed Application) - distributed applications that run on the Corda platform

## Pre-Requisites

See https://docs.corda.net/getting-set-up.html.

## Deploying our CorDapp and running the nodes

See https://docs.corda.net/tutorial-cordapp.html#running-the-example-cordapp for details.

In short: we can run this `deployNodes` task using Gradle. For each node definition, Gradle will:

- Package the project’s source files into a CorDapp jars
- Create a new node in `build/nodes` with our CorDapp already installed

We can do that now by running the following commands from the root of the project:

    ./gradlew clean deployNodes

Running `deployNodes` will build the nodes under `build/nodes`. If we navigate to one of these folders, 
we’ll see the three node folders. Each node folder has the following structure:

    .
    |____corda.jar                     // The runnable node
    |____node.conf                     // The node's configuration file
    |____cordapps                      // Our IOU CorDapp

Let’s start the nodes by running the following commands from the root of the project:  

    build/nodes/runnodes

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

Check available flows by `flow list` command:

    Wed Dec 04 17:47:34 MSK 2019>>> flow list
    com.template.flows.IOUFlow$Initiator
    net.corda.core.flows.ContractUpgradeFlow$Authorise
    net.corda.core.flows.ContractUpgradeFlow$Deauthorise
    net.corda.core.flows.ContractUpgradeFlow$Initiate

Check node info by `run nodeInfo` command:

    Wed Dec 04 17:47:17 MSK 2019>>> run nodeInfo
    addresses:
    - "partyb:10008"
    legalIdentitiesAndCerts:
    - "O=PartyB, L=New York, C=US"
    platformVersion: 5
    serial: 1575470125333

Start new flow from `NodeA` by `start` command:

    start IOUFlow$Initiator iouValue: 99, otherParty: "O=PartyB,L=New York,C=US"

Inspect status of transaction by `run vaultQuery` command:
 
    run vaultQuery contractStateType: com.template.states.IOUState

Only `NodeA` & `NodeB` know about this transation. You could check it by running same command on notary node

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
    
## Extending the application

You could extend this application as follows:

* Add your own state and contract definitions under `contracts/src/main/java/`
* Add your own flow definitions under `workflows/src/main/java/`
* Extend or replace the client and webserver under `clients/src/main/java/`

For a guided example of how to extend this template, see the Hello, World! tutorial 
[here](https://docs.corda.net/hello-world-introduction.html).

## Put node into Docker container

Firstly we need to generate nodes files with appropriate configuration under `build/nodes` by `deployNodesForDocker` task: 

    ./gradlew clean deployNodesForDocker

And start Docker containers by command:

    docker-compose up

After we could connect to Corda shell of node inside Docker container by `ssh` command using password `test`:

    ssh -p 10024 user1@localhost

Or if needed - could connect to container by SSH using `docker exec` command:

    docker exec -it cordapp-iou-java_partya_1 sh

To get container name use `docker ps` command before that

Note: In case of experiments with multiple logins and Docker images recreation warning about "man in the middle attack" could appear.
To dismiss it you could delete `~.ssh/known_hosts` file

## Connection to node H2 DB

To inspect content of node H2 DB you could use any DB client, for example [this](https://www.h2database.com/html/download.html) from H2 site.
For connection you need to use url like `jdbc:h2:<absolute path to 'persistence' folder of node>`, for example: 

    jdbc:h2:c:\Work\Personal\cordapp-iou-java\build\nodes\PartyA\persistence

and `sa` user with empty password

## Use PostgreSQL DB inside Docker container instead of H2 DB

Add next block into `node.conf`:

    dataSourceProperties {
        dataSourceClassName="org.postgresql.ds.PGSimpleDataSource"
        dataSource.url="jdbc:postgresql://localhost:5433/partya"
        dataSource.user="node-user"
        dataSource.password="node-password"
    }
    database {
        transactionIsolationLevel="READ_COMMITTED"
    }

Start Docker cotainer with PostgreSQL by run `docker-compose up` command
from folder with next `docker-compose.yml` script:

    version: '3'
    services:
      postgres_partya:
        image: postgres:9.6.15-alpine
        volumes:
          - postgres-partya-volume:/var/lib/postgresql/data
        ports:
          - 5433:5432
        environment:
          - POSTGRES_DB=partya
          - POSTGRES_USER=node-user
          - POSTGRES_PASSWORD=node-password
        restart: unless-stopped
    volumes:
      postgres-partya-volume:

## Connecting to DB of running node

To make H2 DB of running node accessible - node should be started with following block added into `node.conf`:

    h2Settings {
        address: "localhost:12345"
    }

After that - connect to `jdbc:h2:tcp://localhost:12345/node` using any database browsing tool that supports JDBC.
