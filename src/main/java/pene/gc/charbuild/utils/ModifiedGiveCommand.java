package pene.gc.charbuild.utils;

import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.GameDepot;
import emu.grasscutter.data.excels.AvatarData;
import emu.grasscutter.data.excels.ItemData;
import emu.grasscutter.data.excels.ReliquaryAffixData;
import emu.grasscutter.data.excels.ReliquaryMainPropData;
import emu.grasscutter.game.inventory.GameItem;
import emu.grasscutter.game.inventory.ItemType;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.ActionReason;
import emu.grasscutter.game.props.FightProperty;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import static emu.grasscutter.command.CommandHelpers.*;

@Command(
        label = "give",
        aliases = {"g", "item", "giveitem"},
        usage = {
                "(<itemId>|<avatarId>|all|weapons|mats|avatars) [lv<level>] [r<refinement>] [x<amount>] [c<constellation>]",
                "<artifactId> [lv<level>] [x<amount>] [<mainPropId>] [<appendPropId>[,<times>]]..."},
        threading = true)
public final class ModifiedGiveCommand{
    private enum GiveAllType {
        NONE,
        ALL,
        WEAPONS,
        MATS,
        AVATARS
    }

    private static final Map<Pattern, BiConsumer<ModifiedGiveCommand.GiveItemParameters, Integer>> intCommandHandlers = Map.ofEntries(
            Map.entry(lvlRegex, ModifiedGiveCommand.GiveItemParameters::setLvl),
            Map.entry(refineRegex, ModifiedGiveCommand.GiveItemParameters::setRefinement),
            Map.entry(amountRegex, ModifiedGiveCommand.GiveItemParameters::setAmount),
            Map.entry(constellationRegex, ModifiedGiveCommand.GiveItemParameters::setConstellation)
    );

    private static class GiveItemParameters {
        public int id;
        @Setter
        public int lvl = 0;
        @Setter public int amount = 1;
        @Setter public int refinement = 1;
        @Setter public int constellation = -1;
        public int mainPropId = -1;
        public List<Integer> appendPropIdList;
        public ItemData data;
        public AvatarData avatarData;
    }

    private GiveItemParameters parseArgs(Player sender, List<String> args) throws IllegalArgumentException {
        GiveItemParameters param = new GiveItemParameters();

        // Extract any tagged arguments (e.g. "lv90", "x100", "r5")
        parseIntParameters(args, param, intCommandHandlers);

        // At this point, first remaining argument MUST be itemId/avatarId
        if (args.size() < 1) {
            throw new IllegalArgumentException();
        }
        String id = args.remove(0);
        boolean isRelic = false;

        try {
            param.id = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            // TODO: Parse from item name using GM Handbook.
            CommandHandler.sendTranslatedMessage(sender, "commands.generic.invalid.itemId");
            throw e;
        }
        param.data = GameData.getItemDataMap().get(param.id);
        if ((param.id > 10_000_000) && (param.id < 12_000_000))
            param.avatarData = GameData.getAvatarDataMap().get(param.id);
        else if ((param.id > 1000) && (param.id < 1100))
            param.avatarData = GameData.getAvatarDataMap().get(param.id - 1000 + 10_000_000);
        isRelic = ((param.data != null) && (param.data.getItemType() == ItemType.ITEM_RELIQUARY));
        if (!isRelic && !args.isEmpty() && (param.amount == 1)) {  // A concession for the people that truly hate [x<amount>].
            try {
                param.amount = Integer.parseInt(args.remove(0));
            } catch (NumberFormatException e) {
                CommandHandler.sendTranslatedMessage(sender, "commands.generic.invalid.amount");
                throw e;
            }
        }

        if (param.amount < 1) param.amount = 1;
        if (param.refinement < 1) param.refinement = 1;
        if (param.refinement > 5) param.refinement = 5;
        if (isRelic) {
            // Input 0-20 to match game, instead of 1-21 which is the real level
            if (param.lvl < 0) param.lvl = 0;
            if (param.lvl > 20) param.lvl = 20;
            param.lvl += 1;
        } else {
            // Suitable for Avatars and Weapons
            if (param.lvl < 1) param.lvl = 1;
            if (param.lvl > 90) param.lvl = 90;
        }

        if (!args.isEmpty()) {
            if (isRelic) {
                try {
                    parseRelicArgs(param, args);
                } catch (IllegalArgumentException e) {
                    CommandHandler.sendTranslatedMessage(sender, "commands.execution.argument_error");
                    CommandHandler.sendTranslatedMessage(sender, "commands.give.usage_relic");
                    throw e;
                }
            } else {
                throw new IllegalArgumentException();
            }
        }

        return param;
    }

    public GameItem execute(Player sender, Player targetPlayer, List<String> args) {
        try {
            GiveItemParameters param = parseArgs(sender, args);
            // If it's not an avatar, it needs to be a valid item
            if (param.data == null) {
                CommandHandler.sendTranslatedMessage(sender, "commands.generic.invalid.itemId");

            }

            if (param.data.getItemType() == ItemType.ITEM_RELIQUARY) {
                List<GameItem> artifact = makeArtifacts(param);
                targetPlayer.getInventory().addItems(artifact, ActionReason.SubfieldDrop);
                return artifact.get(0);
            }
            targetPlayer.getInventory().addItem(new GameItem(param.data, param.amount), ActionReason.SubfieldDrop);
        } catch (IllegalArgumentException ignored) {}
        return null;
    }

    private static List<GameItem> makeArtifacts(GiveItemParameters param) {
        param.lvl = Math.min(param.lvl, param.data.getMaxLevel());
        int rank = param.data.getRankLevel();
        int totalExp = 0;
        for (int i = 1; i < param.lvl; i++)
            totalExp += GameData.getRelicExpRequired(rank, i);

        List<GameItem> items = new ArrayList<>(param.amount);
        for (int i = 0; i < param.amount; i++) {
            // Create item for the artifact.
            GameItem item = new GameItem(param.data);
            item.setLevel(param.lvl);
            item.setTotalExp(totalExp);
            int numAffixes = param.data.getAppendPropNum() + (param.lvl-1)/4;
            if (param.mainPropId > 0)  // Keep random mainProp if we didn't specify one
                item.setMainPropId(param.mainPropId);
            if (param.appendPropIdList != null) {
                item.getAppendPropIdList().clear();
                item.getAppendPropIdList().addAll(param.appendPropIdList);
            }
            // If we didn't include enough substats, top them up to the appropriate level at random
            item.addAppendProps(numAffixes - item.getAppendPropIdList().size());
            items.add(item);
        }
        return items;
    }

    private static int getArtifactMainProp(ItemData itemData, FightProperty prop) throws IllegalArgumentException {
        if (prop != FightProperty.FIGHT_PROP_NONE)
            for (ReliquaryMainPropData data : GameDepot.getRelicMainPropList(itemData.getMainPropDepotId()))
                if (data.getWeight() > 0 && data.getFightProp() == prop)
                    return data.getId();
        throw new IllegalArgumentException();
    }

    private static List<Integer> getArtifactAffixes(ItemData itemData, FightProperty prop) throws IllegalArgumentException {
        if (prop == FightProperty.FIGHT_PROP_NONE) {
            throw new IllegalArgumentException();
        }
        List<Integer> affixes = new ArrayList<>();
        for (ReliquaryAffixData data : GameDepot.getRelicAffixList(itemData.getAppendPropDepotId())) {
            if (data.getWeight() > 0 && data.getFightProp() == prop) {
                affixes.add(data.getId());
            }
        }
        return affixes;
    }

    private static int getAppendPropId(String substatText, ItemData itemData) throws IllegalArgumentException {
        // If the given substat text is an integer, we just use that as the append prop ID.
        try {
            return Integer.parseInt(substatText);
        } catch (NumberFormatException ignored) {
            // If the argument was not an integer, we try to determine
            // the append prop ID from the given text + artifact information.
            // A substat string has the format `substat_tier`, with the
            // `_tier` part being optional, defaulting to the maximum.
            String[] substatArgs = substatText.split("_");
            String substatType = substatArgs[0];

            int substatTier = 4;
            if (substatArgs.length > 1) {
                substatTier = Integer.parseInt(substatArgs[1]);
            }

            List<Integer> substats = getArtifactAffixes(itemData, FightProperty.getPropByShortName(substatType));

            if (substats.isEmpty()) {
                throw new IllegalArgumentException();
            }

            substatTier -= 1;  // 1-indexed to 0-indexed
            substatTier = Math.min(Math.max(0, substatTier), substats.size() - 1);
            return substats.get(substatTier);
        }
    }

    private static void parseRelicArgs(GiveItemParameters param, List<String> args) throws IllegalArgumentException {
        // Get the main stat from the arguments.
        // If the given argument is an integer, we use that.
        // If not, we check if the argument string is in the main prop map.
        String mainPropIdString = args.remove(0);

        try {
            param.mainPropId = Integer.parseInt(mainPropIdString);
        } catch (NumberFormatException ignored) {
            // This can in turn throw an exception which we don't want to catch here.
            param.mainPropId = getArtifactMainProp(param.data, FightProperty.getPropByShortName(mainPropIdString));
        }

        // Get substats.
        param.appendPropIdList = new ArrayList<>();
        // Every remaining argument is a substat.
        for (String prop : args) {
            // The substat syntax permits specifying a number of rolls for the given
            // substat. Split the string into stat and number if that is the case here.
            String[] arr = prop.split(",");
            prop = arr[0];
            int n = 1;
            if (arr.length > 1) {
                n = Math.min(Integer.parseInt(arr[1]), 200);
            }

            // Determine the substat ID.
            int appendPropId = getAppendPropId(prop, param.data);

            // Add the current substat.
            for (int i = 0; i < n; i++) {
                param.appendPropIdList.add(appendPropId);
            }
        };
    }
}

