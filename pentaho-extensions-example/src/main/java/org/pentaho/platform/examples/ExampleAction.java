package org.pentaho.platform.examples;

import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;

/**
 * Created by nbaker on 10/13/15.
 */
public class ExampleAction implements IAuthorizationAction {

  public void ExampleAction( IUnifiedRepository repository, IUserRoleListService userRoleListService){

  }

  @Override public String getName() {
    return "Example Action";
  }

  @Override public String getLocalizedDisplayName( String s ) {
    return "Example Action";
  }
}
