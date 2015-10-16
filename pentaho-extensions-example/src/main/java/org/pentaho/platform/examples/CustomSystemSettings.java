package org.pentaho.platform.examples;

import org.dom4j.Document;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;

import java.util.List;
import java.util.Properties;

/**
 * Created by nbaker on 10/7/15.
 */
public class CustomSystemSettings implements ISystemSettings {
  private ISystemSettings defaultSettings;

  public CustomSystemSettings( ISystemSettings defaultSettings ) {
    this.defaultSettings = defaultSettings;
  }


  @Override public String getSystemSetting( String s, String s1 ) {

    if ( "default-theme".equals( s ) ) {
      IPentahoSession session = PentahoSessionHolder.getSession();
      String username = session.getName();

      if ( username.equals( "suzy" ) ) {
        String activeTheme = "onyx";
//        String activeTheme = "onyx".equals( session.getAttribute( "theme" ) ) ? "crystal" : "onyx";
        session.setAttribute( "theme", activeTheme );
        return activeTheme;
      }

    }

    return defaultSettings.getSystemSetting( s, s1 );
  }

  // The Rest delegate directly (pass-through)














  

  @Override public String getSystemCfgSourceName() {
    return defaultSettings.getSystemCfgSourceName();
  }

  @Override public String getSystemSetting( String s, String s1, String s2 ) {
    return defaultSettings.getSystemSetting( s, s1, s2 );
  }


  @Override public List getSystemSettings( String s, String s1 ) {
    return defaultSettings.getSystemSettings( s, s1 );
  }

  @Override public List getSystemSettings( String s ) {
    return defaultSettings.getSystemSettings( s );
  }

  @Override public void resetSettingsCache() {
    defaultSettings.resetSettingsCache();
  }

  @Override public Document getSystemSettingsDocument( String s ) {
    return defaultSettings.getSystemSettingsDocument( s );
  }

  @Override public Properties getSystemSettingsProperties( String s ) {
    return defaultSettings.getSystemSettingsProperties( s );
  }
}
