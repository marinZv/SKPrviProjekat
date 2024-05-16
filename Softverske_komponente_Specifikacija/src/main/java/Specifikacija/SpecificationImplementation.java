package Specifikacija;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import configFolder.Config;
import RuNode.Folder;
import RuNode.MyFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public abstract class SpecificationImplementation implements Specification{
    static private String root;
    static private Config config;

    @Override
    /**
     * Sets storage with root and creates config file if it doesnt exist
     *
     * @param path The path to the root of the storage
     */
    public void createStorage(String path) {
        this.setRoot(path);
        this.setConfig(new Config(getRoot()));
    }

    @Override
    /**
     * Configures maximum size of uploaded file
     *
     * @param size Maximum size of a file
     */
    public void configureSize(float size) {
        config.setMaxSize(size);
    }

    @Override
    /**
     * Adds forbiden extensions to the list
     *
     * @param extensions Variadic of forbiden extensions
     */
    public void addExtensions(String...extensions) {
        for (String extension : extensions) {
            if(!config.getExtensions().contains(extension))
                config.addExtension(extension);
        }

    }
    @Override
    /**
     * Sets the list of forbiden extensions
     *
     * @param extensions Variadic of forbiden extensions
     */
    public void setExtensions(String...extensions) {
        List<String> exts = new ArrayList<>();
        for(String e : extensions){
            exts.add(e);
        }
        config.setExtensions(exts);
    }

    @Override
    /**
     * Removes forbiden extensions from the list
     *
     * @param extensions Variadic of forbiden extensions
     */
    public void removeExtensions(String... extensions) {
        for (String extension : extensions) {
            if(config.getExtensions().contains(extension))
                config.removeExtension(extension);
        }
    }

    @Override
    /**
     * Configures maximum muber of files in given folder
     *
     * @param path Path to the folder we are configuring
     * @param maxNumberofFiles number of flies allowed in folder
     */
    public void configureMaxFiles(String path,int maxNumberOfFiles) {
        config.addBan(path,maxNumberOfFiles);
    }

    @Override
    /**
     * Creates config file and saves it in root
     *
     * @param config Configuration parametars
     */
    public MyFile configure(Config config) {
        ObjectMapper objectMapper = new ObjectMapper();
        File jsonFile = new File(root+"\\config.json");
        try {
            try {
                if(!jsonFile.exists())
                    jsonFile.createNewFile();
            } catch (Exception e){
                System.out.println("Please check if the path is right");
            }
            objectMapper.writeValue(jsonFile, config);
            MyFile ret = new MyFile(jsonFile);
            saveAll(getRoot(),ret);
            return ret;
        } catch (StreamWriteException e) {
            System.out.println("ERROR! file in use");
        } catch (DatabindException e) {
            System.out.println("ERROR! Wrong JSON format - coulnt format config file");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    /**
     * Checks if certain action is allowes with the file
     *
     * @param file MyFile file that we are checking
     * @retrun boolean if action is valid
     */
    public boolean check(MyFile file) {
        if(file.getSize()>getConfig().getMaxSize())
            return false;
        for (String extension : config.getExtensions()) {
            if(file.getName().endsWith(extension))
                return false;
        }
        return true;
    }


    /**
     * Creates folders in given directory named 1..n
     *
     * @param path Path to the folder where we are creating subfolders
     * @param name Name of subfolders
     * @param count Number of subfolders created
     */
    public abstract void mkdir(String path, String name, int count);
    /**
     * Save all files in folder
     *
     * @param path Path to the folder where we want to save files
     * @param files Variadic of files we want to save
     */
    public abstract void saveAll(String path,MyFile...files);
    /**
     * Delete file for given path
     *
     * @param path Path to the file we want to delete
     */
    public abstract void delete(String path);
    /**
     * Move file to folder
     *
     * @param oldDestination File location
     * @param newDestination Location of new folder we want to move file to
     */
    public abstract void move(String oldDestination, String newDestination);
    /**
     * Downloads file to downloads folder on pc
     *
     * @param path Path to the file we want to download
     */
    public abstract void download(String path);
    /**
     * Renames file with given location to new name
     *
     * @param oldName File location
     * @param newName New file name
     */
    public abstract void rename(String oldName,String newName);
    public abstract void absolutePath(String relativePath);
    /**
     * Search given folder for file
     *
     * @param path Path to the folder we want to search
     * @param search Full name of a file we want to search for
     * @return List<MyFile> List of files we found
     */
    public abstract List<MyFile> searchByDir(String path,String search);
    /**
     * Search given folder and its subfolders for file
     *
     * @param path Path to the folder we want to search
     * @param search Full name of a file we want to search for
     * @return List<MyFile> List of files we found
     */
    public abstract List<MyFile> deepSearch(String path,String search);
    /**
     * Search given folder and all its subfolders for file
     *
     * @param path Path to the folder we want to search
     * @param search Full name of a file we want to search for
     * @return List<MyFile> List of files we found
     */
    public abstract List<MyFile> recursiveSearch(String path,String search);
    /**
     * Search given folder for file with given extension
     *
     * @param path Path to the folder we want to search
     * @param extension Extension of a file we want to search for
     * @return List<MyFile> List of files we found
     */
    public abstract List<MyFile> searchByExtension(String path,String extension);
    /**
     * Search given folder for file that begins with given string
     *
     * @param path Path to the folder we want to search
     * @param beginig Begining string of a file
     * @return List<MyFile> List of files we found
     */
    public abstract List<MyFile> beginsWith(String path,String beginig);
    /**
     * Search given folder for file
     *
     * @param path Path to the folder we want to search
     * @param contains Part of name of a file we want to search for
     * @return List<MyFile> List of files we found
     */
    public abstract List<MyFile> contains(String path,String contains);
    /**
     * Search given folder for file ending with given string
     *
     * @param path Path to the folder we want to search
     * @param ending Ending string for a file we are searching for
     * @return List<MyFile> List of files we found
     */
    public abstract List<MyFile> endsWith(String path,String ending);
    /**
     * Check if given folder contains all the files with filenames
     *
     * @param path Path to the folder we want to search
     * @param fileNames List of names we want to find in a folder
     * @return boolean Does this folder contain all of the files
     */
    public abstract boolean hasFile(String path,String...fileNames);
    /**
     * Search all folders and subfolders of Root for given file
     *
     * @param fileName Full name of a file we want to search for
     * @return List<MyFile> List of files we found
     */
    public abstract List<MyFile> searchByFileName(String fileName);
    /**
     * Sorts given list by name in a given order
     *
     * @param myFiles List of files we want to sort
     * @param ord Order of sorting
     * @return List<MyFile> Sorted list of files
     */
    public List<MyFile> sortByName(List<MyFile> myFiles,String ord){
        final String order = ord;
        myFiles.sort(new Comparator<MyFile>() {
            @Override
            public int compare(MyFile o1, MyFile o2) {
                if(order.equalsIgnoreCase("desc")) {
                    return -(o1.getName().compareTo(o2.getName()));
                } else {
                    return o1.getName().compareTo(o2.getName());
                }}});
        return myFiles;
    }
    /**
     * Sorts given list by Creation Date in a given order
     *
     * @param myFiles List of files we want to sort
     * @param ord Order of sorting
     * @return List<MyFile> Sorted list of files
     */
    public List<MyFile> sortByCreationDate(List<MyFile> myFiles,String ord){
        final String order = ord;
            myFiles.sort(new Comparator<MyFile>() {
                @Override
                public int compare(MyFile o1, MyFile o2) {
                    if(order.equalsIgnoreCase("desc")) {
                        return o1.getDateCreated()<o2.getDateCreated()?-1:1;
                    } else {
                        return o1.getDateCreated()<o2.getDateCreated()?1:-1;
                    }}});
            return myFiles;
    }
    /**
     * Sorts given list by last modified in a given order
     *
     * @param myFiles List of files we want to sort
     * @param ord Order of sorting
     * @return List<MyFile> Sorted list of files
     */
    public List<MyFile> sortByLastModified(List<MyFile> myFiles,String ord){
        final String order = ord;
        myFiles.sort(new Comparator<MyFile>() {
            @Override
            public int compare(MyFile o1, MyFile o2) {
                if(order.equalsIgnoreCase("desc")) {
                    return o1.getLastModified()<o2.getLastModified()?-1:1;
                } else {
                    return o1.getLastModified()<o2.getLastModified()?1:-1;
                }}});
        return myFiles;
    }
    /**
     * Searches given folder for file with given name created between start and end date
     *
     * @param path Path to the folder we want to search in
     * @param filename Name of a file we want to search for
     * @param start Unix time for dateandtime we want to search from
     * @param end Unix time for dateandtime we want to search to
     * @return List<MyFile> List of files we found
     */
    public abstract List<MyFile> findByDate(String path, String filename, String start, String end);
    /**
     * Creates output list for list of files we send
     *
     * @param files List of files we want formated for output
     * @param path Do we want path to be in formating
     * @param size Do we want size to be in formating
     * @param created Do we want date created to be in formating
     * @param modified Do we want last modified to be in formating
     * @return List<String> formated output
     */
    public List<String> output(List<MyFile> files,boolean path,boolean size,boolean created, boolean modified){
        ArrayList<String> out = new ArrayList<>();
        String line;
        for(MyFile myFile : files){
            line = myFile.getName();
            if(path)
                line = line +" "+myFile.getPath();
            if(size)
                line = line +" "+Long.toString(myFile.getSize())+"B";
            if(created) {
                Date date = new java.util.Date(myFile.getDateCreated());
                SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+1"));
                String formattedDate = sdf.format(date);
                line = line + " " + formattedDate;
            }
            if(modified) {
                Date date = new java.util.Date(myFile.getLastModified());
                SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+1"));
                String formattedDate = sdf.format(date);
                line = line + " " + formattedDate;
            }
            out.add(line);
        }
        return out;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }
}
