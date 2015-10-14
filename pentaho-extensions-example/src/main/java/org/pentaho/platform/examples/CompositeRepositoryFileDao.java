package org.pentaho.platform.examples;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.pentaho.platform.api.locale.IPentahoLocale;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Created by nbaker on 10/8/15.
 */
public class CompositeRepositoryFileDao implements IRepositoryFileDao {
  private IRepositoryFileDao delegate;

  public CompositeRepositoryFileDao( IRepositoryFileDao delegate ) {
    this.delegate = delegate;
  }

  @Override public RepositoryFile getFileByAbsolutePath( String absPath ) {
    return delegate.getFileByAbsolutePath( absPath );
  }

  @Override public RepositoryFile getFile( String relPath, boolean loadLocaleMaps ) {
    if ( relPath.startsWith( "/pworld" ) ) {
      return getFile( relPath );
    }
    return delegate.getFile( relPath, loadLocaleMaps );
  }

  private String findPathByUUID( UUID id ) {
    for ( Map.Entry<String, UUID> stringUUIDEntry : uuids.asMap().entrySet() ) {
      if ( stringUUIDEntry.getValue().equals( id ) ) {
        return stringUUIDEntry.getKey();
      }
    }
    return null;
  }


  @Override public RepositoryFile getFileById( Serializable fileId ) {
    if ( fileId.toString().startsWith( "file:" ) ) {
      UUID uuid = UUID.fromString( fileId.toString().substring( 5 ) );
      String path = findPathByUUID( uuid );
      return getFile( path );
    }
    return delegate.getFileById( fileId );
  }

  @Override public RepositoryFile getFileById( Serializable fileId,
                                               boolean loadLocaleMaps ) {
    if ( fileId.toString().startsWith( "file:" ) ) {
      UUID uuid = UUID.fromString( fileId.toString().substring( 5 ) );
      String path = findPathByUUID( uuid );
      return getFile( path );
    }
    return delegate.getFileById( fileId, loadLocaleMaps );
  }


  @Override public <T extends IRepositoryFileData> T getData(
      Serializable fileId, Serializable versionId, Class<T> dataClass ) {
    if ( fileId.toString().startsWith( "file:" ) ) {
      UUID uuid = UUID.fromString( fileId.toString().substring( 5 ) );
      String path = findPathByUUID( uuid );
      File file = new File( "/Users/nbaker/Desktop/" + path );
      try {
        SimpleRepositoryFileData data = new SimpleRepositoryFileData( new FileInputStream( file ), "UTF-8", "text/xml"
            + "" );
        return (T) data;
      } catch ( FileNotFoundException e ) {
        e.printStackTrace();
      }
    }
    return delegate.getData( fileId, versionId, dataClass );
  }

  LoadingCache<String, UUID> uuids = CacheBuilder.newBuilder().build(
      new CacheLoader<String, UUID>() {
        public UUID load( String key ) throws Exception {
          return UUID.randomUUID();
        }
      } );

  @Override public RepositoryFile getFile( String relPath ) {

    if ( relPath.startsWith( "/pworld" ) ) {
      String filePath = relPath.substring( relPath.indexOf( "/pworld" ) );
      File file = new File( filePath );

      String path = file.getPath().substring( file.getPath().indexOf( "pworld" ) - 1 );
      return new RepositoryFile.Builder( file.getName() ).folder( true ).id( "file:" + getUuid( path ) ).path(
          path ).lastModificationDate( new Date( file.lastModified() ) ).build();

    } else {
      return delegate.getFile( relPath );
    }

  }

  @Override @Deprecated public RepositoryFileTree getTree( String relPath, int depth,
                                                           String filter, boolean showHidden ) {
    return delegate.getTree( relPath, depth, filter, showHidden );
  }

  @Override public RepositoryFileTree getTree(
      RepositoryRequest repositoryRequest ) {
    RepositoryFileTree tree = delegate.getTree( repositoryRequest );

    RepositoryFileTree folder = getFolderAsTree( new File( "/Users/nbaker/Desktop/pworld" ) );
    tree.getChildren().add( folder );
    return tree;
  }

  @Override public List<RepositoryFile> getChildren(
      RepositoryRequest repositoryRequest ) {
    String path = repositoryRequest.getPath();
    List<RepositoryFile> children = new ArrayList<>();
    if ( path.startsWith( "/pworld" ) ) {
      String filePath = path.substring( path.indexOf( "/pworld" ) );
      File file = new File( "/Users/nbaker/Desktop" + filePath );
      if ( file.isFile() ) {
        return children;
      }
      for ( File f : file.listFiles() ) {
        String relPath = f.getPath().substring( file.getPath().indexOf( "pworld" ) - 1 );
        RepositoryFile repositoryFile = null;

        repositoryFile = new RepositoryFile.Builder( f.getName() ).id( "file:" + getUuid( relPath ) )
            .path( relPath ).build();

        children.add( repositoryFile );
      }
      return children;
    }
    return delegate.getChildren( repositoryRequest );
  }


  private RepositoryFileTree getFolderAsTree( File file ) {
    List<RepositoryFileTree> children = new ArrayList<>();
    for ( File f : file.listFiles() ) {
      if ( f.isDirectory() ) {
        RepositoryFileTree dir = getFolderAsTree( f );
        children.add( dir );
      } else {

        String path = file.getPath().substring( f.getPath().indexOf( "pworld" ) - 1 );

        RepositoryFile repositoryFile = new RepositoryFile.Builder( f.getName() ).id( "file:" + getUuid( path ) )
            .path( path ).build();

        RepositoryFileTree newFolder = new RepositoryFileTree( repositoryFile,
            Collections.<RepositoryFileTree>emptyList() );
        children.add( newFolder );
      }
    }

    String path = file.getPath().substring( file.getPath().indexOf( "pworld" ) - 1 );
    RepositoryFile repositoryFile =
        new RepositoryFile.Builder( file.getName() ).folder( true ).id( "file:" + getUuid( path ) ).path(
            path ).build();

    RepositoryFileTree newFolder = new RepositoryFileTree( repositoryFile, children );
    return newFolder;
  }

  private UUID getUuid( String path ) {
    try {
      return uuids.get( path );
    } catch ( ExecutionException e ) {
      e.printStackTrace();
    }
    return UUID.randomUUID();
  }

  @Override public Map<String, Serializable> getFileMetadata( Serializable fileId ) {
    if ( fileId.toString().startsWith( "file:" ) ) {
      return Collections.emptyMap();
    }
    return delegate.getFileMetadata( fileId );
  }

  @Override public RepositoryFile getFile( String relPath,
                                           IPentahoLocale locale ) {
    return delegate.getFile( relPath, locale );
  }

  @Override public RepositoryFile getFileById( Serializable fileId,
                                               IPentahoLocale locale ) {
    return delegate.getFileById( fileId, locale );
  }

  @Override public RepositoryFile getFile( String relPath, boolean loadLocaleMaps,
                                           IPentahoLocale locale ) {
    return delegate.getFile( relPath, loadLocaleMaps, locale );
  }

  @Override public RepositoryFile getFileById( Serializable fileId,
                                               boolean loadLocaleMaps,
                                               IPentahoLocale locale ) {
    return delegate.getFileById( fileId, loadLocaleMaps, locale );
  }

  @Override public RepositoryFile createFile( Serializable parentFolderId,
                                              RepositoryFile file,
                                              IRepositoryFileData data,
                                              RepositoryFileAcl acl,
                                              String versionMessage ) {
    return delegate.createFile( parentFolderId, file, data, acl, versionMessage );
  }

  @Override public RepositoryFile createFolder( Serializable parentFolderId,
                                                RepositoryFile file,
                                                RepositoryFileAcl acl,
                                                String versionMessage ) {
    return delegate.createFolder( parentFolderId, file, acl, versionMessage );
  }

  @Override public RepositoryFile updateFolder(
      RepositoryFile file, String versionMessage ) {
    return delegate.updateFolder( file, versionMessage );
  }

  @Override @Deprecated public List<RepositoryFile> getChildren(
      Serializable folderId, String filter, Boolean showHiddenFiles ) {
    return delegate.getChildren( folderId, filter, showHiddenFiles );
  }

  @Override public RepositoryFile updateFile(
      RepositoryFile file,
      IRepositoryFileData data, String versionMessage ) {
    return delegate.updateFile( file, data, versionMessage );
  }

  @Override public void deleteFile( Serializable fileId, String versionMessage ) {
    delegate.deleteFile( fileId, versionMessage );
  }

  @Override public void deleteFileAtVersion( Serializable fileId, Serializable versionId ) {
    delegate.deleteFileAtVersion( fileId, versionId );
  }

  @Override public void undeleteFile( Serializable fileId, String versionMessage ) {
    delegate.undeleteFile( fileId, versionMessage );
  }

  @Override public void permanentlyDeleteFile( Serializable fileId, String versionMessage ) {
    delegate.permanentlyDeleteFile( fileId, versionMessage );
  }

  @Override public List<RepositoryFile> getDeletedFiles(
      String origParentFolderPath, String filter ) {
    return delegate.getDeletedFiles( origParentFolderPath, filter );
  }

  @Override public List<RepositoryFile> getDeletedFiles() {
    return delegate.getDeletedFiles();
  }

  @Override public boolean canUnlockFile( Serializable fileId ) {
    return delegate.canUnlockFile( fileId );
  }

  @Override public void lockFile( Serializable fileId, String message ) {
    delegate.lockFile( fileId, message );
  }

  @Override public void unlockFile( Serializable fileId ) {
    delegate.unlockFile( fileId );
  }

  @Override public List<VersionSummary> getVersionSummaries(
      Serializable fileId ) {
    return delegate.getVersionSummaries( fileId );
  }

  @Override public VersionSummary getVersionSummary( Serializable fileId,
                                                     Serializable versionId ) {
    return delegate.getVersionSummary( fileId, versionId );
  }

  @Override public RepositoryFile getFile( Serializable fileId,
                                           Serializable versionId ) {
    return delegate.getFile( fileId, versionId );
  }

  @Override public void moveFile( Serializable fileId, String destRelPath, String versionMessage ) {
    delegate.moveFile( fileId, destRelPath, versionMessage );
  }

  @Override public void copyFile( Serializable fileId, String destAbsPath, String versionMessage ) {
    delegate.copyFile( fileId, destAbsPath, versionMessage );
  }

  @Override public void restoreFileAtVersion( Serializable fileId, Serializable versionId,
                                              String versionMessage ) {
    delegate.restoreFileAtVersion( fileId, versionId, versionMessage );
  }

  @Override public List<RepositoryFile> getReferrers(
      Serializable fileId ) {
    return delegate.getReferrers( fileId );
  }

  @Override public void setFileMetadata( Serializable fileId,
                                         Map<String, Serializable> metadataMap ) {
    delegate.setFileMetadata( fileId, metadataMap );
  }

  @Override public List<Character> getReservedChars() {
    return delegate.getReservedChars();
  }

  @Override public List<Locale> getAvailableLocalesForFileById( Serializable fileId ) {
    return delegate.getAvailableLocalesForFileById( fileId );
  }

  @Override public List<Locale> getAvailableLocalesForFileByPath( String relPath ) {
    return delegate.getAvailableLocalesForFileByPath( relPath );
  }

  @Override public List<Locale> getAvailableLocalesForFile(
      RepositoryFile repositoryFile ) {
    return delegate.getAvailableLocalesForFile( repositoryFile );
  }

  @Override public Properties getLocalePropertiesForFileById( Serializable fileId, String locale ) {
    return delegate.getLocalePropertiesForFileById( fileId, locale );
  }

  @Override public Properties getLocalePropertiesForFileByPath( String relPath, String locale ) {
    return delegate.getLocalePropertiesForFileByPath( relPath, locale );
  }

  @Override public Properties getLocalePropertiesForFile(
      RepositoryFile repositoryFile, String locale ) {
    return delegate.getLocalePropertiesForFile( repositoryFile, locale );
  }

  @Override public void setLocalePropertiesForFileById( Serializable fileId, String locale,
                                                        Properties properties ) {
    delegate.setLocalePropertiesForFileById( fileId, locale, properties );
  }

  @Override public void setLocalePropertiesForFileByPath( String relPath, String locale, Properties properties ) {
    delegate.setLocalePropertiesForFileByPath( relPath, locale, properties );
  }

  @Override public void setLocalePropertiesForFile( RepositoryFile repositoryFile,
                                                    String locale, Properties properties ) {
    delegate.setLocalePropertiesForFile( repositoryFile, locale, properties );
  }

  @Override public void deleteLocalePropertiesForFile( RepositoryFile repositoryFile,
                                                       String locale ) {
    delegate.deleteLocalePropertiesForFile( repositoryFile, locale );
  }
}
