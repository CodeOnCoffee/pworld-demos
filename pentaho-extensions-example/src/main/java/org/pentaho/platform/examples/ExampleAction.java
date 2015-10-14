package org.pentaho.platform.examples;

import org.pentaho.platform.api.engine.IAuthorizationAction;

/**
 * Created by nbaker on 10/13/15.
 */
public class ExampleAction implements IAuthorizationAction {
  @Override public String getName() {
    return "Example Action";
  }

  @Override public String getLocalizedDisplayName( String s ) {
    return "Example Action";
  }
}
