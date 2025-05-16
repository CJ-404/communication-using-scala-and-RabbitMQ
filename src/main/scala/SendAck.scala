
import com.rabbitmq.client.ConnectionFactory

object SendAck {

  private val QUEUE_NAME = "hello"

  def main(argv: Array[String]) {
    val factory = new ConnectionFactory()
    factory.setHost("localhost")
    val connection = factory.newConnection()
    val channel = connection.createChannel()
    channel.confirmSelect()

    channel.queueDeclare(QUEUE_NAME, false, false, false, null)
    
    val message = "Hello World!"
    channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"))
    
    // 5 second timeout
    channel.waitForConfirmsOrDie(5000);

    println(" [x] Sent '" + message + "' and waited for `atmost` 5 seconds to recieve ack from the broker")
    
    channel.close()
    connection.close()
  }
}
