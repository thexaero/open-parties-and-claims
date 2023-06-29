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
import xaero.pac.common.mods.prometheus.PrometheusFabric;

public class ModSupportFabric extends ModSupport {

	public boolean FABRIC_PERMISSIONS;
	private FabricPermissions fabricPermissions;

	@Override
	public void check(boolean client) {
		super.check(client);
		try {
			Class.forName("me.lucko.fabric.api.permissions.v0.Permissions");
			fabricPermissions = new FabricPermissions();
			FABRIC_PERMISSIONS = true;
		} catch (ClassNotFoundException e) {
		}

	}

	public FabricPermissions getFabricPermissionsSupport() {
		return fabricPermissions;
	}

	@Override
	protected Prometheus createPrometheusSupport(boolean client) {
		return new PrometheusFabric(client);
	}

}
