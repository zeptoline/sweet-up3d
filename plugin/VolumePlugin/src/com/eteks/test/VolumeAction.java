package com.eteks.test;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import com.eteks.sweethome3d.j3d.Object3DBranchFactory;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.InterruptedRecorderException;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.plugin.PluginAction;
import com.eteks.sweethome3d.swing.HomePane;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.Object3DFactory;


public class VolumeAction extends PluginAction {

	private Home home;
	private HomeController homeCont;
	private UserPreferences preference;

	public void execute() {

		try {
			String path = showSaveDialog(home.getName());
			
			exportToOBJ(path, new Object3DBranchFactory());

		} catch (RecorderException e) {

			e.printStackTrace();
		}
	}

	public VolumeAction(Home home, HomeController homeCont, UserPreferences preference) {
		this.home = home;
		this.homeCont = homeCont;
		this.preference = preference;
		putPropertyValue(Property.NAME, "Export to OBJ");
		putPropertyValue(Property.MENU, "Sweet-up3D");
		// Enables the action by default
		setEnabled(true);
	} 


	/**
	 * Exports to an OBJ file the objects of the 3D view created with the given factory.
	 * Caution !!! This method may be called from an other thread than EDT.  
	 */
	protected void exportToOBJ(String objFile, Object3DFactory object3dFactory) throws RecorderException {
		String header = this.preference != null
		        ? this.preference.getLocalizedString(HomePane.class, 
                        "exportToOBJ.header", new Date()): "";

		// Use a clone of home to ignore selection and for thread safety
		OBJExporter.exportHomeToFile(cloneHomeInEventDispatchThread(home), 
		objFile, header, true, object3dFactory);
	}

	/**
	 * Returns a clone of the given <code>home</code> safely cloned in the EDT.
	 */
	private Home cloneHomeInEventDispatchThread(final Home home) throws RecorderException {
		if (EventQueue.isDispatchThread()) {
			return home.clone();
		} else {
			try {
				final AtomicReference<Home> clonedHome = new AtomicReference<Home>();
				EventQueue.invokeAndWait(new Runnable() {
					public void run() {
						clonedHome.set(home.clone());
					}
				});
				return clonedHome.get();
			} catch (InterruptedException ex) {
				throw new InterruptedRecorderException(ex.getMessage());
			} catch (InvocationTargetException ex) {
				throw new RecorderException("Couldn't clone home", ex.getCause());
			} 
		}
	}
	  
	  /**
	   * Displays a content chooser save dialog to choose the name of a home.
	   */
	  public String showSaveDialog(String homeName) {
	    return homeCont.getContentManager().showSaveDialog(homeCont.getView(),
	        this.preference.getLocalizedString(HomePane.class, "exportToOBJDialog.title"), 
	        ContentManager.ContentType.OBJ, homeName);
	  }

}
