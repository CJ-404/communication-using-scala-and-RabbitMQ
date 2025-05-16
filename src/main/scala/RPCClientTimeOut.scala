import java.util.UUID
import java.util.concurrent.{ArrayBlockingQueue, BlockingQueue}
import java.util.concurrent.{ArrayBlockingQueue, BlockingQueue, TimeUnit, TimeoutException}

import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client._

class RPCResponseCallback(val corrId: String) extends DeliverCallback {
  val response: BlockingQueue[String] = new ArrayBlockingQueue[String](1)

  override def handle(consumerTag: String, message: Delivery): Unit = {
    if (message.getProperties.getCorrelationId.equals(corrId)) {
      response.offer(new String(message.getBody, "UTF-8"))
    }
  }

  def take(timeoutMillis: Long): Option[String] = {
    val result = response.poll(timeoutMillis, TimeUnit.MILLISECONDS)
    Option(result)
  }
}

class RPCClientTimeout(host: String) {

  val factory = new ConnectionFactory()
  factory.setHost(host)

  val connection: Connection = factory.newConnection()
  val channel: Channel = connection.createChannel()
  val requestQueueName: String = "rpc_queue"
  val replyQueueName: String = channel.queueDeclare().getQueue

  def call(message: String, timeoutMillis: Long = 5000): String = {
    val corrId = UUID.randomUUID().toString
    val props = new BasicProperties.Builder().correlationId(corrId)
      .replyTo(replyQueueName)
      .build()
    channel.basicPublish("", requestQueueName, props, message.getBytes("UTF-8"))

    val responseCallback = new RPCResponseCallback(corrId)
    val consumerTag = channel.basicConsume(replyQueueName, true, responseCallback, _ => { })

    val maybeResponse = responseCallback.take(timeoutMillis)

    channel.basicCancel(consumerTag)

    maybeResponse.getOrElse{
      throw new java.util.concurrent.TimeoutException(s"No response received in $timeoutMillis ms")
    }
  }

  def close() {
    connection.close()
  }
}

object RPCClientTimeout {

  def main(argv: Array[String]) {
    var fibonacciRpc: RPCClientTimeout = null
    var response: String = null
    try {
      val host = if (argv.isEmpty) "localhost" else argv(0)

      fibonacciRpc = new RPCClientTimeout(host)
      println(" [x] Requesting fib(30) and waitin for 3 seconds")
      response = fibonacciRpc.call("30", 3000)
      println(" [.] Got '" + response + "'")
    } catch {
      case e: java.util.concurrent.TimeoutException => println(" [!] Request timed out (3 seconds) and but request is still inside the queue")
      case e: Exception => e.printStackTrace()
    } finally {
      if (fibonacciRpc != null) {
        try {
          fibonacciRpc.close()
        } catch {
          case ignore: Exception =>
        }
      }
    }
  }
}
