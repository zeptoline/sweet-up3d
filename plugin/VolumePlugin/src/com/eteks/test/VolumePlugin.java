package com.eteks.test;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.plugin.Plugin;
import com.eteks.sweethome3d.plugin.PluginAction;
import com.eteks.sweethome3d.viewcontroller.HomeController;


public class VolumePlugin extends Plugin {

	public PluginAction[] getActions() {
		// TODO Auto-generated method stub
		Home home = getHome();
		HomeController homeCont = getHomeController();
		UserPreferences preference = getUserPreferences();
		
		
		return new PluginAction [] {new VolumeAction(home, homeCont, preference)};
		//return null;
	}

}
