package moe.nea.caelo

import moe.nea.caelo.config.CConfig
import moe.nea.caelo.util.MC
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos

object CaeloCommand : CommandBase() {
    override fun getCommandName(): String {
        return "veloxcaelo"
    }

    override fun getCommandAliases(): List<String> {
        return listOf("velox")
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean {
        return true
    }

    override fun getCommandUsage(sender: ICommandSender?): String {
        return ""
    }

    override fun addTabCompletionOptions(
        sender: ICommandSender?,
        args: Array<out String>,
        pos: BlockPos?
    ): List<String> {
        if (args.size == 1) {
            return getListOfStringsMatchingLastWord(args, subcommands.keys)
        }
        return emptyList()
    }

    private val subcommands = mutableMapOf<String, (args: Array<String>) -> Unit>()

    fun subcommand(name: String, function: (args: Array<String>) -> Unit) {
        subcommands[name] = function
    }

    override fun processCommand(iCommandSender: ICommandSender?, args: Array<String>) {
        if (args.isEmpty()) {
            Caelo.toOpen = CConfig.managed.getGui()
            return
        }
        val subCommand = subcommands[args[0]]
        if (subCommand == null) {
            MC.display("§cInvalid subcommand. Check the tab completions.")
            return
        }
        subCommand.invoke(args)
    }
}