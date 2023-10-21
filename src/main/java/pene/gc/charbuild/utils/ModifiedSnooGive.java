package pene.gc.charbuild.utils;

import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.ItemData;
import emu.grasscutter.data.excels.reliquary.ReliquaryAffixData;
import emu.grasscutter.data.excels.reliquary.ReliquaryMainPropData;
import emu.grasscutter.game.inventory.GameItem;
import emu.grasscutter.game.inventory.ItemType;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.ActionReason;
import emu.grasscutter.game.props.FightProperty;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

@SuppressWarnings("deprecation")
public final class ModifiedSnooGive {

    //maps substat typable names to substat actual names
    private static final Map<String,String> substatNameMap = Map.ofEntries(
            entry("hp", "FIGHT_PROP_HP"),
            entry("healthpoint","FIGHT_PROP_HP"),
            entry("healthpoints","FIGHT_PROP_HP"),
            entry("hp%", "FIGHT_PROP_HP_PERCENT"),
            entry("hppercentage","FIGHT_PROP_HP_PERCENT"),
            entry("atk", "FIGHT_PROP_ATTACK"),
            entry("attack","FIGHT_PROP_ATTACK"),
            entry("atk%", "FIGHT_PROP_ATTACK_PERCENT"),
            entry("attackpercent","FIGHT_PROP_ATTACK_PERCENT"),
            entry("attackpercentage","FIGHT_PROP_ATTACK_PERCENT"),
            entry("def", "FIGHT_PROP_DEFENSE"),
            entry("defense","FIGHT_PROP_DEFENSE"),
            entry("defence","FIGHT_PROP_DEFENSE"),
            entry("def%", "FIGHT_PROP_DEFENSE_PERCENT"),
            entry("defensepercent","FIGHT_PROP_DEFENSE_PERCENT"),
            entry("defencepercent","FIGHT_PROP_DEFENSE_PERCENT"),
            entry("defensepercentage","FIGHT_PROP_DEFENSE_PERCENT"),
            entry("defencepercentage","FIGHT_PROP_DEFENSE_PERCENT"),
            entry("er", "FIGHT_PROP_CHARGE_EFFICIENCY"),
            entry("energyrecharge","FIGHT_PROP_CHARGE_EFFICIENCY"),
            entry("em", "FIGHT_PROP_ELEMENT_MASTERY"),
            entry("elementalmastery","FIGHT_PROP_ELEMENT_MASTERY"),
            entry("cr", "FIGHT_PROP_CRITICAL"),
            entry("crate","FIGHT_PROP_CRITICAL"),
            entry("critrate","FIGHT_PROP_CRITICAL"),
            entry("criticalrate","FIGHT_PROP_CRITICAL"),
            entry("cdmg", "FIGHT_PROP_CRITICAL_HURT"),
            entry("critdmg","FIGHT_PROP_CRITICAL_HURT"),
            entry("critdamage","FIGHT_PROP_CRITICAL_HURT"),
            entry("criticaldmg","FIGHT_PROP_CRITICAL_HURT"),
            entry("criticaldamage","FIGHT_PROP_CRITICAL_HURT"),
            entry("cd","FIGHT_PROP_CRITICAL_HURT"),
            entry("cdr","FIGHT_PROP_SKILL_CD_MINUS_RATIO"),
            entry("cdreduction","FIGHT_PROP_SKILL_CD_MINUS_RATIO"),
            entry("cooldownreduction","FIGHT_PROP_SKILL_CD_MINUS_RATIO"),
            entry("phys","FIGHT_PROP_PHYSICAL_ADD_HURT"),
            entry("dendro","FIGHT_PROP_GRASS_ADD_HURT"),
            entry("geo","FIGHT_PROP_ROCK_ADD_HURT"),
            entry("anemo","FIGHT_PROP_WIND_ADD_HURT"),
            entry("hydro","FIGHT_PROP_WATER_ADD_HURT"),
            entry("cryo","FIGHT_PROP_ICE_ADD_HURT"),
            entry("electro","FIGHT_PROP_ELEC_ADD_HURT"),
            entry("pyro","FIGHT_PROP_FIRE_ADD_HURT"),
            entry("hb","FIGHT_PROP_HEAL_ADD"),
            entry("speed","FIGHT_PROP_SPEED_PERCENT"),
            entry("sb","FIGHT_PROP_SHIELD_COST_MINUS_RATIO"),
            entry("shieldbonus","FIGHT_PROP_SHIELD_COST_MINUS_RATIO"),
            entry("shield","FIGHT_PROP_SHIELD_COST_MINUS_RATIO"),
            entry("damage","FIGHT_PROP_SUB_HURT"),
            entry("dr","FIGHT_PROP_SUB_HURT"),
            entry("dmgr","FIGHT_PROP_SUB_HURT"),
            entry("dmgreduction","FIGHT_PROP_SUB_HURT"),
            entry("damagereduction","FIGHT_PROP_SUB_HURT"),
            entry("damager","FIGHT_PROP_SUB_HURT")
    );

    //maps mainstat typable names to mainstat actual names
    private static final Map<String,String> mainstatNameMap = Map.ofEntries(
            entry("hp","FIGHT_PROP_HP"),
            entry("hp%","FIGHT_PROP_HP_PERCENT"),
            entry("atk","FIGHT_PROP_ATTACK"),
            entry("atk%","FIGHT_PROP_ATTACK_PERCENT"),
            entry("def","FIGHT_PROP_DEFENSE"),
            entry("def%","FIGHT_PROP_DEFENSE_PERCENT"),
            entry("er","FIGHT_PROP_CHARGE_EFFICIENCY"),
            entry("em","FIGHT_PROP_ELEMENT_MASTERY"),
            entry("hb","FIGHT_PROP_HEAL_ADD"),
            entry("cdmg","FIGHT_PROP_CRITICAL_HURT"),
            entry("cd","FIGHT_PROP_CRITICAL_HURT"),
            entry("cr","FIGHT_PROP_CRITICAL"),
            entry("phys","FIGHT_PROP_PHYSICAL_ADD_HURT"),
            entry("dendro","FIGHT_PROP_GRASS_ADD_HURT"),
            entry("geo","FIGHT_PROP_ROCK_ADD_HURT"),
            entry("anemo","FIGHT_PROP_WIND_ADD_HURT"),
            entry("hydro","FIGHT_PROP_WATER_ADD_HURT"),
            entry("cryo","FIGHT_PROP_ICE_ADD_HURT"),
            entry("electro","FIGHT_PROP_ELEC_ADD_HURT"),
            entry("pyro","FIGHT_PROP_FIRE_ADD_HURT")
    );

    //gets mainstat from mainstat name to id map
    private int getMainstatId(String mainstatName, Map<String,Integer> mainstatMap) {
        if (!mainstatNameMap.containsKey(mainstatName)) {
            return 0;
        } else {
            return mainstatMap.get(mainstatNameMap.get(mainstatName));
        }
    }

    //gets the closest roll with the given substat name with the remaining substat value left, returning the closest roll id and value of it
    private List getSubstatIdClosestRoll(String substatName,String substatValue, Map<String,Map<Integer,Float>> substatMap) {
        float value;

        if (!substatNameMap.containsKey(substatName)) {
            return List.of(0,0f);
        }

        //catch non float type substat
        try {
            value = Float.parseFloat(substatValue);
        }
        catch (NumberFormatException e) {
            return List.of(0,0f);
        }

        int closestId = 0;
        float closestValue = 0f;
        for (Map.Entry<Integer,Float> entry: substatMap.get(substatNameMap.get(substatName)).entrySet()) {
            if (entry.getValue() < value && entry.getValue() > closestValue) { //iterrates to get the max value that is smaller than parsed stats
                closestId = entry.getKey();
                closestValue = entry.getValue();
            }
        }
        //returns in [int,float]
        return List.of(closestId,closestValue);
    }
    public GameItem execute(Player sender, Player targetPlayer, List<String> args) {
        // Sanity check
        if (targetPlayer == null) {
            targetPlayer = sender;
        }

        //mainstatmap formation
        Map<String,Integer> mainstatMap = new HashMap<>();
        for (Map.Entry<Integer,ReliquaryMainPropData> entry : GameData.getReliquaryMainPropDataMap().entrySet()) {
            mainstatMap.put(entry.getValue().getFightProp().toString(),entry.getKey());
        }

        //substatmap formation
        Map<String,Map<Integer,Float>> substatMap = new HashMap<String,Map<Integer,Float>>();
        Int2ObjectMap<ReliquaryAffixData> loadSubstatMap = GameData.getReliquaryAffixDataMap();
        for (Map.Entry<Integer,ReliquaryAffixData> entry2 : loadSubstatMap.entrySet()) {
            if (substatMap.get(entry2.getValue().getFightProp().toString()) != null) {
                Map<Integer,Float> temp = substatMap.get(entry2.getValue().getFightProp().toString());
                temp.put(entry2.getValue().getId(),entry2.getValue().getPropValue());
                substatMap.remove(entry2.getValue().getFightProp().toString());
                substatMap.put(entry2.getValue().getFightProp().toString(),temp);

            } else {
                Map<Integer,Float> map = new HashMap<>();
                map.put(entry2.getValue().getId(),entry2.getValue().getPropValue());
                substatMap.put(entry2.getValue().getFightProp().toString(),map);
            }
        }
        // Get the artifact piece ID from the arguments.
        int itemId;

        try {
            itemId = Integer.parseInt(args.remove(0)); //not an itemId at all
        } catch (NumberFormatException ignored) {
            CommandHandler.sendMessage(sender, "this itemId does not belong to an artifact");
            return null;
        }

        ItemData itemData = GameData.getItemDataMap().get(itemId); //not an artifact
        if (itemData != null) {
            if (itemData.getItemType() != ItemType.ITEM_RELIQUARY) {
                CommandHandler.sendMessage(sender, "this itemId does not belong to an artifact");
                return null;
            }
        } else {
            CommandHandler.sendMessage(sender, "This is not even a valid item wtf, can u type properly thanks.");
            return null;
        }

        // Get the main stat name from the arguments, and remove it
        String mainstatIdString;
        try {
            String temp = args.get(0);    // test the waters
            //incase have substat no mainstat, thus  skip
            if (temp.split(",").length == 2 || temp.split("=").length == 2) {
                mainstatIdString = "0";
            } else {
                mainstatIdString = args.remove(0);
            }
        } catch (IndexOutOfBoundsException e) {
            mainstatIdString = "0";
        }
        int mainstatId = 0;  // allows for no mainstat at all

        // Convert this main stat name to id by retrieving from mainpropmap
        if (mainstatIdString.equals("0")) {
            CommandHandler.sendMessage(sender,"you did not input a mainstat,thus it is set to NONE");
        } else if (getMainstatId(mainstatIdString, mainstatMap) != 0) {
            mainstatId = getMainstatId(mainstatIdString, mainstatMap);
        } else {
            CommandHandler.sendMessage(sender,"This main stat does not exist, generating artifact with no mainstat"); // allows no mainstat
        }

        // Get the level from the arguments.
        int level = 21;  // default 21 for retards who dont know how to specify this
        try {
            int lastArgument = Integer.parseInt(args.get(args.size()-1));
            if (lastArgument > 0 && lastArgument < 22) {  // Luckily appendPropIds aren't in the range of [1,21]
                level = lastArgument;
                args.remove(args.size()-1);
            } else {
                CommandHandler.sendMessage(sender,"No level specified, auto set to level 20");
            }
        } catch (NumberFormatException ignored) {  // Could be a statname roll once,times string so no need to panic
            CommandHandler.sendMessage(sender,"No level specified, auto set to level 20");
        } catch (IndexOutOfBoundsException e) {    // could be a dumb fuck who dont know how type numbers
            CommandHandler.sendMessage(sender,"No level specified, auto set to level 20");
        }

        // Get substats from parems.
        ArrayList<Integer> substatIdList = new ArrayList<>();

        try {
            // Every remaining argument is a substat.
            args.forEach(statWithValue -> {
                // Split the string into substatName and substatValue if that is the case here.
                String[] arr;
                String substatName = "";
                String remainder;
                List statInfo;

                //splits and check stats to add to substatlist
                if ((arr = statWithValue.split(",")).length == 2 || (arr = statWithValue.split("=")).length == 2) {
                    if (substatNameMap.get(arr[0]) != null) {
                        substatName = arr[0];
                    } else {
                        CommandHandler.sendMessage(sender, "this substatname is invalid");
                        arr[1] = "0";
                    }
                    //makes sure that % arent 100 times of what it shld be [hp,atk,def,em]
                    if (Float.parseFloat(arr[1]) > 0) {
                        if (substatNameMap.get(substatName).equals("FIGHT_PROP_HP") || substatNameMap.get(substatName).equals("FIGHT_PROP_DEFENSE") || substatNameMap.get(substatName).equals("FIGHT_PROP_ATTACK") || substatNameMap.get(substatName).equals("FIGHT_PROP_ELEMENT_MASTERY")) {  // dont need to divide 100, the rest of stats are all in %
                            remainder = arr[1];
                        } else {
                            remainder = String.valueOf( (Float.parseFloat(arr[1]) + 0.1f) / 100);
                        }
                    } else {
                        remainder = "0"; //ignorestats neg(-)
                        CommandHandler.sendMessage(sender, "The stat inputted " + arr[0] + " was below 0, which doesnt make sense, so the stat will be ignored");
                    }
                    //adds substats to substatlist until remainder is 0
                    while (Float.parseFloat(remainder) >= 0f && !getSubstatIdClosestRoll(substatName, remainder, substatMap).equals(List.of(0,0f))) {
                        statInfo = getSubstatIdClosestRoll(substatName, remainder, substatMap);
                        substatIdList.add(Integer.parseInt(statInfo.get(0).toString()));
                        remainder = String.valueOf((Float.parseFloat(remainder) - Float.parseFloat(statInfo.get(1).toString())));
                    }
                } else {
                    CommandHandler.sendMessage(sender,"mistyped stat value for " + statWithValue + ",this ignoring it");
                }
            });
        } catch (NumberFormatException e) {
            CommandHandler.sendMessage(sender, "substat value for one of the inputted substats do not have a numerical value, thus this substat is ignored");
        } catch (IllegalArgumentException e) {
            CommandHandler.sendMessage(sender,"type a valid substat value for frick sake");
            return null;
        } catch (Exception e) {
            CommandHandler.sendMessage(sender, e + ".\nomg error occured, please open an issue in the github page and report it there thanks:]");    //catch unknown error for now debug
        }

        //check if empty for substats coz why not
        if (substatIdList.size() == 0) {
            CommandHandler.sendMessage(sender, "this artifact has no substats since u didnt specify them at all, for some reason");
        }

        // Create item for the artifact.
        GameItem item = new GameItem(itemData);
        item.setLevel(level);
        item.setMainPropId(mainstatId);
        item.getAppendPropIdList().clear();
        item.getAppendPropIdList().addAll(substatIdList);
        targetPlayer.getInventory().addItem(item, ActionReason.SubfieldDrop);

        //shows mainstat in string format
        String finalMainstatInString = "NONE";
        if (mainstatId != 0) {
            finalMainstatInString = GameData.getReliquaryMainPropDataMap().get(mainstatId).getFightProp().toString();
        }
        //calculates stats added
        List<Map<String,FightProperty>> finalSubstatsAdded = new ArrayList<>();
        Float tempTotal = 0f;
        String finalTotal;
        int previousSubstatId = 0;
        if (substatIdList.size() != 0) {  // check if substats are even available
            FightProperty currentStatBeforeSwitch = GameData.getReliquaryAffixDataMap().get(substatIdList.get(0)).getFightProp();
            for (int substatId : substatIdList) {
                if (currentStatBeforeSwitch.toString().equals(GameData.getReliquaryAffixDataMap().get(substatId).getFightProp().toString())) {
                    tempTotal = tempTotal + GameData.getReliquaryAffixDataMap().get(substatId).getPropValue();
                    previousSubstatId = substatId;
                } else {
                    Map<String,FightProperty> tempMap = new HashMap<>();
                    if (GameData.getReliquaryAffixDataMap().get(previousSubstatId).getFightProp().equals(FightProperty.FIGHT_PROP_ATTACK) || GameData.getReliquaryAffixDataMap().get(previousSubstatId).getFightProp().equals(FightProperty.FIGHT_PROP_HP) || GameData.getReliquaryAffixDataMap().get(previousSubstatId).getFightProp().equals(FightProperty.FIGHT_PROP_DEFENSE) || GameData.getReliquaryAffixDataMap().get(previousSubstatId).getFightProp().equals(FightProperty.FIGHT_PROP_ELEMENT_MASTERY)) {
                        finalTotal = String.valueOf(tempTotal); // no need x100
                    } else {
                        finalTotal = tempTotal * 100 + "%";
                    }
                    tempMap.put(finalTotal,GameData.getReliquaryAffixDataMap().get(previousSubstatId).getFightProp());
                    finalSubstatsAdded.add(tempMap);
                    tempTotal = 0f;
                    tempTotal = tempTotal + GameData.getReliquaryAffixDataMap().get(substatId).getPropValue();
                    currentStatBeforeSwitch = GameData.getReliquaryAffixDataMap().get(substatId).getFightProp();
                    previousSubstatId = substatId;
                }
            }
            // final appended
            Map<String,FightProperty> tempMap = new HashMap<>();
            if (GameData.getReliquaryAffixDataMap().get(previousSubstatId).getFightProp().equals(FightProperty.FIGHT_PROP_ATTACK) || GameData.getReliquaryAffixDataMap().get(previousSubstatId).getFightProp().equals(FightProperty.FIGHT_PROP_HP) || GameData.getReliquaryAffixDataMap().get(previousSubstatId).getFightProp().equals(FightProperty.FIGHT_PROP_DEFENSE) || GameData.getReliquaryAffixDataMap().get(previousSubstatId).getFightProp().equals(FightProperty.FIGHT_PROP_ELEMENT_MASTERY)) {
                finalTotal = String.valueOf(tempTotal); // no need x100
            } else {
                finalTotal = tempTotal * 100 + "%";
            }
            tempMap.put(finalTotal,GameData.getReliquaryAffixDataMap().get(previousSubstatId).getFightProp());
            finalSubstatsAdded.add(tempMap);
            tempTotal = 0f;
            tempTotal = tempTotal + GameData.getReliquaryAffixDataMap().get(previousSubstatId).getPropValue();
            currentStatBeforeSwitch = GameData.getReliquaryAffixDataMap().get(previousSubstatId).getFightProp();
        }


        String finalStatsInString = "";
        for (Map<String,FightProperty> map : finalSubstatsAdded) {
            finalStatsInString = finalStatsInString + map.entrySet().iterator().next().getValue() + " : " + map.entrySet().iterator().next().getKey() + "\n";
        }
        if (finalStatsInString.equals("")) {
            finalStatsInString = "NONE";
        }

        //handles actual give command for future usage?
        StringBuilder giveCommandArgs = new StringBuilder("/g ");
        giveCommandArgs.append(item.getItemId()).append(" ").append(mainstatId).append(" ");
        int tempRollCount = 1;
        if (substatIdList.size() > 0) {
            for (int i = 0 ; i < substatIdList.size() - 1; i++) {
                if (substatIdList.get(i+1).equals(substatIdList.get(i))) {
                    tempRollCount += 1;
                } else {
                    giveCommandArgs.append(substatIdList.get(i)).append(",").append(tempRollCount).append(" ");
                    tempRollCount = 1;
                }
            }
            // handle last element
            if (substatIdList.get(substatIdList.size()-2).equals(substatIdList.get(substatIdList.size()-1))) {
                tempRollCount += 1;
                giveCommandArgs.append(substatIdList.get(substatIdList.size() - 1)).append(",").append(tempRollCount).append(" ").append(level);
                tempRollCount = 1;
            } else {
                tempRollCount = 1;
                giveCommandArgs.append(substatIdList.get(substatIdList.size() - 1)).append(",").append(tempRollCount).append(" ").append(level);
            }
        } else {
            giveCommandArgs.append(level);
        }

        CommandHandler.sendMessage(sender, "The artifact has been added to your inventory!\n\nArtifact ID : " + itemId + "\nTarget Player : @" + targetPlayer.getUid() + "\n\nMainstat:\n" + finalMainstatInString + "\n\nSubstats:\n" + finalStatsInString + "\nLevel : " + level + "\n\n\nIf you wanted use normal give command, this would be your input\n" + giveCommandArgs);
        return item;
    }
}