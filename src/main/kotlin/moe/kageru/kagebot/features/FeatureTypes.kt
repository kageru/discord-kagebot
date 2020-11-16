package moe.kageru.kagebot.features

import org.javacord.api.DiscordApi
import org.javacord.api.event.message.CertainMessageEvent

interface MessageFeature {
  fun handle(message: CertainMessageEvent)
}

interface EventFeature {
  fun register(api: DiscordApi)
}
