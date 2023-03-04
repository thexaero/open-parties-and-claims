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

package xaero.pac.client.patreon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.PlayerModelPart;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.patreon.decrypt.DecryptInputStream;

import javax.crypto.Cipher;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

public class Patreon {

	private static final ResourceLocation cape1 = new ResourceLocation("xaeropatreon", "capes/cape1.png");
	private static final ResourceLocation cape2 = new ResourceLocation("xaeropatreon", "capes/cape2.png");
	private static final ResourceLocation cape3 = new ResourceLocation("xaeropatreon", "capes/cape3.png");
	private static final ResourceLocation cape4 = new ResourceLocation("xaeropatreon", "capes/cape4.png");
	private static final File optionsFile = new File("./config/xaeropatreon.txt");

	private static final HashMap<Integer, ArrayList<String>> patrons = new HashMap<Integer, ArrayList<String>>();//pledge amount -> patron name(s)

	private static boolean pauseCapes = false;
	private static boolean loaded = false;
	private static boolean showCapes = true;
	private static int patronPledge = -1;

	private static Cipher cipher = null;
	private static int KEY_VERSION = 4;
	private static String publicKeyString = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoBeELcruvAEIeLF/UsWF/v5rxyRXIpCs+eORLCbDw5cz9jHsnoypQKx0RTk5rcXIeA0HbEfY0eREB25quHjhZKul7MnzotQT+F2Qb1bPfHa6+SPie+pj79GGGAFP3npki6RqoU/wyYkd1tOomuD8v5ytEkOPC4U42kxxvx23A7vH6w46dew/E/HvfbBvZF2KrqdJtwKAunk847C3FgyhVq8/vzQc6mqAW6Mmn4zlwFvyCnTOWjIRw/I93WIM/uvhE3lt6pmtrWA2yIbKIj1z4pgG/K72EqHfYLGkBFTh7fV1wwCbpNTXZX2JnTfmvMGqzHjq7FijwVfCpFB/dWR3wQIDAQAB";//TODO PUBLIC KEY
	private static boolean shouldChill = false;
	public static boolean optifine = false;

	static {
		try {
			Class.forName("xaero.common.patreon.Patreon");
			shouldChill = true;
		} catch (ClassNotFoundException e) {
		}
		try {
			Class.forName("xaero.map.patreon.Patreon");
			shouldChill = true;
		} catch (ClassNotFoundException e) {
		}
		try {
			Class.forName("optifine.Patcher");
			optifine = true;
		} catch (ClassNotFoundException e) {
		}
		if(!shouldChill)
			try {
				cipher = Cipher.getInstance("RSA");
				KeyFactory factory = KeyFactory.getInstance("RSA");
				byte[] byteKey = Base64.getDecoder().decode(getPublicKeyString().getBytes());
				X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
				PublicKey publicKey = factory.generatePublic(X509publicKey);
				cipher.init(Cipher.DECRYPT_MODE, publicKey);
			} catch (Exception e) {
				cipher = null;
				OpenPartiesAndClaims.LOGGER.error("suppressed exception", e);
			}
	}

	public static void checkPatreon(){
		if(shouldChill)
			return;
		synchronized(patrons) {
			if(loaded)
				return;
			loadSettings();
			String s = "http://data.chocolateminecraft.com/Versions_" + KEY_VERSION + "/Patreon.dat";
			s = s.replaceAll(" ", "%20");
			URL url;
			try {
				url = new URL(s);
				BufferedReader reader;
				URLConnection conn = url.openConnection();
				conn.setReadTimeout(900);
				conn.setConnectTimeout(900);
				if(conn.getContentLengthLong() > 524288)
					throw new IOException("Input too long to trust!");
				reader = new BufferedReader(new InputStreamReader(new DecryptInputStream(conn.getInputStream(), cipher)));
				String line;
				int pledge = -1;
				while((line = reader.readLine()) != null && !line.equals("LAYOUTS")){
					if(line.startsWith("PATREON")){
						pledge = Integer.parseInt(line.substring(7));
						patrons.put(pledge, new ArrayList<String>());
						continue;
					}
					if(pledge == -1)
						continue;
					String args[] = line.split("\\t");
					patrons.get(pledge).add(args[0]);
					if(args[0].equalsIgnoreCase(Minecraft.getInstance().getUser().getGameProfile().getName()))
						setPatronPledge(pledge);
				}
				reader.close();
			} catch (Throwable e) {
				OpenPartiesAndClaims.LOGGER.error("suppressed exception", e);
				patrons.clear();
			} finally {
				loaded = true;
			}
		}
	}

	public static int getPatronPledge(String name){
		if(shouldChill)
			return -1;
		Integer[] keys = patrons.keySet().toArray(new Integer[0]);
		for(int i = 0; i < keys.length; i++)
			if(patrons.get(keys[i]).contains(name))
				return keys[i];
		return -1;
	}

	public static void saveSettings() {
		if(shouldChill)
			return;
		PrintWriter writer;
		try {
			writer = new PrintWriter(new FileWriter(optionsFile));
			writer.println("showCapes:" + showCapes);
			writer.close();
		} catch (IOException e) {
			OpenPartiesAndClaims.LOGGER.error("suppressed exception", e);
		}
	}

	public static void loadSettings() {
		if(shouldChill)
			return;
		try {
			if (!optionsFile.exists()) {
				saveSettings();
				return;
			}

			BufferedReader reader = new BufferedReader(new FileReader(optionsFile));
			String line;
			while((line = reader.readLine()) != null){
				String[] args = line.split(":");
				if(args[0].equalsIgnoreCase("showCapes"))
					showCapes = args[1].equals("true");
			}
			reader.close();
		} catch (IOException e) {
			OpenPartiesAndClaims.LOGGER.error("suppressed exception", e);
		}
	}

	public static ResourceLocation getPlayerCape(AbstractClientPlayer playerEntity) {//returning null means use vanilla cape
		if(shouldChill)
			return null;
		if(!pauseCapes && showCapes) {
			ResourceLocation cape = null;
			int pledge = getPatronPledge(playerEntity.getName().getString());
			if(pledge == 2)
				cape = cape1;
			else if(pledge == 5)
				cape = cape2;
			else if(pledge == 10)
				cape = cape3;
			else if(pledge == 50)
				cape = cape4;
			if(cape == null)
				return null;
			pauseCapes = true;
			ResourceLocation realCape = playerEntity.getCloakTextureLocation();
			boolean realIsWearing = playerEntity.isModelPartShown(PlayerModelPart.CAPE);
			pauseCapes = false;
			if(realCape != null && realIsWearing)
				return realCape;
			return cape;
		}
		return null;
	}

	public static Boolean isWearingCape(AbstractClientPlayer playerEntity) {//returning null means use vanilla value
		if(shouldChill)
			return null;
		if(!pauseCapes && showCapes) {
			pauseCapes = true;
			ResourceLocation realCape = playerEntity.getCloakTextureLocation();
			boolean realIsWearing = playerEntity.isModelPartShown(PlayerModelPart.CAPE);
			pauseCapes = false;
			if(realIsWearing || realCape == null)
				return realIsWearing;
			int pledge = getPatronPledge(playerEntity.getName().getString());
			return pledge >= 2;
		}
		return null;
	}

	public static int getPatronPledge() {
		if(shouldChill)
			return -1;
		return patronPledge;
	}

	public static void setPatronPledge(int patronPledge) {
		if(shouldChill)
			return;
		Patreon.patronPledge = patronPledge;
	}

	public static String getPublicKeyString() {
		return publicKeyString;
	}

	public static int getKEY_VERSION() {
		return KEY_VERSION;
	}

}
