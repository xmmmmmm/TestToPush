package testmod.seccult;

import net.minecraft.item.Item;

public class CommonProxy 
{

	public void registerItemRenderer(Item item, int meta, String id) {}
	
	public void registerVariantRenderer(Item item, int meta, String filename, String id) {}
	
	public void sphereRender() {}
	
	public void cylinderRender() {}
}