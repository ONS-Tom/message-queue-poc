import com.google.inject.AbstractModule
import config.HBaseRestEventRepositoryConfigLoader
import consumers.RabbitMQEventConsumer
import play.api.{ Configuration, Environment }
import repository.EventRepository
import repository.hbase.HBaseRestEventRepository

class Module(environment: Environment, configuration: Configuration) extends AbstractModule {

  override def configure(): Unit = {
    val underlyingConfig = configuration.underlying
    val hbaseRestConfig = HBaseRestEventRepositoryConfigLoader.load(underlyingConfig)
    val hbaseRestEventRepository = new HBaseRestEventRepository
    new RabbitMQEventConsumer(hbaseRestEventRepository).setupSubscriber()
    bind(classOf[EventRepository]).toInstance(hbaseRestEventRepository)
  }
}
