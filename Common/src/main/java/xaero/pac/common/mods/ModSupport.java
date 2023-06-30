/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2023, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.mods;

import xaero.pac.common.mods.prometheus.Prometheus;

public abstract class ModSupport {

	public boolean LUCK_PERMS;
	private LuckPerms luckPerms;
	public boolean FTB_RANKS;
	public boolean PROMETHEUS;
	private FTBRanks ftbRanks;
	private Prometheus prometheus;
	public boolean FTB_TEAMS;
	private FTBTeams ftbTeams;

	public void check(boolean client){
		if(!client) {
			try {
				Class.forName("net.luckperms.api.LuckPerms");
				LUCK_PERMS = true;
				luckPerms = new LuckPerms();
			} catch (ClassNotFoundException e) {
			}
		}
		try {
			Class.forName("dev.ftb.mods.ftbranks.api.FTBRanksAPI");
			FTB_RANKS = true;
			ftbRanks = new FTBRanks();
		} catch (ClassNotFoundException e) {
		}
		try {
			Class.forName("earth.terrarium.prometheus.api.permissions.PermissionApi");
			PROMETHEUS = true;
			prometheus = new Prometheus(client);
		} catch (ClassNotFoundException e) {
		}
		try {
			Class.forName("dev.ftb.mods.ftbteams.api.FTBTeamsAPI");
			FTB_TEAMS = true;
			ftbTeams = new FTBTeams();
		} catch (ClassNotFoundException e) {
		}
	}

	public LuckPerms getLuckPerms() {
		return luckPerms;
	}

	public FTBRanks getFTBRanksSupport(){
		return ftbRanks;
	}

	public Prometheus getPrometheusSupport() {
		return prometheus;
	}

	public FTBTeams getFTBTeamsSupport() {
		return ftbTeams;
	}

	public void init() {
		if(PROMETHEUS)
			prometheus.init();
	}

	public void initClient() {
		if(PROMETHEUS)
			prometheus.initClient();
	}

}
