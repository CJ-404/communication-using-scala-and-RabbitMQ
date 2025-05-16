
import com.rabbitmq.client._

object RecvAck {

  private val QUEUE_NAME = "hello"

  def main(argv: Array[String]) {
    val factory = new ConnectionFactory()
    factory.setHost("localhost")
    val connection = factory.newConnection()
    val channel = connection.createChannel()
    channel.queueDeclare(QUEUE_NAME, false, false, false, null)
    println(" [*] Waiting for messages. To exit press CTRL+C")
    val deliverCallback: DeliverCallback = (_, delivery) => {
      val message = new String(delivery.getBody, "UTF-8")
      println(" [x] Received '" + message + "'")
    }
    // channel.basicConsume(QUEUE_NAME, true, deliverCallback, _ => { })
  

    val autoAck : Boolean = false
    channel.basicConsume(QUEUE_NAME, autoAck, "consumer-tag-xyz", // autoack disabled
     new DefaultConsumer(channel) {
         override def handleDelivery(
           consumerTag : String,
           envelope : Envelope,
           properties : AMQP.BasicProperties,
           body: Array[Byte]
         ): Unit = {
             val message = new String(body, "UTF-8")

             val deliveryTag = envelope.getDeliveryTag;
             // positively acknowledge a single delivery, the message will be discarded from the queue
             channel.basicAck(deliveryTag, false)
             
             println(s"Received message: '$message' and sent basic ack")
         }
     });
  }
}
