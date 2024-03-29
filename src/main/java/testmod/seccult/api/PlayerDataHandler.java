package testmod.seccult.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import testmod.seccult.Seccult;
import testmod.seccult.init.ModDamage;
import testmod.seccult.network.NetworkHandler;
import testmod.seccult.network.NetworkPlayerWandData;

public class PlayerDataHandler {
	
	private static HashMap<Integer, PlayerData> playerData = new HashMap();
	
	public static PlayerData get(EntityPlayer player) {
		int key = getKey(player);
		if(!playerData.containsKey(key))
			playerData.put(key, new PlayerData(player));

		PlayerData data = playerData.get(key);
		if(data != null && data.player != null && data.player != player) {
			NBTTagCompound cmp = new NBTTagCompound();
			data.writeToNBT(cmp);
			playerData.remove(key);
			data = get(player);
			data.readFromNBT(cmp);
		}

		return data;
	}
	
	public static void cleanup() {
		List<Integer> removals = new ArrayList();
		Iterator<Entry<Integer, PlayerData>> it = playerData.entrySet().iterator();
		while(it.hasNext()) {
			Entry<Integer, PlayerData> item = it.next();
			PlayerData d = item.getValue();
			if(d != null && d.player == null)
				removals.add(item.getKey());
		}

		for(int i : removals)
			playerData.remove(i);
	}
	
	private static int getKey(EntityPlayer player) {
		return player.hashCode() << 1 + (player.getEntityWorld().isRemote ? 1 : 0);
	}
	
	public static NBTTagCompound getDataCompoundForPlayer(EntityPlayer player) {
		NBTTagCompound forgeData = player.getEntityData();
		if(!forgeData.hasKey(EntityPlayer.PERSISTED_NBT_TAG))
			forgeData.setTag(EntityPlayer.PERSISTED_NBT_TAG, new NBTTagCompound());

		NBTTagCompound persistentData = forgeData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
		if(!persistentData.hasKey(Seccult.Data))
			persistentData.setTag(Seccult.Data, new NBTTagCompound());

		return persistentData.getCompoundTag(Seccult.Data);
	}
	
	public static class PlayerData{
		static Random rand = new Random();
		private static final String TAG_MANA_TALENT_VALUE = "ManaTalentValue";
		private static final String TAG_COMTROL_ABILITY = "ControlAbility";
		private static final String TAG_MANA_STRENGH = "ManaStrengh";
		private static final String TAG_GROWTH_ABILITY = "GrowthAbility";
		
		private static final String TAG_MANA_VALUE = "ManaValue";
		private static final String TAG_MAX_MANA_VALUE = "MaxManaValue";
		
		private static final String TAG_PROFICIENCY_LEVEL = "proficiency";
		
		private float ManaTalentValue;
		private float ControlAbility;
		private float ManaStrengh;
		private float GrowthAbility;
		
		private float ManaValue;
		private float MaxManaValue;
		private float proficiency;
		
		private float regenCooldown;
		
		private int color2;
		private int color3;
		private int color4;
		
		private int[] magickData = {0};
		
		private int wand;
		
		public EntityPlayer player;
		private final boolean client;
		
		public PlayerData(EntityPlayer player) {
			this.player = player;
			client = player.getEntityWorld().isRemote;

			load();
		}
		
		public void load() {
			if(!client) {
				if(this.player != null) {
					NBTTagCompound cmp = getDataCompoundForPlayer(this.player);
					readFromNBT(cmp);
				}
			}
		}
		
		public void readFromNBT(NBTTagCompound cmp) {
			ManaTalentValue = cmp.getFloat(TAG_MANA_TALENT_VALUE);
			MaxManaValue = cmp.getFloat(TAG_MAX_MANA_VALUE);
			ControlAbility = cmp.getFloat(TAG_COMTROL_ABILITY);
			ManaStrengh = cmp.getFloat(TAG_MANA_STRENGH);
			GrowthAbility = cmp.getFloat(TAG_GROWTH_ABILITY);
			ManaValue = cmp.getFloat(TAG_MANA_VALUE);
			proficiency = cmp.getFloat(TAG_PROFICIENCY_LEVEL);
			
			wand = cmp.getInteger("WandStyle");
			
			color2 = cmp.getInteger("Color2");
			color3 = cmp.getInteger("Color3");
			color4 = cmp.getInteger("Color4");
			
			NetworkHandler.getNetwork().sendToAll(new NetworkPlayerWandData(color2, color3, color4, player.getUniqueID(), wand));
			
			if(ManaTalentValue == 0)
			{
				ArrayList<Float> list = new ArrayList<Float>();
				for(int i = 0; i < 155; i++) 
				{
					if(i <= 9)
						list.add(5F);
					else if(i <= 29)
						list.add(3.5F);
					else if(i <= 59)
						list.add(2.5F);
					else if(i <= 99)
						list.add(2F);
					else if(i <= 149)
						list.add(1.5F);
					else if(i <= 154)
						list.add(7F);
				}
				float y = rand.nextFloat() * list.get(rand.nextInt(155));
				if(y < 1)
					y = 1;
				ManaTalentValue = y;
			}
			
			if(MaxManaValue < 100)
				MaxManaValue = 100;
			
			if(ControlAbility == 0)
				ControlAbility = ManaTalentValue / 1+rand.nextFloat();
			
			if(ManaStrengh == 0)
				ManaStrengh = ManaTalentValue / 1+rand.nextFloat();
			
			if(GrowthAbility == 0 )
				GrowthAbility = ManaTalentValue / 1+rand.nextFloat();
		}
		
		public void save() {
			if(!client) {
				if(this.player != null) {
					NBTTagCompound cmp = getDataCompoundForPlayer(player);
					writeToNBT(cmp);
				}
			}
		}

		public void writeToNBT(NBTTagCompound cmp) {
			
			if(!cmp.hasKey("WandStyle"))
				cmp.setInteger("WandStyle", rand.nextInt(6) + 1);
				
				if(!cmp.hasKey("Color2") || !cmp.hasKey("Color3") || !cmp.hasKey("Color4"))
				{
					cmp.setInteger("Color2", getColor());
					cmp.setInteger("Color3", getColor());
					cmp.setInteger("Color4", getColor());
				}
			
			cmp.setFloat(TAG_MANA_TALENT_VALUE, ManaTalentValue);
			cmp.setFloat(TAG_COMTROL_ABILITY, ControlAbility);
			cmp.setFloat(TAG_MANA_STRENGH, ManaStrengh);
			cmp.setFloat(TAG_GROWTH_ABILITY, GrowthAbility);
			
			cmp.setFloat(TAG_MANA_VALUE, ManaValue);
			cmp.setFloat(TAG_MAX_MANA_VALUE, MaxManaValue);
			
			cmp.setFloat(TAG_PROFICIENCY_LEVEL, proficiency);
		}
		
		public static int getColor(){
	        String red;
	        String green;
	        String blue;

	        int co = rand.nextInt(3);
	        int co2 = rand.nextInt(2);
	        
	        if(co == 0) {
	        	if(co2 == 0) 
	        	{
	        		red = Integer.toHexString(rand.nextInt(86) + 170).toUpperCase();
	        		green = Integer.toHexString(rand.nextInt(80) + 80).toUpperCase();
	        		blue = Integer.toHexString(rand.nextInt(80) + 80).toUpperCase();
	        	}
	        	else
	        	{
			        red = Integer.toHexString(rand.nextInt(80) + 80).toUpperCase();
			        green = Integer.toHexString(rand.nextInt(86) + 170).toUpperCase();
		    		blue = Integer.toHexString(rand.nextInt(86) + 170).toUpperCase();
	        	}
	        }
	        else if(co == 1)
	        {	
	        	if(co2 == 0) 
	        	{
	        		red = Integer.toHexString(rand.nextInt(80) + 80).toUpperCase();
	        		green = Integer.toHexString(rand.nextInt(86) + 170).toUpperCase();
	        		blue = Integer.toHexString(rand.nextInt(80) + 80).toUpperCase();
	        	}
	        	else
	        	{
			        red = Integer.toHexString(rand.nextInt(86) + 170).toUpperCase();
			        green = Integer.toHexString(rand.nextInt(80) + 80).toUpperCase();
		    		blue = Integer.toHexString(rand.nextInt(86) + 170).toUpperCase();
	        	}
	        }
	        else
	        {
	        	if(co2 == 0) 
	        	{
	        		red = Integer.toHexString(rand.nextInt(80) + 80).toUpperCase();
	        		green = Integer.toHexString(rand.nextInt(80) + 80).toUpperCase();
	        		blue = Integer.toHexString(rand.nextInt(86) + 170).toUpperCase();
	        	}
	        	else
	        	{
			        red = Integer.toHexString(rand.nextInt(86) + 170).toUpperCase();
			        green = Integer.toHexString(rand.nextInt(86) + 170).toUpperCase();
		    		blue = Integer.toHexString(rand.nextInt(80) + 80).toUpperCase();
	        	}
	        }
	        
	        red = red.length()==1 ? "0" + red : red ;
	        green = green.length()==1 ? "0" + green : green ;
	        blue = blue.length()==1 ? "0" + blue : blue ;
	        
	        String color = red+green+blue;
	        return Integer.parseInt(color, 16);
	    }
		
		private float getRegenPerTick() {
			float Regen = (float) (Math.pow(ManaValue + ControlAbility, 1.0005) - ManaValue);
			return Regen;
		}

		public void levelUpper() 
		{
			MaxManaValue += ManaTalentValue + 1+rand.nextFloat() / GrowthAbility;
			proficiency+=5;
		}
		
		public void addAttributeValue()
		{
			if(ManaTalentValue > 0.11)
			proficiency++;
			
			if(proficiency > ManaTalentValue * 100)
			{
				ManaTalentValue -= 0.1;
				if(ManaTalentValue < 0.1)
					ManaTalentValue = 0.1F;
				proficiency = 0;
				
				ControlAbility += GrowthAbility / ControlAbility;
				ManaStrengh += GrowthAbility / ManaStrengh;
				GrowthAbility += ManaTalentValue / 1+rand.nextFloat();
			}
		}
		
		public void reduceMana(float mana)
		{
			if(!player.isCreative())
			ManaValue -= mana;
		}
		
		public void setColor(int color2, int color3, int color4, int wand)
		{
			this.color2 = color2;
			this.color3 = color3;
			this.color4 = color4;
			this.wand = wand;
		}
		
		public void setMagickData(NBTTagList idList)
		{
			int amount = idList.tagCount();
			int[] NewData = new int[amount];
			for(int i = 0; i < idList.tagCount(); i++)
			{
				NewData[i] = idList.getIntAt(i);
			}
			magickData = newMagickList(NewData);
		}
		
	    private int[] newMagickList(int[] list)
	    {
	    	int max = 0;
	    	int slot = 0;
	    	int[] NewList = new int[list.length];
	    	for(int i = list.length - 1; i >= 0; i--)
	    	{
	    		for(int z = 0; z < list.length; z++)
	    		{
	    			if(list[z] >= max)
	    			{
	    				max = list[z];
	    				slot = z;
	    			}
	    		}
	    		NewList[i] = max;
				list[slot] = 0;
				slot = 0;
				max = 0;
	    	}
			return NewList;
	    }
		
		public void addMagickData(int id)
		{
			int[] NewData = new int[magickData.length + 1];
			for(int i = 0; i < magickData.length; i++)
			{
				NewData[i] = magickData[i];
			}
			NewData[magickData.length + 1] = id;
			magickData = NewData;
		}
		
		public int[] getAllMagickData()
		{
			return magickData;
		}
		
		public void addCoolDown(float CoolDown)
		{
			regenCooldown -= CoolDown;
		}
		
		public float getMaxMana() {
			return MaxManaValue;
		}
		
		public float getMana() {
			return ManaValue;
		}
		
		public float getManaTalent() {
			return ManaTalentValue;
		}
		public float getManaStrengh() {
			return ManaStrengh;
		}
		public float getGrowth() {
			return GrowthAbility;
		}
		public float getControlAbility() {
			return ControlAbility;
		}
		public void tick() {
			if(regenCooldown == 0) {
					ManaValue = Math.min(MaxManaValue, ManaValue + getRegenPerTick());
					save();
			} else {
				regenCooldown -= (float)Math.sqrt(GrowthAbility) -  1;
				save();
			}
			
			if(ManaValue <= 0)
			{
				player.attackEntityFrom(ModDamage.MagickOverLoad, (float)Math.sqrt(0 - ManaValue));
				ManaValue = 0;
			}
		}

		public int getColor2() {
			return color2;
		}
		
		public int getColor3() {
			return color3;
		}
		
		public int getColor4() {
			return color4;
		}
		
		public int getWandStyle() {
			return wand;
		}
	}
}
