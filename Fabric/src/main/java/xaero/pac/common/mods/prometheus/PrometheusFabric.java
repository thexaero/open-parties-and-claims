/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2023-2023, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.mods.prometheus;

import earth.terrarium.prometheus.api.roles.options.RoleOptionsApi;
import xaero.pac.client.mods.prometheus.PrometheusFabricClient;
import xaero.pac.common.server.player.permission.PlayerPrometheusPermissionsFabric;

public class PrometheusFabric extends Prometheus {

	private final boolean client;

	public PrometheusFabric(boolean client) {
		super(new PlayerPrometheusPermissionsFabric());
		this.client = client;
	}

	@Override
	public void init() {
		RoleOptionsApi.API.register(OPACOptionsFabric.SERIALIZER);
	}

	@Override
	public void initClient(){
		if(client)
			new PrometheusFabricClient().init();
	}

}
