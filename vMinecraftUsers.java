import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Scanner;

public class vMinecraftUsers {
    private static volatile vMinecraftUsers instance;
    protected static final Logger log = Logger.getLogger("Minecraft");
    String file = "vminecraftusers.txt";
    private PropertiesFile properties;
    String location = "vminecraftusers.txt";
    public void loadUsers(){
        File theDir = new File("vminecraftusers.txt");
		if(!theDir.exists()){
			properties = new PropertiesFile("vminecraftusers.txt");
			FileWriter writer = null;
			try {
				writer = new FileWriter(location);
				writer.write("#Storage place for user information\r\n");
                                writer.write("#username:nickname:suffix:tag:ignore,list,names:alias,commands,here\r\n");
			} catch (Exception e) {
				log.log(Level.SEVERE, "Exception while creating " + location, e);
			} finally {
				try {
					if (writer != null) {
						writer.close();
					}
				} catch (IOException e) {
					log.log(Level.SEVERE, "Exception while closing writer for " + location, e);
				}
			}

		} else {
			properties = new PropertiesFile("vminecraftusers.txt");
			try {
				properties.load();
			} catch (IOException e) {
				log.log(Level.SEVERE, "Exception while loading vminecraftusers.txt", e);
			}
		}
    }

        public boolean doesPlayerExist(String player) {
        try {
            Scanner scanner = new Scanner(new File(location));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("#") || line.equals("") || line.startsWith("﻿")) {
                    continue;
                }
                String[] split = line.split(":");
                if (!split[0].equalsIgnoreCase(player)) {
                    continue;
                }
                return true;
            }
            scanner.close();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Exception while reading " + location + " (Are you sure you formatted it correctly?)", e);
        }
        return false;
    }
    public Player getPlayer(String name) {
        Player player = new Player();
        try {
            Scanner scanner = new Scanner(new File(location));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("#") || line.equals("") || line.startsWith("﻿")) {
                    continue;
                }
                String[] split = line.split(":");
                if (!split[0].equalsIgnoreCase(name)) {
                    continue;
                }
                if (split.length >= 2) {
                    //player.setNickname(split[1]
                }
                if (split.length >= 3) {
                    //player.setSuffix(split[2]);
                }
                if (split.length >= 5) {
                    //player.setIgnoreList(split[4].split(","));
                }
                if (split.length >= 6) {
                    //player.setAlias(split[5].split(","));
                }
            }
            scanner.close();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Exception while reading " + location + " (Are you sure you formatted it correctly?)", e);
        }
        return player;
    }
    public static void addUser(Player player){
        FileWriter writer = null;
        String location = "vminecraftusers.txt";
        String playerName = player.getName();
        if (!vMinecraftUsers.getInstance().doesPlayerExist(playerName)){ //Check to see if the player exists before writing
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(location, true));
            bw.append(player.getName()+":::::\r");
            bw.newLine();
            bw.close();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Exception while trying to add user with BufferedWriter to " + location, e);
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				log.log(Level.SEVERE, "Exception while closing BufferedWriter to " + location, e);
			}
		}
    }
    }
    public static vMinecraftUsers getInstance() {
		if (instance == null) {
			instance = new vMinecraftUsers();
		}
		return instance;
	}
    public static void getRow(){

    }
}

//=====================================================================
//Class:	PlayerList
//Use:		Encapsulates the player list
//Author:	cerevisiae
//=====================================================================
class PlayerList
{
	ArrayList<PlayerProfile> players;
	
	//=====================================================================
	//Function:	PlayerList
	//Input:	Player player: The player to create a profile object for
	//Output:	none
	//Use:		Initializes the ArrayList
	//=====================================================================
	public PlayerList() { players = new ArrayList<PlayerProfile>(); }

	//=====================================================================
	//Function:	addPlayer
	//Input:	Player player: The player to add
	//Output:	None
	//Use:		Add a profile of the specified player
	//=====================================================================
	public void addPlayer(Player player)
	{
		players.add(new PlayerProfile(player));
	}

	//=====================================================================
	//Function:	removePlayer
	//Input:	Player player: The player to remove
	//Output:	None
	//Use:		Remove the profile of the specified player
	//=====================================================================
	public void removePlayer(Player player)
	{
		players.remove(findProfile(player));
	}

	//=====================================================================
	//Function:	findProfile
	//Input:	Player player: The player to find's profile
	//Output:	PlayerProfile: The profile of the specified player
	//Use:		Get the profile for the specified player
	//=====================================================================
	private PlayerProfile findProfile(Player player)
	{
		for(PlayerProfile ply : players)
		{
			if(ply.getPlayer().equals(player))
				return ply;
		}
		return null;
	}
	
	//=====================================================================
	//Class:	PlayerProfile
	//Use:		Encapsulates all commands for player options
	//Author:	cerevisiae
	//=====================================================================
	class PlayerProfile
	{
		private Player playerName;
		private String nickName;
		private String tag;
		private ArrayList<Player> ignoreList;
		private commandList aliasList;
		
	    static final int EXIT_FAIL		= 0,
			 			 EXIT_SUCCESS	= 1,
			 			 EXIT_CONTINUE	= 2;

		//=====================================================================
		//Function:	PlayerProfile
		//Input:	Player player: The player to create a profile object for
		//Output:	none
		//Use:		Loads settings for the player or creates them if they don't
		//			exist.
		//=====================================================================
		public PlayerProfile(Player player)
		{
			ignoreList = new ArrayList<Player>();
			aliasList = new commandList();
		}

		//=====================================================================
		//Function:	getPlayer
		//Input:	None
		//Output:	Player: The player this profile belongs to
		//Use:		Finds if the specified player is in the ignore list
		//=====================================================================
		public Player getPlayer(){return playerName;}

		//=====================================================================
		//Function:	isIgnored
		//Input:	Player player: Checks if a player is ignored
		//Output:	boolean: If they're ignored
		//Use:		Finds if the specified player is in the ignore list
		//=====================================================================
		public boolean isIgnored(Player player){return ignoreList.contains(player);}

		//=====================================================================
		//Function:	addIgnore
		//Input:	Player name: The player to ignore
		//Output:	None
		//Use:		Ignores a player.
		//=====================================================================
		public void addIgnore(Player name)
		{
			if(!ignoreList.contains(name))
				ignoreList.add(name);
		}

		//=====================================================================
		//Function:	removeIgnore
		//Input:	Player name: The player to ignore
		//Output:	None
		//Use:		Ignores a player.
		//=====================================================================
		public void removeIgnore(Player name)
		{
			if(ignoreList.contains(name))
				ignoreList.remove(name);
		}

		//=====================================================================
		//Function:	addAlias
		//Input:	String command: The command to try to call
		//			String[] args: The arguments for the command
		//Output:	None
		//Use:		Adds a command
		//=====================================================================
		public void addAlias(String name, String callCommand)
		{
			aliasList.registerAlias(name, callCommand);
		}

		//=====================================================================
		//Function:	addAlias
		//Input:	String command: The command to try to call
		//			String[] args: The arguments for the command
		//Output:	None
		//Use:		Adds a command
		//=====================================================================
		public void addAlias(String name, String callCommand, String[] args)
		{
			aliasList.registerAlias(name, callCommand, args);
		}

		//=====================================================================
		//Function:	callAlias
		//Input:	String command: The command to try to call
		//			Player player: Checks if a player is ignored
		//			String[] args: The arguments for the command
		//Output:	int: Exit code
		//Use:		Attempts to call a command
		//=====================================================================
		public int callAlias(String command, Player player, String[] args)
		{
			try
			{
				//Attemt to call the function
				return aliasList.call(command, player, args);
			}
			catch (Throwable e)
			{
				//The function wasn't found, returns fail
				return EXIT_FAIL;
			}
		}

		//=====================================================================
		//Function:	setTag
		//Input:	String newTag: The tag to set for the player
		//Output:	None
		//Use:		Sets a player tag
		//=====================================================================
		public void setTag(String newTag){ tag = newTag; }

		//=====================================================================
		//Function:	getTag
		//Input:	None
		//Output:	String: The player tag
		//Use:		Gets a player tag
		//=====================================================================
		public String getTag() { return tag; }
	}
}
