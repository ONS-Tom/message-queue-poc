package config

import com.typesafe.config.Config

object HBaseRestEventRepositoryConfigLoader {
  def load(rootConfig: Config, path: String = "db.hbase.rest"): HBaseRestEventRepositoryConfig ={
    HBaseRestEventRepositoryConfig()
  }
}
