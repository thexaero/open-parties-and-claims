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
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.patreon.decrypt.DecryptInputStream;

import javax.crypto.Cipher;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Patreon {
	private static final File optionsFile = new File("./config/xaeropatreon.txt");

	private static final Object SYNC = new Object();
	private static boolean loaded = false;
	private static Cipher cipher = null;
	private static int KEY_VERSION = 4;
	private static String publicKeyString = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoBeELcruvAEIeLF/UsWF/v5rxyRXIpCs+eORLCbDw5cz9jHsnoypQKx0RTk5rcXIeA0HbEfY0eREB25quHjhZKul7MnzotQT+F2Qb1bPfHa6+SPie+pj79GGGAFP3npki6RqoU/wyYkd1tOomuD8v5ytEkOPC4U42kxxvx23A7vH6w46dew/E/HvfbBvZF2KrqdJtwKAunk847C3FgyhVq8/vzQc6mqAW6Mmn4zlwFvyCnTOWjIRw/I93WIM/uvhE3lt6pmtrWA2yIbKIj1z4pgG/K72EqHfYLGkBFTh7fV1wwCbpNTXZX2JnTfmvMGqzHjq7FijwVfCpFB/dWR3wQIDAQAB";//TODO PUBLIC KEY
	private static boolean shouldChill = true;//TODO set back to false when actually using Patreon for something
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
		synchronized(SYNC) {
			if(loaded)
				return;
			loadSettings();
			String s = "http://data.chocolateminecraft.com/Versions_" + KEY_VERSION + "/Patreon2.dat";
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
				boolean parsingPatrons = false;
				String localPlayerName = Minecraft.getInstance().getUser().getName();
				while((line = reader.readLine()) != null && !line.equals("LAYOUTS")){
					if(line.startsWith("PATREON")){
						parsingPatrons = true;
						continue;
					}
					if(!parsingPatrons)
						continue;
					String rewards[] = line.split(";");
					if(rewards.length <= 1 || !rewards[0].equalsIgnoreCase(localPlayerName))
						continue;
					for(int i = 1; i < rewards.length; i++) {
						String rewardString = rewards[i].trim();
						//rewardString checks go here
						String[] keyAndValue = rewardString.split(":");
						if(keyAndValue.length < 2)
							continue;
						//keyAndValue checks go here
					}
				}
				reader.close();
			} catch (Throwable e) {
				OpenPartiesAndClaims.LOGGER.error("suppressed exception", e);
			} finally {
				loaded = true;
			}
		}
	}

	public static void saveSettings() {
		if(shouldChill)
			return;
		PrintWriter writer;
		try {
			writer = new PrintWriter(new FileWriter(optionsFile));
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
			}
			reader.close();
		} catch (IOException e) {
			OpenPartiesAndClaims.LOGGER.error("suppressed exception", e);
		}
	}

	public static String getPublicKeyString() {
		return publicKeyString;
	}

	public static int getKEY_VERSION() {
		return KEY_VERSION;
	}

}
