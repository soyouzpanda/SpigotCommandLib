package io.github.mrblobman.spigotcommandlib;

import org.bukkit.permissions.Permissible;

import java.util.*;

public class SubCommand {
	private Map<String, SubCommand> subCommands = new HashMap<>();
	private String name;
	private SubCommand superCommand;
	private Set<String> permissions;
	private List<String> aliases;
	
	SubCommand(String name, String[] aliases, String permission, SubCommand superCommand, SubCommand... subCommands) {
		this.name = name;
		this.aliases = new ArrayList<>();
		for (String alias : aliases) {
			this.aliases.add(alias.toLowerCase());
		}
		this.permissions = new HashSet<>();
		this.permissions.add(permission);
		for (SubCommand cmd : subCommands) {
			this.subCommands.put(cmd.getName().toLowerCase(), cmd);
		}
		this.superCommand = superCommand;
	}
	
	public String getName() {
		return this.name;
	}
	
	public List<String> getAliases() {
		return Collections.unmodifiableList(this.aliases);
	}
	
	/**
	 * Check if {@code caller} has permission to execute this {@code SubCommand}
	 * and all super commands.
	 * @param caller the caller executing the sub command
	 * @return true iff the called has permission to execute this and all super commands.
	 */
	public boolean canExecute(Permissible caller) {
		for (String permission : this.permissions) {
			if (caller.hasPermission(permission) && (this.isBase() || this.superCommand.canExecute(caller))) return true;
		}
		return false;
	}
	
	public void addPermission(String permission) {
		this.permissions.add(permission);
	}
	
	public boolean removePermission(String permission) {
		return this.permissions.remove(permission);
	}

	public SubCommand getSubCommand(String name) {
		String lowerCaseName = name.toLowerCase();
		SubCommand cmd = this.subCommands.get(lowerCaseName);
		if (cmd != null) return cmd;
		else {
			for (SubCommand sub : this.subCommands.values()) {
				if (sub.getAliases().contains(lowerCaseName)) {
					return sub;
				}
			}
		}
		return null;
	}
	
	public List<String> getSubCommands() {
        List<String> subCommands = new ArrayList<>();
        subCommands.addAll(this.subCommands.keySet());
        return subCommands;
	}
	
	public void addSubCommand(SubCommand cmd) {
		this.subCommands.put(cmd.getName().toLowerCase(), cmd);
	}
	
	/**
	 * The super command that directly leads this command.
	 * @return null if this SubCommand is a BaseCommand
	 */
	public SubCommand getSuperCommand() {
		return this.superCommand;
	}

	public boolean isBase() {
		return this.superCommand == null;
	}
	
	@Override
	public String toString() {
        String name = this.name;
        if (!this.aliases.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String alias : aliases) sb.append("|").append(alias);
            name = name + sb.toString();
            //JDK8+ name = name + "|" + this.aliases.stream().collect(Collectors.joining("|"));
        }
        if (this.superCommand == null) {
			return "/"+name;
		} else {
			return this.superCommand.toString()+" "+name;
		}
	}

	public String toExecutableString() {
		String name = this.name;
		if (this.superCommand == null) {
			return "/"+name;
		} else {
			return this.superCommand.toString()+" "+name;
		}
	}
}