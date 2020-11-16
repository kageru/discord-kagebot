package moe.kageru.kagebot.features

import arrow.core.extensions.listk.functorFilter.filter
import moe.kageru.kagebot.MessageUtil.sendEmbed
import moe.kageru.kagebot.command.MatchType
import moe.kageru.kagebot.config.Config
import org.javacord.api.event.message.CertainMessageEvent

class HelpFeature : MessageFeature {
  override fun handle(message: CertainMessageEvent) {
    message.channel.sendEmbed {
      addField("Commands:", listCommands(message))
    }
  }
}

private fun listCommands(message: CertainMessageEvent) = Config.commands
  .filter { it.matchType == MatchType.PREFIX && it.isAllowed(message) }
  .joinToString("\n") { it.trigger }
