package org.pentaho.platform.examples.wadl;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.swing.SwingXulLoader;
import org.pentaho.ui.xul.swing.SwingXulRunner;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by nbaker on 10/11/15.
 */
public class Manager {

  public static void main( String[] args ) {


    try {
      SwingXulLoader swingXulLoader = new SwingXulLoader();
      XulDomContainer xulDomContainer = swingXulLoader.loadXul( "xul/manager.xul" );
      SwingXulRunner runner = new SwingXulRunner();
      runner.addContainer( xulDomContainer );
      runner.initialize();
      runner.start();

    } catch ( XulException e ) {
      e.printStackTrace();
    }

  }
}
