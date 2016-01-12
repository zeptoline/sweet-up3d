package com.eteks.test;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.media.j3d.Node;

import com.eteks.sweethome3d.j3d.Ground3D;
import com.eteks.sweethome3d.j3d.OBJWriter;
import com.eteks.sweethome3d.model.Elevatable;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeFurnitureGroup;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.InterruptedRecorderException;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.viewcontroller.Object3DFactory;


  /**
   * Export to OBJ in a separate class to be able to run HomePane without Java 3D classes.
   */
  public class OBJExporter {
    public static void exportHomeToFile(Home home, String objFile, String header, 
                                        boolean exportAllToOBJ, Object3DFactory object3dFactory) throws RecorderException {
      OBJWriter writer = null;
      boolean exportInterrupted = false;
      try {
        writer = new OBJWriter(objFile, header, -1);
  
        List<Selectable> exportedItems = new ArrayList<Selectable>(exportAllToOBJ
            ? home.getSelectableViewableItems()
            : home.getSelectedItems());
        // Search furniture in groups
        List<Selectable> furnitureInGroups = new ArrayList<Selectable>();
        for (Iterator<Selectable> it = exportedItems.iterator(); it.hasNext();) {
          Selectable selectable = (Selectable)it.next();
          if (selectable instanceof HomeFurnitureGroup) {
            it.remove();
            for (HomePieceOfFurniture piece : ((HomeFurnitureGroup)selectable).getAllFurniture()) {
              if (!(piece instanceof HomeFurnitureGroup)) {
                furnitureInGroups.add(piece);
              }
            }
          }
        }
        exportedItems.addAll(furnitureInGroups);

        List<Selectable> emptySelection = Collections.emptyList();
        home.setSelectedItems(emptySelection);
        if (exportAllToOBJ) {
          // Create a not alive new ground to be able to explore its coordinates without setting capabilities
          Rectangle2D homeBounds = getExportedHomeBounds(home);
          if (homeBounds != null) {
            Ground3D groundNode = new Ground3D(home, 
                (float)homeBounds.getX(), (float)homeBounds.getY(), 
                (float)homeBounds.getWidth(), (float)homeBounds.getHeight(), true);
            writer.writeNode(groundNode, "ground");
          }
        }
        
        // Write 3D objects 
        int i = 0;
        for (Selectable item : exportedItems) {
          // Create a not alive new node to be able to explore its coordinates without setting capabilities 
          
          
          
          /* LIGNES MODIFIEES POUR L'EXPORT DAE
           */
          
          
          if(item instanceof Wall)
            ((Wall)item).setThickness(0.08f);
          
          /* LIGNES MODIFIEES POUR L'EXPORT DAE
           */
          
          
          
          
          
          
          Node node = (Node)object3dFactory.createObject3D(home, item, true);
          if (node != null) {
            if (item instanceof HomePieceOfFurniture) {
              writer.writeNode(node);
            } else {
              writer.writeNode(node, item.getClass().getSimpleName().toLowerCase() + "_" + ++i);
            }
          }
        }
      } catch (InterruptedIOException ex) {
        exportInterrupted = true;
        throw new InterruptedRecorderException("Export to " + objFile + " interrupted");
      } catch (IOException ex) {
        throw new RecorderException("Couldn't export to OBJ in " + objFile, ex);
      } finally {
        if (writer != null) {
          try {
            writer.close();
            // Delete the file if exporting is interrupted
            if (exportInterrupted) {
              new File(objFile).delete();
            }
          } catch (IOException ex) {
            throw new RecorderException("Couldn't export to OBJ in " + objFile, ex);
          }
        }
      }
    }
    
    /**
     * Returns <code>home</code> bounds. 
     */
    private static Rectangle2D getExportedHomeBounds(Home home) {
      // Compute bounds that include walls and furniture
      Rectangle2D homeBounds = updateObjectsBounds(null, home.getWalls());
      for (HomePieceOfFurniture piece : getVisibleFurniture(home.getFurniture())) {
        for (float [] point : piece.getPoints()) {
          if (homeBounds == null) {
            homeBounds = new Rectangle2D.Float(point [0], point [1], 0, 0);
          } else {
            homeBounds.add(point [0], point [1]);
          }
        }
      }
      return updateObjectsBounds(homeBounds, home.getRooms());
    }

    /**
     * Returns all the visible pieces in the given <code>furniture</code>.  
     */
    private static List<HomePieceOfFurniture> getVisibleFurniture(List<HomePieceOfFurniture> furniture) {
      List<HomePieceOfFurniture> visibleFurniture = new ArrayList<HomePieceOfFurniture>(furniture.size());
      for (HomePieceOfFurniture piece : furniture) {
        if (piece.isVisible()
            && (piece.getLevel() == null
                || piece.getLevel().isViewable())) {
          if (piece instanceof HomeFurnitureGroup) {
            visibleFurniture.addAll(getVisibleFurniture(((HomeFurnitureGroup)piece).getFurniture()));
          } else {
            visibleFurniture.add(piece);
          }
        }
      }
      return visibleFurniture;
    }

    /**
     * Updates <code>objectBounds</code> to include the bounds of <code>items</code>.
     */
    private static Rectangle2D updateObjectsBounds(Rectangle2D objectBounds,
                                            Collection<? extends Selectable> items) {
      for (Selectable item : items) {
        if (!(item instanceof Elevatable)
            || ((Elevatable)item).getLevel() == null
            || ((Elevatable)item).getLevel().isViewableAndVisible()) {
          for (float [] point : item.getPoints()) {
            if (objectBounds == null) {
              objectBounds = new Rectangle2D.Float(point [0], point [1], 0, 0);
            } else {
              objectBounds.add(point [0], point [1]);
            }
          }
        }
      }
      return objectBounds;
    }
  }
  