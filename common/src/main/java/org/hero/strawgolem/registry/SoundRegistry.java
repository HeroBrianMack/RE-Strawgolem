package org.hero.strawgolem.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.hero.strawgolem.Constants;

import java.util.function.Supplier;

public final class SoundRegistry {
	public static void init() {}

	public static Supplier<SoundEvent> GOLEM_AMBIENT = registerSound("golem_ambient", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Constants.MODID, "golem_ambient")));
	public static Supplier<SoundEvent> GOLEM_HURT = registerSound("golem_hurt", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Constants.MODID, "golem_hurt")));
	public static Supplier<SoundEvent> GOLEM_DEATH = registerSound("golem_death", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Constants.MODID, "golem_death")));
	public static Supplier<SoundEvent> GOLEM_INTERESTED = registerSound("golem_interested", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Constants.MODID, "golem_interested")));
	public static Supplier<SoundEvent> GOLEM_SCARED = registerSound("golem_scared", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Constants.MODID, "golem_scared")));
	public static Supplier<SoundEvent> GOLEM_STRAINED = registerSound("golem_strained", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Constants.MODID, "golem_strained")));
	public static Supplier<SoundEvent> GOLEM_HAPPY = registerSound("golem_happy", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Constants.MODID, "golem_happy")));
	public static Supplier<SoundEvent> GOLEM_DISGUSTED = registerSound("golem_disgusted", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Constants.MODID, "golem_disgusted")));

	private static <T extends SoundEvent> Supplier<T> registerSound(String id, Supplier<T> sound) {
		return Constants.COMMON_PLATFORM.registerSound(id, sound);
	}
}