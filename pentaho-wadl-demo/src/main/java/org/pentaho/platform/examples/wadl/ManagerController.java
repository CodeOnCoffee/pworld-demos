package org.pentaho.platform.examples.wadl;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.multipart.impl.MultiPartWriter;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.components.XulDialogheader;
import org.pentaho.ui.xul.components.XulFileDialog;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.swing.SwingBindingFactory;
import org.pentaho.ui.xul.util.AbstractModelList;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by nbaker on 10/12/15.
 */
public class ManagerController extends AbstractXulEventHandler {

  private SwingBindingFactory bindingFactory;
  private String url = "http://localhost:8080/pentaho/api";
  private Client client;
  private WebResource resource;
  private UserResource userResource;
  private BackupRestoreResource backupRestoreResource;
  private AbstractModelList userList;
  private AbstractModelList<RoleBinding> boundRoles = new AbstractModelList<>(  );
  private String selectedUser;
  private String newUserName, newUserPassword;
  private XulDialog newUserDialog;
  private XulDialog waitDialog;


  public String getNewUserName() {
    return newUserName;
  }

  public void setNewUserName( String newUserName ) {
    String prevVal = this.newUserName;
    this.newUserName = newUserName;
    this.firePropertyChange( "newUserName", prevVal, newUserName );
  }

  public String getNewUserPassword() {
    return newUserPassword;
  }

  public void setNewUserPassword( String newUserPassword ) {
    String prevVal = this.newUserPassword;
    this.newUserPassword = newUserPassword;
    this.firePropertyChange( "newUserPassword", prevVal, newUserPassword );
  }

  public String getSelectedUser() {
    return selectedUser;
  }

  public void setSelectedUser( String selectedUser ) {
    String prevVal = this.selectedUser;
    this.selectedUser = selectedUser;
    this.firePropertyChange( "selectedUser", prevVal, selectedUser );
    boundRoles.clear();
    if(selectedUser == null ){
      return;
    }
    java.util.List<String> rolesForUser = userResource.getRolesForUser( selectedUser );
    java.util.List<String> allRoles = userResource.getAllRoles( );

    Map< String, RoleBinding > mappings = new HashMap<>();
    for( String role : allRoles ){
      mappings.put( role, new RoleBinding( role, false ) );
    }

    for ( String selectedRole : rolesForUser ) {
      mappings.get( selectedRole ).setSelected( true );
    }

    boundRoles.addAll( mappings.values() );

  }

  @Override public String getName() {
    return "handler";
  }

  public void init() {
    userList = new AbstractModelList();
    bindingFactory = new SwingBindingFactory();
    newUserDialog = (XulDialog) document.getElementById( "newUserDialog" );
    waitDialog = (XulDialog) document.getElementById( "waitDialog" );


    SwingBindingFactory bindingFactory = this.bindingFactory;
    bindingFactory.setDocument( getXulDomContainer().getDocumentRoot() );

    Binding binding = bindingFactory.createBinding( this, "url", "address", "value" );
    bindingFactory.createBinding( userList, "children", "users", "elements" );
    bindingFactory.createBinding( "users", "selectedItem", this, "selectedUser" );
    bindingFactory.createBinding( this, "newUserName", "newUserName", "value" );
    bindingFactory.createBinding( this, "newUserPassword", "newUserPassword", "value" );


    bindingFactory.setBindingType( Binding.Type.ONE_WAY );
    bindingFactory.createBinding( boundRoles, "children", "roleTree", "elements" );
    bindingFactory
        .createBinding( this, "selectedUser", "deleteBtn", "disabled", new BindingConvertor<String, Boolean>() {

          @Override public Boolean sourceToTarget( String s ) {
            return s == null;
          }

          @Override public String targetToSource( Boolean aBoolean ) {
            return null;
          }
        } );
    try {
      binding.fireSourceChanged();
    } catch ( XulException e ) {
      e.printStackTrace();
    } catch ( InvocationTargetException e ) {
      e.printStackTrace();
    }


  }

  public void connect() {

    this.url = url;
    final ClientConfig config = new DefaultClientConfig();
    config.getProperties().put( ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true );
    config.getClasses().add( MultiPartWriter.class );
    config.getClasses().add( JacksonJsonProvider.class );
    client = Client.create( config );
    client.addFilter( new HTTPBasicAuthFilter( "admin", "password" ) );
    resource = client.resource( url );
    userResource = new UserResource( resource );
    backupRestoreResource = new BackupRestoreResource( resource );
    java.util.List<String> users = userResource.getUsers();
    userList.clear();
    userList.addAll( users );

  }

  public void deleteUser() {
    int confirmed = JOptionPane.showConfirmDialog( (Component) document.getRootElement().getManagedObject(),
        "Are you sure you want to delete " + selectedUser, "Delete User", JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.WARNING_MESSAGE );
    if ( confirmed == JOptionPane.YES_OPTION ) {
      userResource.deleteUser( selectedUser );
      connect();
    }
  }

  public String getUrl() {
    return url;
  }

  public void setUrl( String url ) {
    this.url = url;
  }

  public void createNewUser() {
    userResource.addUser( newUserName, newUserPassword );
    closeNewUserDialog();
    connect();
  }

  public void closeNewUserDialog() {
    newUserDialog.setVisible( false );
  }

  public void showNewUserDialog() {
    setNewUserName( null );
    setNewUserPassword( null );
    newUserDialog.setVisible( true );
  }

  public void download() {
    try {
      XulDialogheader dialogheader = (XulDialogheader) document.getElementById( "waitDialogHeader" );
      dialogheader.setDescription( "Backup in progress..." );

      XulFileDialog fileDialog = (XulFileDialog) document.createElement( "FileDialog" );
      XulFileDialog.RETURN_CODE return_code = fileDialog.showSaveDialog();
      if ( return_code == XulFileDialog.RETURN_CODE.OK ) {
        final File selectedFile = (File) fileDialog.getFile();


        Thread t = new Thread( new Runnable() {
          @Override public void run() {

            backupRestoreResource.backup( selectedFile );
            waitDialog.setVisible( false );
          }
        } );
        t.start();

        waitDialog.setVisible( true );
      }
    } catch ( XulException e ) {
      e.printStackTrace();
    }
  }

  public void restore() {
    XulDialogheader dialogheader = (XulDialogheader) document.getElementById( "waitDialogHeader" );
    dialogheader.setDescription( "Restore in progress..." );

    XulFileDialog fileDialog = null;
    try {
      fileDialog = (XulFileDialog) document.createElement( "FileDialog" );
    } catch ( XulException e ) {
      e.printStackTrace();
    }
    XulFileDialog.RETURN_CODE return_code = fileDialog.showOpenDialog();
    if ( return_code == XulFileDialog.RETURN_CODE.OK ) {
      final File selectedFile = (File) fileDialog.getFile();

      Thread t = new Thread( new Runnable() {
        @Override public void run() {
          backupRestoreResource.restore( selectedFile );
          waitDialog.setVisible( false );
        }
      } );
      t.start();
      waitDialog.setVisible( true );
    }
  }

}
