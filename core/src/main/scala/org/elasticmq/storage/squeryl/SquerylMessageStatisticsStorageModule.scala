package org.elasticmq.storage.squeryl

import org.squeryl.PrimitiveTypeMode._
import org.elasticmq.storage.MessageStatisticsStorageModule
import org.elasticmq.{NeverReceived, MessageStatistics, SpecifiedMessage}

trait SquerylMessageStatisticsStorageModule extends MessageStatisticsStorageModule {
  this: SquerylSchemaModule =>

  object squerylMessageStatisticsStorage extends MessageStatisticsStorage {
    def readMessageStatistics(message: SpecifiedMessage) = {
      inTransaction {
        messageStatistics
          .lookup(message.id.get)
          .map(_.toMessageStatistics(message))
          .getOrElse(MessageStatistics.emptyFor(message))
      }
    }


    def writeMessageStatistics(statistics: MessageStatistics) {
      transaction {
        val messageInDb = messages.lookup(statistics.message.id.get)

        if (messageInDb.isDefined) {
          val squerylStatistics = SquerylMessageStatistics.from(statistics)
          if (statistics.approximateReceiveCount == 1) {
            messageStatistics.insert(squerylStatistics)
          } else {
            messageStatistics.update(squerylStatistics)
          }
        }
      }
    }
  }

  def messageStatisticsStorage = squerylMessageStatisticsStorage
}