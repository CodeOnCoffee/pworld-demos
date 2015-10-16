package org.pentaho.platform.examples;

import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 * Created by nbaker on 10/7/15.
 */
public class Activator {

  private ISystemSettings defaultSettings;

  public void loaded() {

    // Get Current Settings Service
    defaultSettings = PentahoSystem.getSystemSettings();

    // Create and register custom Settings Service
    CustomSystemSettings customSystemSettings = new CustomSystemSettings( defaultSettings );
    PentahoSystem.setSystemSettingsService( customSystemSettings );

  }

  public void unLoaded() {

    // Restore original Settings Service
    PentahoSystem.setSystemSettingsService( defaultSettings );

  }

}
