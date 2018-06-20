import com.google.inject.AbstractModule
import config.HBaseRestEventRepositoryConfigLoader
import play.api.{ Configuration, Environment }
import repository.EventRepository
import repository.hbase.HBaseRestEventRepository

class Module(environment: Environment, configuration: Configuration) extends AbstractModule {

  override def configure(): Unit = {
    val underlyingConfig = configuration.underlying
    val elasticConfig = HBaseRestEventRepositoryConfigLoader.load(underlyingConfig)
    val hbaseRestEventRepository = new HBaseRestEventRepository
    bind(classOf[EventRepository]).toInstance(hbaseRestEventRepository)
  }
}
