package moe.kageru.kagebot.features

import moe.kageru.kagebot.config.ConfigParser
import org.javacord.api.event.message.CertainMessageEvent

/**
 * Simple message handler to send the current config file via message attachment.
 */
class GetConfigFeature : MessageFeature {
  override fun handle(message: CertainMessageEvent) {
    message.channel.sendMessage(ConfigParser.configFile)
  }
}
