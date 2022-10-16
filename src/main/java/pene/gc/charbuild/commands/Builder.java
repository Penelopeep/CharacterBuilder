package pene.gc.charbuild.commands;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.game.avatar.Avatar;
import emu.grasscutter.game.inventory.GameItem;
import emu.grasscutter.game.player.Player;
import pene.gc.charbuild.utils.Datareader;

import java.util.List;

/**
 * Commands are comprised of 3 things:
 * 1. The {@link Command} annotation.
 * 2. Implementing the {@link CommandHandler} interface.
 * 3. Implementing the {@link CommandHandler#execute(Player, Player, List)} method.
 * 
 * The {@link Command} annotation should contain:
 * 1. A command label. ('example' in this case makes '/example' runnable in-game or on the console)
 * 2. A description of the command. (this is shown in the `/help` command description)
 * 3. A permission node. (this is used to check if the player has permission to use the command)
 * Other optional fields can be found in the {@link Command} annotation interface.
 */

@Command(label = "build", usage = "build <build name")
public final class Builder implements CommandHandler {

    @Override public void execute(Player sender, Player targetPlayer, List<String> args) {
        Avatar currentAvatar = targetPlayer.getTeamManager().getCurrentAvatarEntity().getAvatar();
        String avatarName = currentAvatar.getAvatarData().getName();
        Grasscutter.getLogger().info(String.format("AvatarName: %s", avatarName));
        if (args.size()<1){
            String buildList = Datareader.builds(avatarName);
            if (sender==null){
                Grasscutter.getLogger().info(String.format("Available builds for %s: %s",avatarName, buildList));
            } else {
                CommandHandler.sendMessage(targetPlayer,String.format("Available builds for %s: %s",avatarName, buildList));
            }
        } else if (args.size()>1) {
            if (sender==null){
                Grasscutter.getLogger().info("Wrong amount of arguments");
            } else {
                CommandHandler.sendMessage(targetPlayer,"Wrong amount of arguments");
            }
        } else {
            String buildName = args.get(0);
            List<GameItem> doneArtifacts = Datareader.artifacts(targetPlayer, avatarName, buildName);
            for (GameItem artifact : doneArtifacts) {
                currentAvatar.equipItem(artifact, true);
            }
        }
    }
}
