# communication-using-scala-and-RabbitMQ

This project was done by learning from "https://github.com/rabbitmq/rabbitmq-tutorials/tree/main/scala" which was provided by the RabbitMQ documentation about `other languages`.

This is a minimalistic Scala port of the RabbitMQ.
This is primarily to the fact that RabbitMQ Java client still supports
JDK 6 and doesn't have a lambda-friendly API.

### system req
- maven:  Apache Maven 3.5.4 (mvnw is provided within the repo - so no problem)
- jdk:    openjdk version "1.8.0_452" / openjdk-8-jdk (should work for newest ones)
- scala:  Scala code runner version 2.13.16 
- RabbitMQ: 4.1.0

## Compiling the Code

    ./mvnw compile

## Running each scenario

### Hello World using Async (Unreliable since using implicit acks from TCP layer)

Execute the following command to receive a hello world:

    ./mvnw exec:java -Dexec.mainClass="Recv"

Execute the following in a separate shell to send a hello world:

    ./mvnw exec:java -Dexec.mainClass="Send"

### Hello World using Async (Reliable way using consumer ack and publisher confirm )
#### consumer ack and publisher confirm is orthogonal,
#### therefore still async in the context of consumer and publisher

Execute the following command to receive a hello world and send acknowledge to the QabbitMQ node:

    ./mvnw exec:java -Dexec.mainClass="RecvAck"

Execute the following in a separate shell to send a hello world and wait for he RMQ node to acknowledge:

    ./mvnw exec:java -Dexec.mainClass="SendAck"

### Call fibonacci function using RPC (sync)

#### service provider
In another shell to send the solution for fibonacci(30) for exactly 3 requests:

    ./mvnw exec:java -Dexec.mainClass="RPCServer"

#### request 1
In one shell to call and recv the solution for fibonacci(30) - Blocking infinitly:

    ./mvnw exec:java -Dexec.mainClass="RPCClient"

#### request 2
In one shell to call and recv the solution for fibonacci(30) - Blocking 3 seconds and persist the request:

    ./mvnw exec:java -Dexec.mainClass="RPCClientTimeout"

#### request 3
In one shell to call and recv the solution for fibonacci(30) - Blocking 3 seconds and clean the request:

    ./mvnw exec:java -Dexec.mainClass="RPCClientCleanTimeout"
