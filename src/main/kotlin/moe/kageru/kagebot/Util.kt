package moe.kageru.kagebot

import moe.kageru.kagebot.config.Config
import moe.kageru.kagebot.config.Config.server
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.message.MessageAuthor
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.permission.Role
import org.javacord.api.entity.user.User
import org.javacord.api.event.message.MessageCreateEvent
import java.awt.Color
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException

object Util {
    inline fun <T> T.doIf(condition: (T) -> Boolean, op: (T) -> T): T {
        return if (condition(this)) op(this) else this
    }

    /**
     * Mimics the behavior of [Optional.ifPresent], but returns null if the optional is empty,
     * allowing easier fallback behavior via Kotlin’s ?: operator.
     */
    private inline fun <T, R> Optional<T>.ifNotEmpty(op: (T) -> R): R? {
        if (this.isPresent) {
            return op(this.get())
        }
        return null
    }

    fun hasOneOf(messageAuthor: MessageAuthor, roles: Set<Role>): Boolean {
        return messageAuthor.asUser().ifNotEmpty { user ->
            user.getRoles(server).toSet().intersect(roles).isNotEmpty()
        } ?: false
    }

    private val channelIdRegex = Regex("\\d{18}")
    private fun String.isEntityId() = channelIdRegex.matches(this)

    @Throws(IllegalArgumentException::class)
    fun findRole(idOrName: String): Role {
        return when {
            idOrName.isEntityId() -> server.getRoleById(idOrName).ifNotEmpty { it }
                ?: throw IllegalArgumentException("Role $idOrName not found.")
            else -> server.getRolesByNameIgnoreCase(idOrName).let {
                when (it.size) {
                    0 -> throw IllegalArgumentException("Role $idOrName not found.")
                    1 -> it[0]
                    else -> throw IllegalArgumentException("More than one role found with name $idOrName. Please specify the role ID instead")
                }
            }
        }
    }

    fun <T> wasSuccessful(future: CompletableFuture<T>): Boolean {
        try {
            future.join()
        } catch (e: CompletionException) {
            // we don’t care about this error, but I don’t want to spam stdout
        }
        return !future.isCompletedExceptionally
    }

    @Throws(IllegalArgumentException::class)
    fun findChannel(idOrName: String): TextChannel {
        return when {
            idOrName.isEntityId() -> server.getTextChannelById(idOrName).ifNotEmpty { it }
                ?: throw IllegalArgumentException("Channel ID $idOrName not found.")
            else -> if (idOrName.startsWith('@')) {
                Globals.api.getCachedUserByDiscriminatedName(idOrName.removePrefix("@")).ifNotEmpty { user ->
                    val channelFuture = user.openPrivateChannel()
                    val channel = channelFuture.join()
                    if (channelFuture.isCompletedExceptionally) {
                        throw IllegalArgumentException("Could not open private channel with user $idOrName for redirection.")
                    }
                    channel
                }
                    ?: throw IllegalArgumentException("Can’t find user $idOrName for redirection.")
            } else {
                server.getTextChannelsByName(idOrName).let {
                    when (it.size) {
                        0 -> throw IllegalArgumentException("Channel $idOrName not found.")
                        1 -> it[0]
                        else -> throw IllegalArgumentException("More than one channel found with name $idOrName. Please specify the channel ID instead")
                    }
                }
            }
        }
    }

    inline fun checked(op: (() -> Unit)) {
        try {
            op()
        } catch (e: Exception) {
            Log.warn("An uncaught exception occurred.\n$e")
            MessageUtil.sendEmbed(
                Globals.api.owner.get(),
                EmbedBuilder()
                    .setColor(Color.RED)
                    .addField("Error", "kagebot has encountered an error")
                    .addField(
                        "$e", """```
                       ${e.stackTrace.joinToString("\n")}
                    ```""".trimIndent()
                    )
            )
        }
    }

    fun userFromMessage(message: MessageCreateEvent): User? {
        return message.messageAuthor.id.let { id ->
            Config.server.getMemberById(id).orElse(null)
        }
    }
}
