package org.pentaho.platform.examples.wadl;

import org.pentaho.ui.xul.XulEventSourceAdapter;

/**
 * Created by nbaker on 10/13/15.
 */
public class RoleBinding extends XulEventSourceAdapter {
  private String role;
  private boolean selected;

  public RoleBinding( String role, boolean selected ) {
    this.role = role;
    this.selected = selected;
  }

  public String getRole() {
    return role;
  }

  public void setRole( String role ) {
    this.role = role;
  }

  public boolean isSelected() {
    return selected;
  }

  public void setSelected( boolean selected ) {
    this.selected = selected;
  }
}
