package org.pentaho.platform.examples.wadl;

import com.sun.jersey.api.client.WebResource;
import generated.RoleListWrapper;
import generated.User;
import generated.UserListWrapper;

import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by nbaker on 10/11/15.
 */
public class UserResource {
  private WebResource resource;

  public UserResource( WebResource resource ) {
    this.resource = resource;
  }

  public List<String> getUsers() {
    UserListWrapper users =
        resource.path( "userroledao/users" ).accept( MediaType.APPLICATION_XML_TYPE ).get( UserListWrapper.class );
    return users.getUsers();
  }

  public void addUser( String text, String password ) {

    User user = new User();
    user.setUserName( text );
    user.setPassword( password );

    // Create User
    resource.path( "userroledao/createUser" ).type( MediaType.APPLICATION_JSON_TYPE ).put( user );

    // Assign Roles to User
    resource.path( "userroledao/assignRoleToUser" ).queryParam( "userName", text ).queryParam( "roleNames",
        "power user" ).accept( MediaType.APPLICATION_JSON_TYPE ).put();

  }

  public void deleteUser( String name ) {

    resource.path( "userroledao/deleteUsers" ).queryParam( "userNames", name ).type( MediaType.APPLICATION_JSON_TYPE )
        .put();

  }

  public List<String> getRolesForUser( String selectedUser ) {

    RoleListWrapper roles =
        resource.path( "userroledao/userRoles" ).queryParam( "userName", selectedUser ).accept(
            MediaType.APPLICATION_XML_TYPE ).get( RoleListWrapper.class );

    return roles.getRoles();
  }

  public List<String> getAllRoles( ) {

    RoleListWrapper roles =
        resource.path( "userroledao/roles" ).accept(
            MediaType.APPLICATION_XML_TYPE ).get( RoleListWrapper.class );

    return roles.getRoles();
  }

}
