package Specifikacija;

import configFolder.Config;
import RuNode.Folder;
import RuNode.MyFile;

import java.util.List;

public interface Specification {
    public void createStorage(String path);
    public void configureSize(float size);
    public void addExtensions(String...extensions);
    public void setExtensions(String...extensions);
    public void removeExtensions(String...extensions);
    public void configureMaxFiles(String path,int maxNumberOfFiles);
    public MyFile configure(Config config);
    public boolean check(MyFile file);
    //osnovne operacije and skladistem
    public void mkdir(String path, String name, int count);
    public void saveAll(String path,MyFile...files);
    public void delete(String path);
    public void move(String oldDestination, String newDestination);
    public void download(String path);
    public void rename(String oldName,String newName);
    public void absolutePath(String relativePath);
    public List<MyFile> searchByDir(String path,String search);
    public List<MyFile> deepSearch(String path,String search);
    public List<MyFile> recursiveSearch(String path,String search);
    public List<MyFile> searchByExtension(String path,String extension);
    public List<MyFile> beginsWith(String path,String beginig);
    public List<MyFile> contains(String path,String contains);
    public List<MyFile> endsWith(String path,String ending);
    public boolean hasFile(String path,String...fileNames);
    public List<MyFile> searchByFileName(String fileName);
    public List<MyFile> sortByName(List<MyFile> myFiles,String order);
    public List<MyFile>sortByCreationDate(List<MyFile> myFiles,String order);
    public List<MyFile> sortByLastModified(List<MyFile> myFiles,String order);
    public List<MyFile> findByDate(String path, String filename, String startUnix, String endUnix);
    public List<String> output(List<MyFile> fileslist,boolean path,boolean size,boolean created, boolean modified);
}
