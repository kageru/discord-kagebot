package moe.kageru.kagebot

import com.moandjiezana.toml.Toml
import java.io.File

class Config(val system: System, val commands: List<Command>) {
    companion object {
        val config: Config by lazy { read("config.toml") }
        val secret = File("secret").readText().replace("\n", "")

        private fun read(path: String): Config {
            val rawConfig: Toml = Toml().read(run {
                val file = File(path)
                if (file.isFile) {
                    return@run file
                }
                println("Config not found, falling back to defaults...")
                File(this::class.java.classLoader.getResource(path)!!.toURI())
            })
            val parsed = rawConfig.to(Config::class.java)
            return Config(
                System(parsed.system),
                parsed.commands.map { Command(it) }
            )
        }

    }
}

data class System(val serverId: String, val admins: List<String>) {
    /**
     * Self constructor to explicitly repeat the nullability checks after TOML parsing.
     */
    constructor(system: System) : this(system.serverId, system.admins)
}

