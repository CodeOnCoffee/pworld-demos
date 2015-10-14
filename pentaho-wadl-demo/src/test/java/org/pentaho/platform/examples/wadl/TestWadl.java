package org.pentaho.platform.examples.wadl;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import generated.User;
import generated.UserListWrapper;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.junit.Test;

import javax.ws.rs.core.MediaType;

/**
 * Created by nbaker on 10/11/15.
 */
public class TestWadl {

  @Test
  public void testWadl() {

    final ClientConfig config = new DefaultClientConfig();
    config.getProperties().put( ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true );
    config.getClasses().add( JacksonJsonProvider.class );
    Client client = Client.create( config );
    client.addFilter( new HTTPBasicAuthFilter( "admin", "password" ) );

    WebResource resource = client.resource( "http://localhost:8080/pentaho/api/" );

    UserListWrapper
        users =
        resource.path( "userrolelist/users" ).accept( MediaType.APPLICATION_JSON_TYPE ).get( UserListWrapper.class );
    for ( String user : users.getUsers() ) {
      System.out.println( user );
    }

    User user = new User();
    user.setUserName( "testUser" );
    user.setPassword( "testPassword" );
    resource.path( "userroledao/createUser" ).type( MediaType.APPLICATION_JSON_TYPE ).put( user );

  }
}
