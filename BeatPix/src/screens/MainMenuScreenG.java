package screens;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;

import gui.components.*;
import gui.interfaces.FocusController;
import gui.interfaces.Visible;
import gui.userInterfaces.FullFunctionScreen;
import screens.components.ImageButton;

public class MainMenuScreenG extends FullFunctionScreen{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7197187517418245951L;

	public Timer time;
	
	public Graphic background;
	
	public ArrayList<ImageButton> buttons;
	public static int LEVEL_IDX = 0;
	public static int CHARACTER_IDX = 1;
	public static int UNLOCK_IDX = 2;
	public static int OPTIONS_IDX = 3;
	
	public AnimatedComponent idleCharacter;
	
	public static FullFunctionPane options;
	public boolean optionsOn;
	
	public MainMenuScreenG(int width, int height) {
		super(width, height);
	}

	public void initAllObjects(List<Visible> viewObjects) {
		//--BACKGROUND
/**/	ImageIcon icon = new ImageIcon("resources\\backgrounds\\start.jpg");
/**/	background = new Graphic(0,0,getWidth(),(int) ((getWidth()/icon.getIconWidth())*icon.getIconHeight()+100),"resources\\backgrounds\\start.jpg");
		background.setY(-background.getHeight()+getHeight()*2);
		
		//--BUTTONS
/**/	icon = new ImageIcon("resources\\ui\\buttons\\buttonwithrivet.png");
		buttons = new ArrayList<ImageButton>();
		for(int i=0; i<4; i++) {
/*need to change dimensions*/		buttons.add(new ImageButton(getWidth()+100,(i*100)+50,icon.getIconWidth(),icon.getIconHeight(),"resources\\ui\\buttons\\buttonwithrivet.png"));
		}
		buttons.get(0).setAction(new Action() {
			public void act(){
				buttons.get(0).unhoverAction();
				Test.test.changeDimensions();
/**/			Test.test.setScreen(new StartScreenG(getWidth(),getHeight()));
			}
		});
		buttons.get(3).setAction(new Action() {
			//Options pop up
			public void act() {
				if(true) {
				viewObjects.add(new OptionsPopUp(null, 250, 250, getWidth()/2, getHeight()/2));
				for(ImageButton b: buttons)
					b.setEnabled(false);
				}
			}
		});
		
		//--IDLE CHARACTER ANIMATION
/**/		idleCharacter = new AnimatedComponent(100, 200 + getHeight(), 300, 300);
/**/		idleCharacter.addSequence("resources/idle.png", 500, 0, 0, 39, 33, 2);
		Thread run = new Thread(idleCharacter);
		run.start();
		
		scrollDown();
		//slideInButtons();
		
		viewObjects.add(background);
		for(ImageButton b: buttons) {
			viewObjects.add(b);
		}
		viewObjects.add(idleCharacter);
	}

//--EVENTS--//
	public void scrollDown() {
		time = new Timer();
		time.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				if(background.getY() > -background.getHeight()+getHeight() && idleCharacter.getY() > 200) {
					background.setY(background.getY() - 1);
					idleCharacter.setY(idleCharacter.getY() - 1);
				}else {
					scrollDownEnd();
				}
			}
		}, 0, 10);
	}
	public void scrollDownEnd() {
		time.cancel();
		slideInButtons();
		background.setY(-background.getHeight()+getHeight());
		idleCharacter.setY(200);
	}
	public void slideInButtons() {
		time = new Timer();
		time.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				if(buttons.get(0).getX() > getWidth()-buttons.get(0).getWidth()-100) {
					for(ImageButton b: buttons) {
						b.setX(b.getX()-5);
					}
				}else {
					slideInButtonsEnd();
				}
			}
		}, 0, 10);
	}
	public void slideInButtonsEnd() {
		time.cancel();
		for(ImageButton b: buttons) {
			b.setEnabled(true);
		}
	}
	
}
