package com.eteks.test;

import com.eteks.sweethome3d.plugin.Plugin;
import com.eteks.sweethome3d.plugin.PluginAction;


public class VolumePlugin extends Plugin {

	public PluginAction[] getActions() {
		// TODO Auto-generated method stub
		
		return new PluginAction [] {new VolumeAction()};
		//return null;
	}

}
