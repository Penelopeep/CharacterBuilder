package pene.gc.charbuild.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.inventory.GameItem;
import emu.grasscutter.game.player.Player;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Datareader {
    public static List<GameItem> artifacts(Player targetPlayer, String avatarName, String buildName) {
        List<GameItem> artifactsList = new ArrayList<>();
        String filepath = String.format("%s/CharacterBuilder/builds.json", Grasscutter.getConfig().folderStructure.plugins);
        File file1 = new File(filepath);
        try (
                InputStream inputStream = new DataInputStream(new FileInputStream(file1));
                InputStreamReader streamReader = new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(streamReader)) {
            JsonElement newJson = new JsonParser().parse(reader);
            JsonElement avatar = newJson.getAsJsonObject().get(avatarName);
            if (avatar == null) return null;
            JsonElement build = avatar.getAsJsonObject().get(buildName);
            if (build == null) return null;
            JsonArray artifactsCodes = build.getAsJsonArray();
            ModifiedGiveCommand giveCommand = new ModifiedGiveCommand();
            ModifiedSnooGive snooCommand = new ModifiedSnooGive();
            List<String> args = new ArrayList<>();
            for (int i = 0; i < artifactsCodes.size(); i++) {
                args.addAll(List.of(artifactsCodes.get(i).getAsString().split(" ")));
                if (artifactsCodes.get(i).toString().contains("lv20")) {
                    artifactsList.add(giveCommand.execute(null, targetPlayer, args));
                } else {
                    artifactsList.add(snooCommand.execute(null, targetPlayer, args));
                }
                args.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return artifactsList;
    }

    public static String builds(String avatarName) {
        String builds = "";
        String filepath = String.format("%s/CharacterBuilder/builds.json", Grasscutter.getConfig().folderStructure.plugins);
        File file1 = new File(filepath);
        try (
                InputStream inputStream = new DataInputStream(new FileInputStream(file1));
                InputStreamReader streamReader = new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(streamReader)) {
            JsonElement newJson = new JsonParser().parse(reader);
            JsonElement avatar = newJson.getAsJsonObject().get(avatarName);
            if (avatar == null) return "No builds found for this avatar";
            Set<String> buildNames = avatar.getAsJsonObject().keySet();
            for (String build : buildNames) {
                builds = String.format("%s, %s", build, builds);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builds;
    }
}
