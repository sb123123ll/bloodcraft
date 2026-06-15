package com.qiamao.blood.command;

import com.qiamao.blood.event.DesireEventHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Bloodcraft 模组主指令
 * 用法: /blood force - 强制下个夜晚触发欲望事件
 */
public class CommandBlood extends CommandBase {

    @Override
    public String getName() {
        return "blood";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/blood force";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2; // 需要 OP 权限 (Level 2)
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            throw new WrongUsageException(getUsage(sender));
        }

        if ("force".equalsIgnoreCase(args[0])) {
            DesireEventHandler.forceNextNightEvent();
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "已强制开启: 下个夜晚将必定触发欲望事件 (血撒大地...)"));
        } else {
            throw new WrongUsageException(getUsage(sender));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "force");
        }
        return Collections.emptyList();
    }
}
