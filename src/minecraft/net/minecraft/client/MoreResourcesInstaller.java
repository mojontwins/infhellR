package net.minecraft.client;

import java.io.File;
import java.net.URISyntaxException;

public class MoreResourcesInstaller {
	Minecraft mc;
	private final String resourceDirectory = "/resources/sounds";
	
	public MoreResourcesInstaller(Minecraft mc) {
		this.mc = mc;
	}
	
	public void installResources() {
		try {
			// Load custom resources
			this.mc.installResourceURL("sound/mob/zombie/wood1.ogg", this.getURLfromResource(this.resourceDirectory +"/wood1.ogg"));
			this.mc.installResourceURL("sound/mob/zombie/wood2.ogg", this.getURLfromResource(this.resourceDirectory +"/wood2.ogg"));
			this.mc.installResourceURL("sound/mob/zombie/wood3.ogg", this.getURLfromResource(this.resourceDirectory +"/wood3.ogg"));
			this.mc.installResourceURL("sound/mob/zombie/wood4.ogg", this.getURLfromResource(this.resourceDirectory +"/wood4.ogg"));
			this.mc.installResourceURL("sound/mob/zombie/woodbreak.ogg", this.getURLfromResource(this.resourceDirectory +"/woodbreak.ogg"));
			
			this.mc.installResourceURL("sound/mob/witch/death1.ogg", this.getURLfromResource(this.resourceDirectory +"/Witch_death1.ogg"));
			this.mc.installResourceURL("sound/mob/witch/death2.ogg", this.getURLfromResource(this.resourceDirectory +"/Witch_death2.ogg"));
			this.mc.installResourceURL("sound/mob/witch/drink1.ogg", this.getURLfromResource(this.resourceDirectory +"/Witch_drink1.ogg"));
			this.mc.installResourceURL("sound/mob/witch/drink2.ogg", this.getURLfromResource(this.resourceDirectory +"/Witch_drink2.ogg"));
			this.mc.installResourceURL("sound/mob/witch/hurt1.ogg", this.getURLfromResource(this.resourceDirectory +"/Witch_hurt1.ogg"));
			this.mc.installResourceURL("sound/mob/witch/hurt2.ogg", this.getURLfromResource(this.resourceDirectory +"/Witch_hurt2.ogg"));
			this.mc.installResourceURL("sound/mob/witch/hurt3.ogg", this.getURLfromResource(this.resourceDirectory +"/Witch_hurt3.ogg"));
			this.mc.installResourceURL("sound/mob/witch/idle1.ogg", this.getURLfromResource(this.resourceDirectory +"/Witch_idle1.ogg"));
			this.mc.installResourceURL("sound/mob/witch/idle2.ogg", this.getURLfromResource(this.resourceDirectory +"/Witch_idle2.ogg"));
			this.mc.installResourceURL("sound/mob/witch/idle3.ogg", this.getURLfromResource(this.resourceDirectory +"/Witch_idle3.ogg"));
			this.mc.installResourceURL("sound/mob/witch/throw1.ogg", this.getURLfromResource(this.resourceDirectory +"/Witch_throw1.ogg"));
			this.mc.installResourceURL("sound/mob/witch/throw2.ogg", this.getURLfromResource(this.resourceDirectory +"/Witch_throw2.ogg"));
			this.mc.installResourceURL("sound/mob/witch/throw3.ogg", this.getURLfromResource(this.resourceDirectory +"/Witch_throw3.ogg"));

			this.mc.installResourceURL("sound/mob/cat/hiss1.ogg", this.getURLfromResource(this.resourceDirectory +"/cat_hiss1.ogg"));
			this.mc.installResourceURL("sound/mob/cat/hiss2.ogg", this.getURLfromResource(this.resourceDirectory +"/cat_hiss2.ogg"));
			this.mc.installResourceURL("sound/mob/cat/hiss3.ogg", this.getURLfromResource(this.resourceDirectory +"/cat_hiss3.ogg"));	
			this.mc.installResourceURL("sound/mob/cat/hitt1.ogg", this.getURLfromResource(this.resourceDirectory +"/cat_hitt1.ogg"));
			this.mc.installResourceURL("sound/mob/cat/hitt2.ogg", this.getURLfromResource(this.resourceDirectory +"/cat_hitt2.ogg"));
			this.mc.installResourceURL("sound/mob/cat/hitt3.ogg", this.getURLfromResource(this.resourceDirectory +"/cat_hitt3.ogg"));
			this.mc.installResourceURL("sound/mob/cat/meow1.ogg", this.getURLfromResource(this.resourceDirectory +"/cat_meow1.ogg"));
			this.mc.installResourceURL("sound/mob/cat/meow2.ogg", this.getURLfromResource(this.resourceDirectory +"/cat_meow2.ogg"));
			this.mc.installResourceURL("sound/mob/cat/meow3.ogg", this.getURLfromResource(this.resourceDirectory +"/cat_meow3.ogg"));
			this.mc.installResourceURL("sound/mob/cat/meow4.ogg", this.getURLfromResource(this.resourceDirectory +"/cat_meow4.ogg"));
			this.mc.installResourceURL("sound/mob/cat/purr1.ogg", this.getURLfromResource(this.resourceDirectory +"/cat_purr1.ogg"));
			this.mc.installResourceURL("sound/mob/cat/purr2.ogg", this.getURLfromResource(this.resourceDirectory +"/cat_purr2.ogg"));
			this.mc.installResourceURL("sound/mob/cat/purr3.ogg", this.getURLfromResource(this.resourceDirectory +"/cat_purr3.ogg"));
			this.mc.installResourceURL("sound/mob/cat/purreow1.ogg", this.getURLfromResource(this.resourceDirectory +"/cat_purreow1.ogg"));
			this.mc.installResourceURL("sound/mob/cat/purreow2.ogg", this.getURLfromResource(this.resourceDirectory +"/cat_purreow2.ogg"));
			
			this.mc.installResourceURL("sound/mob/amazon/hit1.ogg", this.getURLfromResource(this.resourceDirectory +"/amazon_hit1.ogg"));
			this.mc.installResourceURL("sound/mob/amazon/hit2.ogg", this.getURLfromResource(this.resourceDirectory +"/amazon_hit2.ogg"));
			
			this.mc.installResourceURL("sound/mob/triton/hurt1.ogg", this.getURLfromResource(this.resourceDirectory +"/triton_hurt1.ogg"));
			this.mc.installResourceURL("sound/mob/triton/hurt2.ogg", this.getURLfromResource(this.resourceDirectory +"/triton_hurt2.ogg"));
			this.mc.installResourceURL("sound/mob/triton/hurt3.ogg", this.getURLfromResource(this.resourceDirectory +"/triton_hurt3.ogg"));
			this.mc.installResourceURL("sound/mob/triton/idle1.ogg", this.getURLfromResource(this.resourceDirectory +"/triton_idle1.ogg"));
			this.mc.installResourceURL("sound/mob/triton/idle2.ogg", this.getURLfromResource(this.resourceDirectory +"/triton_idle2.ogg"));
			this.mc.installResourceURL("sound/mob/triton/idle3.ogg", this.getURLfromResource(this.resourceDirectory +"/triton_idle3.ogg"));
			this.mc.installResourceURL("sound/mob/triton/idle4.ogg", this.getURLfromResource(this.resourceDirectory +"/triton_idle4.ogg"));
			this.mc.installResourceURL("sound/mob/triton/death.ogg", this.getURLfromResource(this.resourceDirectory +"/triton_death.ogg"));
			
			this.mc.installResourceURL("sound/mob/hauntedcow/hurt1.ogg", this.getURLfromResource(this.resourceDirectory +"/haunted_cow_hurt_01.ogg"));
			this.mc.installResourceURL("sound/mob/hauntedcow/hurt2.ogg", this.getURLfromResource(this.resourceDirectory +"/haunted_cow_hurt_02.ogg"));
			this.mc.installResourceURL("sound/mob/hauntedcow/hurt3.ogg", this.getURLfromResource(this.resourceDirectory +"/haunted_cow_hurt_03.ogg"));
			this.mc.installResourceURL("sound/mob/hauntedcow/idle1.ogg", this.getURLfromResource(this.resourceDirectory +"/haunted_cow_ambient_01.ogg"));
			this.mc.installResourceURL("sound/mob/hauntedcow/idle2.ogg", this.getURLfromResource(this.resourceDirectory +"/haunted_cow_ambient_02.ogg"));
			this.mc.installResourceURL("sound/mob/hauntedcow/idle3.ogg", this.getURLfromResource(this.resourceDirectory +"/haunted_cow_ambient_03.ogg"));
			this.mc.installResourceURL("sound/mob/hauntedcow/idle4.ogg", this.getURLfromResource(this.resourceDirectory +"/haunted_cow_ambient_04.ogg"));
			this.mc.installResourceURL("sound/mob/hauntedcow/death1.ogg", this.getURLfromResource(this.resourceDirectory +"/haunted_cow_death_01.ogg"));
			this.mc.installResourceURL("sound/mob/hauntedcow/death2.ogg", this.getURLfromResource(this.resourceDirectory +"/haunted_cow_death_02.ogg"));
			this.mc.installResourceURL("sound/mob/hauntedcow/death3.ogg", this.getURLfromResource(this.resourceDirectory +"/haunted_cow_death_03.ogg"));
			
			this.mc.installResourceURL("sound/mob/ice/hurt1.ogg", this.getURLfromResource(this.resourceDirectory +"/ice-hurt1.ogg"));
			this.mc.installResourceURL("sound/mob/ice/hurt2.ogg", this.getURLfromResource(this.resourceDirectory +"/ice-hurt2.ogg"));
			this.mc.installResourceURL("sound/mob/ice/hurt3.ogg", this.getURLfromResource(this.resourceDirectory +"/ice-hurt3.ogg"));
			this.mc.installResourceURL("sound/mob/ice/idle1.ogg", this.getURLfromResource(this.resourceDirectory +"/ice_idle1.ogg"));
			this.mc.installResourceURL("sound/mob/ice/idle2.ogg", this.getURLfromResource(this.resourceDirectory +"/ice_idle2.ogg"));
			
			// Twilight
			
			this.mc.installResourceURL("sound/mob/redcap0.ogg", this.getURLfromResource(this.resourceDirectory +"/redcap0.ogg"));
			this.mc.installResourceURL("sound/mob/redcap1.ogg", this.getURLfromResource(this.resourceDirectory +"/redcap1.ogg"));
			this.mc.installResourceURL("sound/mob/redcap2.ogg", this.getURLfromResource(this.resourceDirectory +"/redcap2.ogg"));
			this.mc.installResourceURL("sound/mob/redcap3.ogg", this.getURLfromResource(this.resourceDirectory +"/redcap3.ogg"));
			this.mc.installResourceURL("sound/mob/redcap4.ogg", this.getURLfromResource(this.resourceDirectory +"/redcap4.ogg"));
			this.mc.installResourceURL("sound/mob/redcap5.ogg", this.getURLfromResource(this.resourceDirectory +"/redcap5.ogg"));
			this.mc.installResourceURL("sound/mob/redcaphurt0.ogg", this.getURLfromResource(this.resourceDirectory +"/redcaphurt0.ogg"));
			this.mc.installResourceURL("sound/mob/redcaphurt1.ogg", this.getURLfromResource(this.resourceDirectory +"/redcaphurt1.ogg"));
			this.mc.installResourceURL("sound/mob/redcaphurt2.ogg", this.getURLfromResource(this.resourceDirectory +"/redcaphurt2.ogg"));
			this.mc.installResourceURL("sound/mob/redcaphurt3.ogg", this.getURLfromResource(this.resourceDirectory +"/redcaphurt3.ogg"));
			this.mc.installResourceURL("sound/mob/redcapdie0.ogg", this.getURLfromResource(this.resourceDirectory +"/redcapdie0.ogg"));
			this.mc.installResourceURL("sound/mob/redcapdie1.ogg", this.getURLfromResource(this.resourceDirectory +"/redcapdie1.ogg"));
			this.mc.installResourceURL("sound/mob/redcapdie2.ogg", this.getURLfromResource(this.resourceDirectory +"/redcapdie2.ogg"));
			this.mc.installResourceURL("sound/mob/wraith0.ogg", this.getURLfromResource(this.resourceDirectory +"/wraith0.ogg"));
			this.mc.installResourceURL("sound/mob/wraith1.ogg", this.getURLfromResource(this.resourceDirectory +"/wraith1.ogg"));
			this.mc.installResourceURL("sound/mob/wraith2.ogg", this.getURLfromResource(this.resourceDirectory +"/wraith2.ogg"));
			this.mc.installResourceURL("sound/mob/wraith3.ogg", this.getURLfromResource(this.resourceDirectory +"/wraith3.ogg"));
			
			// Eat
			this.mc.installResourceURL("sound/random/burp.ogg", this.getURLfromResource(this.resourceDirectory +"/burp.ogg"));
			this.mc.installResourceURL("sound/random/drink.ogg", this.getURLfromResource(this.resourceDirectory +"/drink.ogg"));
			this.mc.installResourceURL("sound/random/eat0.ogg", this.getURLfromResource(this.resourceDirectory +"/eat1.ogg"));
			this.mc.installResourceURL("sound/random/eat1.ogg", this.getURLfromResource(this.resourceDirectory +"/eat2.ogg"));
			this.mc.installResourceURL("sound/random/eat2.ogg", this.getURLfromResource(this.resourceDirectory +"/eat3.ogg"));
			
			// Goat
			this.mc.installResourceURL("sound/mocreatures/goateating.ogg", this.getURLfromResource(this.resourceDirectory +"/goateating.ogg"));
			this.mc.installResourceURL("sound/mocreatures/goatsmack.ogg", this.getURLfromResource(this.resourceDirectory +"/goatsmack.ogg"));
			this.mc.installResourceURL("sound/mocreatures/goatdigg.ogg", this.getURLfromResource(this.resourceDirectory +"/goatdigg.ogg"));
			this.mc.installResourceURL("sound/mocreatures/goathurt.ogg", this.getURLfromResource(this.resourceDirectory +"/goathurt.ogg"));
			this.mc.installResourceURL("sound/mocreatures/goatfemale.ogg", this.getURLfromResource(this.resourceDirectory +"/goatfemale.ogg"));
			this.mc.installResourceURL("sound/mocreatures/goatkid.ogg", this.getURLfromResource(this.resourceDirectory +"/goatkid.ogg"));
			this.mc.installResourceURL("sound/mocreatures/goatgrunt.ogg", this.getURLfromResource(this.resourceDirectory +"/goatgrunt.ogg"));			
			this.mc.installResourceURL("sound/mocreatures/goatdying.ogg", this.getURLfromResource(this.resourceDirectory +"/goatdying.ogg"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public File getFileFromResource(String fileName) throws URISyntaxException{

		Class<?> classLoader = this.getClass();
		java.net.URL resource = classLoader.getResource(fileName);
		
		if (resource == null) {
			throw new IllegalArgumentException("file not found! " + fileName);
		} else {

			// failed if files have whitespace or special characters
			//return new File(resource.getFile());

			return new File(resource.toURI());
		}

	}
	
	public java.net.URL getURLfromResource(String fileName) throws URISyntaxException{

		Class<?> classLoader = this.getClass();
		java.net.URL resource = classLoader.getResource(fileName);
		
		if (resource == null) {
			throw new IllegalArgumentException("file not found! " + fileName);
		} else {
			return resource;
		}
	}
}
