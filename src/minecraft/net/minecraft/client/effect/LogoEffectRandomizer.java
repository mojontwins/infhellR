package net.minecraft.client.effect;
import net.minecraft.client.gui.GuiMainMenu;

public class LogoEffectRandomizer {
	public double height;
	public double prevHeight;
	public double dropSpeed;
	final GuiMainMenu mainMenu;

	public LogoEffectRandomizer(GuiMainMenu mainMenu, int startX, int startY) {
		this.mainMenu = mainMenu;
		this.height = this.prevHeight = (double)(10 + startY) + GuiMainMenu.getRandom().nextDouble() * 32.0D + (double)startX;
	}

	public void updateLogoEffects() {
		this.prevHeight = this.height;
		if(this.height > 0.0D) {
			this.dropSpeed -= 0.6D;
		}

		this.height += this.dropSpeed;
		this.dropSpeed *= 0.9D;
		if(this.height < 0.0D) {
			this.height = 0.0D;
			this.dropSpeed = 0.0D;
		}

	}
}
