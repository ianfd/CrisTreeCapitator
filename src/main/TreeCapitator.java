package main;

import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import objs.Configuration;
import updater.Updater;

public class TreeCapitator extends JavaPlugin implements Listener {
	private PluginDescriptionFile desc = getDescription();

	private final ChatColor mainColor = ChatColor.BLUE;
	private final ChatColor textColor = ChatColor.AQUA;
	private final ChatColor accentColor = ChatColor.DARK_AQUA;
	private final ChatColor errorColor = ChatColor.DARK_RED;
	private final String header = mainColor + "[" + desc.getName() + "] " + textColor;

	// Ajustes
	private Configuration config;
	private static final String STRG_MAX_BLOCKS = "destroy limit";
	private int maxBlocks = -1;
	private static final String STRG_VIP_MODE = "vip mode";
	private boolean vipMode = false;
	private static final String STRG_REPLANT = "replant";
	private boolean replant = true;
	private static final String STRG_AXE_NEEDED = "axe needed";
	private boolean axeNeeded = true;

	// Updater
	private static final int ID = 294976;
	private static Updater updater;
	public static boolean update = false;

	private boolean checkUpdate() {
		updater = new Updater(this, ID, this.getFile(), Updater.UpdateType.NO_DOWNLOAD, false);
		update = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE;

		return update;
	}

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);

		if (checkUpdate()) {
			getServer().getConsoleSender()
					.sendMessage(header + ChatColor.GREEN
							+ "An update is available, use /tc update to update to the lastest version (from v"
							+ desc.getVersion() + " to v" + updater.getRemoteVersion() + ")");
		}

		config = new Configuration("plugins/CrisTreeCapitator/config.yml", "Cristichi's TreeCapitator");
		loadConfiguration();
		saveConfiguration();
		getLogger().info("Enabled");
	}

	private void loadConfiguration() {
		config.reloadConfig();
		
		maxBlocks = config.getInt(STRG_MAX_BLOCKS, maxBlocks);
		config.setInfo(STRG_MAX_BLOCKS, "Sets the maximun number of logs and leaves that can be destroyed at once. -1 to unlimit.");
		
		vipMode = config.getBoolean(STRG_VIP_MODE, vipMode);
		config.setInfo(STRG_VIP_MODE, "Sets vip mode. If enabled, a permission node (cristreecapitator.vip) is required to take down trees at once.");
		
		replant = config.getBoolean(STRG_REPLANT, replant);
		config.setInfo(STRG_REPLANT, "Sets if trees should be replanted automatically.");
		
		axeNeeded = config.getBoolean(STRG_AXE_NEEDED, axeNeeded);
		config.setInfo(STRG_AXE_NEEDED, "Sets if an axe is required to Cut down trees at once.");
	}

	private void saveConfiguration() {
		try {
			config.setValue(STRG_MAX_BLOCKS, maxBlocks);
			config.setValue(STRG_REPLANT, replant);
			config.setValue(STRG_VIP_MODE, vipMode);
			config.setValue(STRG_AXE_NEEDED, axeNeeded);
			config.saveConfig();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDisable() {
		getLogger().info("Disabled");
	}

	@EventHandler
	private void onBlockBreak(BlockBreakEvent e) {
		Block primero = e.getBlock();
		Material tipo = primero.getBlockData().getMaterial();

		if ((vipMode && e.getPlayer().hasPermission("cristreecapitator.vip") || !vipMode)
				&& (tipo.name().contains("LOG") /* || tipo.name().contains("LEAVES") */)) {

			try {
				boolean cutDown = true;
				if (axeNeeded) {
					PlayerInventory inv = e.getPlayer().getInventory();
					ItemStack mano = inv.getItemInMainHand();
					if (!mano.getType().name().contains("_AXE")) {
						cutDown = false;
					}
				}
				if (cutDown) {
					if (replant) {
						breakRecReplant(primero, tipo, 0);
					} else {
						breakRecNoReplant(primero, tipo, 0);
					}
					e.setCancelled(true);
					// nt destr = breakRec(primero, tipo, 0);
					// e.getPlayer().sendMessage(header + "Destroyed " + destr + ".");
				}
			} catch (StackOverflowError e1) {
			}
		}
	}

	private int breakRecNoReplant(Block lego, Material type, int destroyed) {
		Material tipo = lego.getBlockData().getMaterial();
		if (tipo.name().contains("LOG") || tipo.name().contains("LEAVES")) {
			if (destroyed > maxBlocks && maxBlocks > 0) {
				return destroyed;
			}
			World mundo = lego.getWorld();
			if (lego.breakNaturally()) {
				destroyed++;
			} else
				return destroyed;

			int x = lego.getX(), y = lego.getY(), z = lego.getZ();

			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecNoReplant(mundo.getBlockAt(x, y - 1, z), type, destroyed);
			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecNoReplant(mundo.getBlockAt(x, y + 1, z), type, destroyed);

			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecNoReplant(mundo.getBlockAt(x + 1, y, z + 1), type, destroyed);
			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecNoReplant(mundo.getBlockAt(x + 1, y, z - 1), type, destroyed);
			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecNoReplant(mundo.getBlockAt(x - 1, y, z + 1), type, destroyed);
			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecNoReplant(mundo.getBlockAt(x - 1, y, z - 1), type, destroyed);

			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecNoReplant(mundo.getBlockAt(x + 1, y, z), type, destroyed);
			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecNoReplant(mundo.getBlockAt(x, y, z + 1), type, destroyed);

			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecNoReplant(mundo.getBlockAt(x - 1, y, z), type, destroyed);
			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecNoReplant(mundo.getBlockAt(x, y, z - 1), type, destroyed);
		}

		return destroyed;
	}

	private int breakRecReplant(Block lego, Material type, int destroyed) {
		Material tipo = lego.getBlockData().getMaterial();
		if (tipo.name().contains("LOG") || tipo.name().contains("LEAVES")) {
			if (maxBlocks > 0 && destroyed > maxBlocks) {
				return destroyed;
			}
			World mundo = lego.getWorld();
			int x = lego.getX(), y = lego.getY(), z = lego.getZ();
			Block below = mundo.getBlockAt(x, y - 1, z);
			if (below.getType().equals(Material.DIRT) || below.getType().equals(Material.GRASS_BLOCK)) {
				switch (lego.getType()) {
				case ACACIA_LOG:
					lego.setType(Material.ACACIA_SAPLING);
					break;
				case BIRCH_LOG:
					lego.setType(Material.BIRCH_SAPLING);
					break;
				case DARK_OAK_LOG:
					lego.setType(Material.DARK_OAK_SAPLING);
					break;
				case JUNGLE_LOG:
					lego.setType(Material.JUNGLE_SAPLING);
					break;
				case OAK_LOG:
					lego.setType(Material.OAK_SAPLING);
					break;
				case SPRUCE_LOG:
					lego.setType(Material.SPRUCE_SAPLING);
					break;
				default:
					if (lego.breakNaturally()) {
						destroyed++;
					} else
						return destroyed;
					break;
				}
			} else {
				if (lego.breakNaturally()) {
					destroyed++;
				} else
					return destroyed;
			}

			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecReplant(mundo.getBlockAt(x, y - 1, z), type, destroyed);
			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecReplant(mundo.getBlockAt(x, y + 1, z), type, destroyed);

			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecReplant(mundo.getBlockAt(x + 1, y, z + 1), type, destroyed);
			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecReplant(mundo.getBlockAt(x + 1, y, z - 1), type, destroyed);
			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecReplant(mundo.getBlockAt(x - 1, y, z + 1), type, destroyed);
			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecReplant(mundo.getBlockAt(x - 1, y, z - 1), type, destroyed);

			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecReplant(mundo.getBlockAt(x + 1, y, z), type, destroyed);
			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecReplant(mundo.getBlockAt(x, y, z + 1), type, destroyed);

			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecReplant(mundo.getBlockAt(x - 1, y, z), type, destroyed);
			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecReplant(mundo.getBlockAt(x, y, z - 1), type, destroyed);
		}

		return destroyed;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		boolean bueno = label.equals(command.getLabel());
		String[] cmds = command.getAliases().toArray(new String[] {});
		for (int i = 0; i < cmds.length && !bueno; i++) {
			if (label.equals(cmds[i])) {
				bueno = true;
			}
		}

		boolean sinPermiso = false;
		if (bueno) {
			if (args.length > 0) {
				switch (args[0]) {

				case "help":
					sender.sendMessage(header + "Commands:\n" + accentColor + "/" + label + " help:" + textColor
							+ " Shows this help message.\n" + accentColor + "/" + label + " update:" + textColor
							+ " Updates the plugin if there is a new version.\n" + accentColor + "/" + label
							+ " setlimit <number>:" + textColor
							+ " Sets the block limit to break each time. Negative number for unlimited.\n" + accentColor
							+ "/" + label + " vipmode <true/false>:" + textColor
							+ " Enables or disables Vip Mode (if cristreecapitator.vip is needed to take down trees at once)");

					break;

				case "limit":
				case "setlimit":
				case "blocklimit":
					if (sender.hasPermission("cristreecapitator.admin")) {
						if (args.length != 2) {
//							sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
//									+ " <number>" + textColor + ".");

							sender.sendMessage(header + "Blocks destroyed at once limit is currently " + accentColor
									+ maxBlocks + textColor + ".");
						} else {
							try {
								int nuevoMax = Integer.parseInt(args[1]);
								maxBlocks = nuevoMax < 0 ? -1 : nuevoMax;
								config.setValue(STRG_MAX_BLOCKS, maxBlocks);
								try {
									config.saveConfig();
									sender.sendMessage(
											header + "Limit set to " + (nuevoMax < 0 ? "unbounded" : nuevoMax) + ".");
								} catch (IOException e) {
									sender.sendMessage(header + errorColor
											+ "Error trying to save the value in the configuration file.");
									e.printStackTrace();
								}
							} catch (NumberFormatException e) {
								sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
										+ " <number>" + textColor + ". (" + accentColor + args[1] + textColor
										+ " is not a valid number)");
							}
						}
					} else {
						sinPermiso = true;
					}

					break;

				case "setvipmode":
				case "vipmode":
				case "vipneeded":
					if (sender.hasPermission("cristreecapitator.admin")) {
						if (args.length != 2) {
							sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
									+ " <true/false/yes/no>" + textColor + ".");
						} else {
							switch (args[1]) {
							case "true":
							case "yes":
								vipMode = true;
								break;
							case "false":
							case "no":
								vipMode = false;
								break;

							default:
								sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
										+ " <true/false/yes/no>" + textColor + ". (" + accentColor + args[1] + textColor
										+ " is not a valid argument)");
								break;
							}
							config.setValue(STRG_VIP_MODE, vipMode);
							try {
								config.saveConfig();
								sender.sendMessage(header + "Vip mode " + accentColor
										+ (vipMode ? "enabled" : "disabled") + textColor + ".");
							} catch (IOException e) {
								sender.sendMessage(header + errorColor
										+ "Error trying to save the value in the configuration file.");
								e.printStackTrace();
							}
						}
					} else {
						sinPermiso = true;
					}

					break;

				case "setreplant":
				case "replant":
					if (sender.hasPermission("cristreecapitator.admin")) {
						if (args.length != 2) {
							sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
									+ " <true/false/yes/no>" + textColor + ".");
						} else {
							switch (args[1]) {
							case "true":
							case "yes":
								replant = true;
								break;
							case "false":
							case "no":
								replant = false;
								break;

							default:
								sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
										+ " <true/false/yes/no>" + textColor + ". (" + accentColor + args[1] + textColor
										+ " is not a valid argument)");
								break;
							}
							config.setValue(STRG_REPLANT, replant);
							try {
								config.saveConfig();
								sender.sendMessage(header + "Replanting " + accentColor
										+ (replant ? "enabled" : "disabled") + textColor + ".");
							} catch (IOException e) {
								sender.sendMessage(header + errorColor
										+ "Error trying to save the value in the configuration file.");
								e.printStackTrace();
							}
						}
					} else {
						sinPermiso = true;
					}

					break;

				case "setaxe":
				case "axeneeded":
					if (sender.hasPermission("cristreecapitator.admin")) {
						if (args.length != 2) {
							sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
									+ " <true/false/yes/no>" + textColor + ".");
						} else {
							switch (args[1]) {
							case "true":
							case "yes":
								axeNeeded = true;
								break;
							case "false":
							case "no":
								axeNeeded = false;
								break;

							default:
								sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
										+ " <true/false/yes/no>" + textColor + ". (" + accentColor + args[1] + textColor
										+ " is not a valid argument)");
								break;
							}
							config.setValue(STRG_AXE_NEEDED, axeNeeded);
							try {
								config.saveConfig();
								sender.sendMessage(header + "Replanting " + accentColor
										+ (replant ? "enabled" : "disabled") + textColor + ".");
							} catch (IOException e) {
								sender.sendMessage(header + errorColor
										+ "Error trying to save the value in the configuration file.");
								e.printStackTrace();
							}
						}
					} else {
						sinPermiso = true;
					}

					break;

				case "reload":
					if (sender.hasPermission("cristreecapitator.admin")) {
						loadConfiguration();
						sender.sendMessage(header + "Configuration loaded from file.");
					} else {
						sinPermiso = true;
					}

					break;

				case "update":
					if (sender.hasPermission("cristreecapitator.admin")) {
						if (checkUpdate()) {
							sender.sendMessage(header + "Updating CrisTreeCapitator...");
							updater = new Updater(this, ID, this.getFile(), Updater.UpdateType.DEFAULT, true);
							updater.getResult();
							sender.sendMessage(
									header + "Use " + accentColor + "/reload" + textColor + " to apply changes.");
						} else {
							sender.sendMessage(header + "This plugin is already up to date.");
						}
					} else {
						sinPermiso = true;
					}

					break;

				default:
					sender.sendMessage(
							header + errorColor + "Command not found, please check \"/" + label + " help\".");
					break;
				}
			} else {
				return false;
			}
		}

		if (sinPermiso) {
			sender.sendMessage(header + errorColor + "You don't have permission to use this command.");
		}
		return bueno;
	}
}
