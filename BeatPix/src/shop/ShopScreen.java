package shop;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


import gui.components.*;
import gui.interfaces.FocusController;
import gui.interfaces.Visible;
import gui.userInterfaces.FullFunctionScreen;
import mainGame.components.Song;
public class ShopScreen extends FullFunctionScreen
{
	private Graphic background;	
	private Graphic songBanner;
	private TextLabel currency;
	private CustomRectangle songArea;
	private TextLabel banner;
	private TextLabel credit;
	private Font bannerFont;
	private Font creditFont;
	private ArrayList<Song> songs;
	private Button songButton1;
	private int credits;
	private Font warningFont;
	private boolean noPress;
	
	public ShopScreen(int width, int height) 
	{
		super(width, height);
	}

	@Override
	public void initAllObjects(List<Visible> viewObjects) 
	{
		
		boolean clicked = false;
		try 
		{
		     GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		     ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("resources//Munro.ttf")));
		}
		catch (IOException|FontFormatException e)
		{
			
		}
		credits = 5000;

		
		bannerFont = new Font("resources//slkscr.ttf", Font.ITALIC, 25);
		creditFont = new Font("Verdana", Font.BOLD, 20);
		warningFont = new Font("Verdana", Font.BOLD, 18);
		//graphics
		background = new Graphic(40,0,getWidth(),getHeight(),"resources//shop_bg.png");
		viewObjects.add(background);
		songBanner = new Graphic(170,100,120,120,"resources//SongsBanner.png");
		viewObjects.add(songBanner);
		
		//background for credits, make new component later
		CustomRectangle creditBG = new CustomRectangle(135,60,190,40, Color.white,5);
		viewObjects.add(creditBG);
		
		String s = "Credits: "+credits; // add method "getCredits()" later
		credit = new TextLabel(150,60,200,200, s);
		credit.setCustomTextColor(Color.white);
		credit.setFont(creditFont);
		viewObjects.add(credit);
		
		// where the new songs will be placed
		songArea = new CustomRectangle(110, 150, 260, 350, Color.CYAN,25);
		viewObjects.add(songArea);
		
		//area where "SONGS" will be displayed
		banner = new TextLabel(190,100,200,200, "Songs");
		banner.setCustomTextColor(Color.white);
		banner.setFont(bannerFont);
	//	viewObjects.add(banner);

		//scroll bar, contains the songs
		ScrollablePane scroll = new ScrollablePane(this, 130,175,220,300);		
		
		//when user clicks yes to buy song
		Button yes = new Button(450,205,30,20, "Yes", new Action() 
		{			
			@Override
			public void act() 
			{
				int x = (credits - 1000);
				if (x >= 0)
				{
					credits -= 1000;
					credit.setText("Credits: "+ credits);
					credit.update();
					//add transfer of song later
				}
				else
				{
					new Thread()
					{
						public void run()
						{
							try
							{
								TextLabel warning = new TextLabel(350,60,450,200, "You Do Not Have Enough Credits");
								warning.setFont(warningFont);
								warning.setCustomTextColor(Color.red);
								viewObjects.add(warning);
								Thread.sleep(1500);
								warning.setVisible(false);
							}
							catch (InterruptedException e) 
							{
								e.printStackTrace();
							}
							
						}
					} .start();
				}
			}
		});
		viewObjects.add(yes);
		yes.setVisible(false);
		
		TextArea text = new TextArea(380,175,200,200,"Do You Want to Buy This Song?");				
		viewObjects.add(text);	
		text.setVisible(false);
		Button no = new Button(490,205,20,20, "No", new Action()
		{
			
			

			@Override
			public void act() 
			{
				yes.setVisible(false);				
				text.setVisible(false);
				noPress = true;
			}
		});
		viewObjects.add(no);
		
		no.setVisible(false);
		
		scroll.addObject(songButton1 = new Button(0,0,200,70,"Song | Cost: 1000", new Action()
		{		
			@Override
			public void act() 
			{								
				text.setVisible(true);
				yes.setVisible(true);				
				no.setVisible(true);
			}				
		}));		
		scroll.update();
		viewObjects.add(scroll);
	}	

}
