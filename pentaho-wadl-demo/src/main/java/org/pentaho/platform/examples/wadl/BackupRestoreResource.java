package org.pentaho.platform.examples.wadl;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import org.apache.commons.io.IOUtils;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by nbaker on 10/12/15.
 */
public class BackupRestoreResource {
  WebResource resource;

  public BackupRestoreResource( WebResource resource ) {
    this.resource = resource;
  }

  public void backup( File file ) {

    InputStream inputStream =
        resource.path( "/repo/files/backup" ).type( MediaType.APPLICATION_JSON_TYPE ).get( InputStream.class );
    try {
      FileOutputStream fileOutputStream = new FileOutputStream( file );
      IOUtils.copy( inputStream, fileOutputStream );
    } catch ( FileNotFoundException e ) {
      e.printStackTrace();
    } catch ( IOException e ) {
      e.printStackTrace();
    } finally {
      IOUtils.closeQuietly( inputStream );
    }


  }

  public void restore( File selectedFile ) {

    FormDataMultiPart form = new FormDataMultiPart();

    form.field( "overwriteFile", "true" );
    FileInputStream in = null;
    try {
      in = new FileInputStream( selectedFile );
    } catch ( FileNotFoundException e ) {
      e.printStackTrace();
    }
    form.field( "fileUpload", in, MediaType.MULTIPART_FORM_DATA_TYPE );

    form.bodyPart( new FileDataBodyPart( "file", selectedFile, MediaType.APPLICATION_OCTET_STREAM_TYPE ) );

    form.getField( "fileUpload" ).setContentDisposition(
        FormDataContentDisposition.name( "fileUpload" ).fileName( selectedFile.getName() ).build() );


    ClientResponse response = resource.path( "/repo/files/systemRestore" ).
        type( MediaType.MULTIPART_FORM_DATA ).post( ClientResponse.class, form );
  }
}
