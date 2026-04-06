/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2026, Xaero <xaero1996@gmail.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of version 3 of the GNU Lesser General Public License
 * (LGPL-3.0-only) as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received copies of the GNU Lesser General Public License
 * and the GNU General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package xaero.pac.common.server.player.config.io.serialization.updater;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import xaero.pac.common.server.player.config.io.serialization.updater.add.PlayerConfigOpConfigurableAdder;
import xaero.pac.common.server.player.config.io.serialization.updater.rename.PlayerConfigOptionRenamer;
import xaero.pac.common.server.player.config.io.serialization.updater.value.PlayerConfigValueChanger;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PlayerConfigTransformers {

	public static void init(List<IPlayerConfigTransformer> transformers){
		Function<String, String> playerGroupUpdateOptionRenamer = oldPath -> {
			String optionName = oldPath.substring(oldPath.lastIndexOf('.') + 1);
			String updatedName;
			if(optionName.startsWith("from"))
				updatedName = (optionName.charAt(4) + "").toLowerCase() + optionName.substring(5);
			else
				updatedName = optionName.replace("From", "By");
			return "playerConfig.claims.protection.exceptions." + updatedName;
		};
		Map<Object, String> protectionToPlayerGroupException = ImmutableMap.of(
				0, "E",
				1, "N",
				2, "P",
				3, "A"
		);
		List<String> optionsToUpdate1 = Lists.newArrayList(
			"playerConfig.claims.protection.buttonsFromProjectiles",
				"playerConfig.claims.protection.targetsFromProjectiles",
				"playerConfig.claims.protection.platesFromPlayers",
				"playerConfig.claims.protection.platesFromMobs",
				"playerConfig.claims.protection.platesFromOther",
				"playerConfig.claims.protection.tripwireFromPlayers",
				"playerConfig.claims.protection.tripwireFromMobs",
				"playerConfig.claims.protection.tripwireFromOther"
		);
		transformers.add(//1
				new PlayerConfigOptionRenamer(
						optionsToUpdate1::contains,
						playerGroupUpdateOptionRenamer
				)
		);

		List<String> optionsToUpdate2 = Lists.newArrayList(
				"playerConfig.claims.protection.exceptions.buttonsByProjectiles",
				"playerConfig.claims.protection.exceptions.targetsByProjectiles",
				"playerConfig.claims.protection.exceptions.platesByPlayers",
				"playerConfig.claims.protection.exceptions.platesByMobs",
				"playerConfig.claims.protection.exceptions.platesByOther",
				"playerConfig.claims.protection.exceptions.tripwireByPlayers",
				"playerConfig.claims.protection.exceptions.tripwireByMobs",
				"playerConfig.claims.protection.exceptions.tripwireByOther"
		);
		transformers.add(//2
				new PlayerConfigValueChanger(
						optionsToUpdate2::contains,
						(c, i) -> protectionToPlayerGroupException.getOrDefault(i, "N")
				)
		);

		String exceptionGroupsPrefix3 = "playerConfig.claims.protection.exceptionGroups.";
		transformers.add(//3
				new PlayerConfigOptionRenamer(
						o -> o.startsWith(exceptionGroupsPrefix3),
						oldPath -> {
							String relativePath = oldPath.substring(exceptionGroupsPrefix3.length());
							return "playerConfig.claims.protection.exceptions.groups." + relativePath;
						}
				)
		);

		transformers.add(//4
				new PlayerConfigOpConfigurableAdder(ImmutableList.of("bonusPlayerGroups", "bonusPlayerGroupSpace"))
		);

		List<String> optionsToUpdate5 = Lists.newArrayList(
				"playerConfig.claims.protection.blocksFromPlayers",
				"playerConfig.claims.protection.blocksFromMobs",
				"playerConfig.claims.protection.blocksFromOther",
				"playerConfig.claims.protection.blocksRedirect",
				"playerConfig.claims.protection.fromFrostWalking",
				"playerConfig.claims.protection.entitiesFromPlayers",
				"playerConfig.claims.protection.entitiesFromMobs",
				"playerConfig.claims.protection.entitiesFromOther",
				"playerConfig.claims.protection.entitiesRedirect",
				"playerConfig.claims.protection.playerLightning",
				"playerConfig.claims.protection.chorusFruitTeleport",
				"playerConfig.claims.protection.netherPortalsPlayers",
				"playerConfig.claims.protection.netherPortalsMobs",
				"playerConfig.claims.protection.netherPortalsOther",
				"playerConfig.claims.protection.itemUse",
				"playerConfig.claims.protection.itemTossPlayers",
				"playerConfig.claims.protection.itemTossMobs",
				"playerConfig.claims.protection.itemTossOther",
				"playerConfig.claims.protection.mobLoot",
				"playerConfig.claims.protection.playersFromPlayers",
				"playerConfig.claims.protection.playersFromMobs",
				"playerConfig.claims.protection.playersFromOther",
				"playerConfig.claims.protection.playersRedirect",
				"playerConfig.claims.protection.itemTossRedirect",
				"playerConfig.claims.protection.itemPickupPlayers",
				"playerConfig.claims.protection.itemPickupMobs",
				"playerConfig.claims.protection.itemPickupRedirect",
				"playerConfig.claims.protection.xpPickup",
				"playerConfig.claims.protection.projectileHitHostileSpawn",
				"playerConfig.claims.protection.projectileHitFriendlySpawn",
				"playerConfig.claims.protection.cropTrample",
				"playerConfig.claims.protection.entitiesFromExplosions",
				"playerConfig.claims.protection.entitiesFromFire",
				"playerConfig.claims.protection.raids",
				"playerConfig.claims.protection.naturalSpawnHostile",
				"playerConfig.claims.protection.naturalSpawnFriendly",
				"playerConfig.claims.protection.spawnersHostile",
				"playerConfig.claims.protection.spawnersFriendly",
				"playerConfig.claims.protection.blocksFromExplosions"
		);
		transformers.add(//5
				new PlayerConfigOptionRenamer(
						optionsToUpdate5::contains,
						playerGroupUpdateOptionRenamer
				)
		);

		List<String> optionsToUpdate6 = Lists.newArrayList(
				"playerConfig.claims.protection.exceptions.blocksByPlayers",
				"playerConfig.claims.protection.exceptions.blocksByMobs",
				"playerConfig.claims.protection.exceptions.blocksByOther",
				"playerConfig.claims.protection.exceptions.frostWalking",
				"playerConfig.claims.protection.exceptions.entitiesByPlayers",
				"playerConfig.claims.protection.exceptions.entitiesByMobs",
				"playerConfig.claims.protection.exceptions.entitiesByOther",
				"playerConfig.claims.protection.exceptions.playerLightning",
				"playerConfig.claims.protection.exceptions.chorusFruitTeleport",
				"playerConfig.claims.protection.exceptions.netherPortalsPlayers",
				"playerConfig.claims.protection.exceptions.netherPortalsMobs",
				"playerConfig.claims.protection.exceptions.netherPortalsOther",
				"playerConfig.claims.protection.exceptions.itemUse",
				"playerConfig.claims.protection.exceptions.itemTossPlayers",
				"playerConfig.claims.protection.exceptions.itemTossMobs",
				"playerConfig.claims.protection.exceptions.itemTossOther",
				"playerConfig.claims.protection.exceptions.mobLoot",
				"playerConfig.claims.protection.exceptions.itemPickupPlayers",
				"playerConfig.claims.protection.exceptions.itemPickupMobs",
				"playerConfig.claims.protection.exceptions.xpPickup"
		);
		transformers.add(//6
				new PlayerConfigValueChanger(
						optionsToUpdate6::contains,
						(c, i) -> protectionToPlayerGroupException.getOrDefault(i, "N")
				)
		);

		transformers.add(//7
				new PlayerConfigOptionRenamer(
						s -> s.equals("playerConfig.claims.protection.fromParty"),
						s -> "playerConfig.claims.protection.exceptions.fullAccess"
				)
		);

		transformers.add(//8
				new PlayerConfigValueChanger(
						s -> s.equals("playerConfig.claims.protection.exceptions.fullAccess"),
						(c, s) -> {
							Boolean protectFromParty = (Boolean) s;
							if(protectFromParty)
								return "N";
							Boolean protectFromAllies = c.get("playerConfig.claims.protection.fromAllyParties");
							return protectFromAllies ? "P" : "A";
						}
				)
		);

		List<String> optionsToUpdate9 = Lists.newArrayList(
				"playerConfig.claims.protection.exceptions.playersByPlayers",
				"playerConfig.claims.protection.exceptions.playersByMobs",
				"playerConfig.claims.protection.exceptions.playersByOther",
				"playerConfig.claims.claims.protection.exceptions.cropTrample",
				"playerConfig.claims.protection.exceptions.entitiesByExplosions",
				"playerConfig.claims.protection.exceptions.entitiesByFire",
				"playerConfig.claims.protection.exceptions.raids",
				"playerConfig.claims.protection.exceptions.naturalSpawnHostile",
				"playerConfig.claims.protection.exceptions.naturalSpawnFriendly",
				"playerConfig.claims.protection.exceptions.spawnersHostile",
				"playerConfig.claims.protection.exceptions.spawnersFriendly",
				"playerConfig.claims.protection.exceptions.blocksByExplosions"
		);

		transformers.add(//9
				new PlayerConfigValueChanger(
						optionsToUpdate9::contains,
						(c, o) -> {
							Boolean protectionOn = (Boolean) o;
							return !protectionOn;
						}
				)
		);

		String barrierGroupsPrefix10 = "playerConfig.claims.protection.exceptions.groups.entity.barrier.";
		transformers.add(//10
				new PlayerConfigValueChanger(
						o -> o.startsWith(barrierGroupsPrefix10),
						(c, i) -> protectionToPlayerGroupException.getOrDefault(i, "E")
				)
		);

		Map<Object, String> exceptionToPlayerGroupException = ImmutableMap.of(
				0, "N",
				1, "P",
				2, "A",
				3, "E"
		);
		String exceptionGroupsPrefix11 = "playerConfig.claims.protection.exceptions.groups.";//dynamic exception options
		transformers.add(//11
				new PlayerConfigValueChanger(
						o -> o.startsWith(exceptionGroupsPrefix11) && !o.contains(".barrier."),
						(c, i) -> exceptionToPlayerGroupException.getOrDefault(i, "N")
				)
		);
	}

}
