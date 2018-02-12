package shop;

import java.util.ArrayList;
import java.util.List;

import gui.components.Action;
import gui.components.AnimatedComponent;
import gui.components.Button;
import gui.components.Component;
import gui.components.FullFunctionPane;
import gui.interfaces.FocusController;
import gui.interfaces.Visible;
import gui.userInterfaces.FullFunctionScreen;

public class ShopScreenKevin2 extends FullFunctionScreen {

	private ArrayList<Button> buttonList;
	private int index;
	private ArrayList<Integer> indexList;
	private String[] imageName;
	private ArrayList<ImageButton> images;
	public ShopScreenKevin2(int width, int height) {
		super(width, height);
	}

	@Override
	public void initAllObjects(List<Visible> viewObjects) {
		// TODO Auto-generated method stub
		buttonList = new ArrayList<Button>();
		indexList = new ArrayList<Integer>();
		images = new ArrayList<ImageButton>();
		imageName = new String[] {"resources/redGuy.png", "resources/greenGuy.png", "resources/whiteGuy.png"};
		for(int i = 0; i < imageName.length; i ++) {
			images.add(new ImageButton(100, (100*i)+50, 200, 300, imageName[i], null, null));
		}
		createIntList(5);
		for(int i=0; i < 3; i++){ 
			//got the index number
			final int x = i;
			buttonList.add(new Button(5,(50*i)+50,100,25,"Button "+i, new Action() {
				int j = x;
				@Override
				public void act() {
				System.out.println(j);
				System.out.println(indexList.indexOf(j));
				index = indexList.indexOf(j);
				System.out.println("The index number is "+index);			
				buttonList.get(j).setVisible(false);
				indexList.remove(index);
				images.get(j).setVisible(true);
				

				}
			}));
		}
		
		for(int a = 0; a < buttonList.size(); a++) {
			images.get(a).setVisible(false);
			viewObjects.add(buttonList.get(a));
			viewObjects.add(images.get(a));
			
		}

		
	}
	public void createIntList(int a) {
		for(int i = 0; i < a; i++) {
			indexList.add(i);
		}
	}
}

