package com.k0.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class PickaxeSaverConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("pickaxesaver.json");
	private static boolean enabled = true;
	private static int durabilityThreshold = 30;
	private static final List<String> customItems = new ArrayList<>();

	private PickaxeSaverConfig() {
	}

	public static void load() {
		if (!Files.exists(CONFIG_FILE)) {
			save();
			return;
		}

		try {
			ConfigData loaded = GSON.fromJson(Files.readString(CONFIG_FILE), ConfigData.class);
			if (loaded != null) {
				enabled = loaded.enabled();
				durabilityThreshold = Math.max(1, Math.min(50, loaded.durabilityThreshold()));
				customItems.clear();
				if (loaded.customItems() != null) customItems.addAll(loaded.customItems());
			}
		} catch (IOException | JsonParseException exception) {
			enabled = true;
			durabilityThreshold = 30;
			customItems.clear();
			save();
		}
	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static void setEnabled(boolean value) {
		enabled = value;
		save();
	}

	public static int getDurabilityThreshold() {
		return durabilityThreshold;
	}

	public static void setDurabilityThreshold(int value) {
		durabilityThreshold = Math.max(1, Math.min(50, value));
		save();
	}

	public static List<String> getCustomItems() {
		return List.copyOf(customItems);
	}

	public static boolean addCustomItem(String itemId) {
		if (itemId == null || itemId.isBlank() || customItems.contains(itemId)) {
			return false;
		}
		customItems.add(itemId);
		save();
		return true;
	}

	public static void removeCustomItem(String itemId) {
		if (customItems.remove(itemId)) save();
	}

	public static boolean protectsCustomItem(String itemId) {
		return customItems.contains(itemId);
	}

	public static String getCustomItem(int index) {
		return customItems.isEmpty() ? null : customItems.get(Math.floorMod(index, customItems.size()));
	}

	public static int getCustomItemCount() {
		return customItems.size();
	}

	public static void clearCustomItems() {
		customItems.clear();
		save();
	}

	private static void save() {
		try {
			Files.createDirectories(CONFIG_FILE.getParent());
			Files.writeString(CONFIG_FILE, GSON.toJson(new ConfigData(enabled, durabilityThreshold,
				customItems)));
		} catch (IOException exception) {
			System.err.println("Could not save PickaxeSaver config: " + exception.getMessage());
		}
	}

	private record ConfigData(boolean enabled, int durabilityThreshold, List<String> customItems) {
	}
}