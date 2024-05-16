package main;

import configFolder.Config;
import RuNode.Folder;
import RuNode.MyFile;
import Specifikacija.SpecificationImplementation;
import Specifikacija.StorageManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    static Config config;
    static List<MyFile> clipboard;
    static SpecificationImplementation storage;
    static String storageString;

    public static void main(String[] args) throws Exception{
        if(args.length != 2)
            throw new Exception("Invalid number of arguments expected 2 got " +args.length);

        System.out.println("M to storage");

        Scanner input = new Scanner(System.in);
        storage = newStorage(args);

        System.out.println("Type help to see the list of commands");
        doTheThing(storage,input);
    }

    private static SpecificationImplementation newStorage(String[] args) throws Exception{
        String type = args[0];
        String path = args[1];
        String classforname;
        clipboard = new ArrayList<>();

        if(type.equalsIgnoreCase("G")){
            classforname = "drive.Implementation";
            storageString = "GOOGLE";
        } else if (type.equalsIgnoreCase("L")){
            classforname = "local.Implementation";
            storageString = "LOCAL";
        } else {
            classforname = "moron";
        }

        if(classforname.equals("moron")){
            throw new Exception("Moron invalid storage");
        }
        Class.forName(classforname);
        return StorageManager.getStorage(path);
    }

    private static String getCommand(String in){
        String[] s = in.split(" ");
        if (s.length == 0){
            System.out.println("WTF");
            return null;
        }
        return s[0].trim().toLowerCase();
    }

    private static String[] getArguments(String in){
        String[] s = in.split(" ");
        List<String> args = new ArrayList<>(Arrays.asList(s).subList(1, s.length));
        String[] arguments = new String[args.size()];
        for (int i = 0 ; i< arguments.length; i++) {
            if(args.get(i).equals(".")){
                arguments[i] = "";
            } else {
                arguments[i] = args.get(i);
            }
        }
        return arguments;
    }

    private static MyFile[] toArray(List<MyFile> list){
        MyFile[] arguments = new MyFile[list.size()];
        for (int i = 0 ; i< arguments.length; i++) {
            arguments[i] = list.get(i);
        }
        return arguments;
    }

    private static void doTheThing(SpecificationImplementation storage , Scanner in){
        String ln = in.nextLine();
        ln = String.join("\\\\",ln.split("/"));
        String command = getCommand(ln);
        String[] arguments;

        while (true) {
            if (command == null) {
                ln = in.nextLine();
                command = getCommand(ln);
            }

            switch (command) {
                case "help" : {
                    if (storageString.equalsIgnoreCase("LOCAL")) {
                        System.out.println("help - shows list of commands");
                        System.out.println("exit - exits the program");
                        System.out.println("configureSize long(size) - configure storage size");
                        System.out.println("addExtensions String...(extensions) - add banned extensions (separated by comma)");
                        System.out.println("removeExtensions String...(extensions) - remove banned extensions (separated by comma)");
                        System.out.println("setExtensions String...(extensions) - set banned extensions (separated by comma)");
                        System.out.println("configureMaxFiles String(path) int(maxFiles) - configures maximum number of files in path");
                        System.out.println("configure - updates config file");
                        System.out.println("mkdir String(path) String(name) int(NumberofFolders) - makes folder in path with name numberofFolders times");
                        System.out.println("saveAll String(path) - saves all files from clipboard to current location");
                        System.out.println("delete String(path) - deletes file at given path");
                        System.out.println("move String(oldDestination) String(newDestionation) - moves file from old destination to new destination");
                        System.out.println("download String(path) downloads file from path to downloads folder");
                        System.out.println("rename String(path) Strgin(newName) - renames file for path with newName");
                        System.out.println("searchByDir String(path) String(filename) - searches all files in path directorium with full filename");
                        System.out.println("deepSearch String(path) String(filename) - searches all files in path directorium and first childern with full filename");
                        System.out.println("recursiveSeach String(path) String(filename) - searches all files in path directorium and all directoriums inside with full filename");
                        System.out.println("searchByExtension String(path) String(extension) - searches all files in path directorium with given extension");
                        System.out.println("beginsWith String(path) String(beginingString) - searches all files in path directorium that begin with BeginingString");
                        System.out.println("contains String(path) String(containing) - searches all files in path directorium with containing in its name");
                        System.out.println("endsWith String(path) String(ending) - searches all files in path directorium with ending in endingString");
                        System.out.println("searchByFileName String(filename) - searches all files for file with full filename");
                        System.out.println("findByDate String(path) String(fileName) long(startingDate) long(endingDate) - finds files with filename in directirium created between start and end date USE UNIX TIME!!!!");
                        System.out.println("hasFile String(path) - checks if given path has all folders from clipboard");
                        System.out.println("sortByName (desc/asc) - sorts clipboard by Name");
                        System.out.println("sortByCreationDate (desc/asc) - sorts clipboard by CreationDate");
                        System.out.println("sortByLastModified (desc/asc) - sorts clipboard by LastModified");
                        System.out.println("output bool(path) bool(size) bool(created) bool(modified) - outputs files from clipboard with fields enabeld (you can use t/f , y/n , true/false");
                    }else{
                        System.out.println("help - shows list of commands");
                        System.out.println("exit - exits the program");
                        System.out.println("configureSize long(size) - configure storage size");
                        System.out.println("addExtensions String...(extensions) - add banned extensions (separated by comma)");
                        System.out.println("removeExtensions String...(extensions) - remove banned extensions (separated by comma)");
                        System.out.println("setExtensions String...(extensions) - set banned extensions (separated by comma)");
                        System.out.println("configureMaxFiles String(folderID) int(maxFiles) - configures maximum number of files in folder");
                        System.out.println("configure - updates config file");
                        System.out.println("mkdir String(folderID) String(name) int(NumberofFolders) - makes folder in folderID with name numberofFolders times");
                        System.out.println("saveAll String(folderID) - saves all files from clipboard to folder with folderID");
                        System.out.println("delete String(fileId) - deletes file with fileID");
                        System.out.println("move String(fileId) String(folderId) - moves file with fileID to folder with filderID");
                        System.out.println("download String(fileID) downloads file from path to downloads folder");
                        System.out.println("searchByDir String(folderId) String(filename) - searches all files in folder with folderID with full filename");
                        System.out.println("deepSearch String(folderId) String(filename) - searches all files in folder with folderID and first childern with full filename");
                        System.out.println("recursiveSeach String(folderID) String(filename) - searches all files folder with folderID and all directoriums inside with full filename");
                        System.out.println("searchByExtension String(folderID) String(extension) - searches all files in folder with folderID directorium with given extension");
                        System.out.println("beginsWith String(folderID) String(beginingString) - searches all files in folder with folderID directorium that begin with BeginingString");
                        System.out.println("contains String(folderID) String(containingString) - searches all files in folder with folderID directorium with containingString in its name");
                        System.out.println("endsWith String(folderID) String(ending) - searches all files in folder with folderID  directorium with ending in endingString");
                        System.out.println("searchByFileName String(filename) - searches all files for file with full filename");
                        System.out.println("findByDate String(folderID) String(fileName) long(startingDate) long(endingDate) - finds files with filename in folder with folderID  created between start and end date USE UNIX TIME!!!!");
                        System.out.println("hasFile String(folderID) - checks if given folder with folderID  has all folders from clipboard");
                        System.out.println("sortByName (desc/asc) - sorts clipboard by Name");
                        System.out.println("sortByCreationDate (desc/asc) - sorts clipboard by CreationDate");
                        System.out.println("sortByLastModified (desc/asc) - sorts clipboard by LastModified");
                        System.out.println("output bool(fileID) bool(size) bool(created) bool(modified) - outputs files from clipboard with fields enabeld (you can use t/f , y/n , true/false");

                    }
                    break;
                }
                case "exit" : {
                    System.exit(0);
                    break;
                }
                case "configuresize" : {
                    arguments = getArguments(ln);
                    long size;
                    if(arguments.length != 1){
                        System.out.println("need one argument");
                        break;
                    }
                    try {
                        size = Long.parseLong(arguments[0]);
                    } catch (Exception e){
                        System.out.println("Please enter long");
                        break;
                    }
                    storage.configureSize(size);
                    break;
                }
                case "addextensions" :{
                    arguments = getArguments(ln);
                    String[] args;
                    if(arguments.length != 1){
                        System.out.println("need one argument separated by comas if you have more");
                        break;
                    }
                    args = arguments[0].split(",");
                    storage.addExtensions(args);
                    break;
                }case "setextensions" :{
                    arguments = getArguments(ln);
                    String[] args;
                    if(arguments.length != 1){
                        System.out.println("need one argument separated by comas if you have more");
                        break;
                    }
                    args = arguments[0].split(",");
                    storage.setExtensions(args);
                    break;
                }case "removeextensions" :{
                    arguments = getArguments(ln);
                    String[] args;
                    if(arguments.length != 1){
                        System.out.println("need one argument separated by comas if you have more");
                        break;
                    }
                    args = arguments[0].split(",");
                    storage.removeExtensions(args);
                    break;
                }
                case "configuremaxfiles" :{

                    arguments = getArguments(ln);
                    int maxFiles;
                    if(arguments.length == 1){
                        System.out.println("need two argument");
                        break;
                    }
                    try {
                        maxFiles = Integer.parseInt(arguments[1]);
                    } catch (Exception e){
                        System.out.println("Please enter int");
                        break;
                    }
                    storage.configureMaxFiles(arguments[0],maxFiles);
                    break;
                }
                case "configure" :{
                    config = storage.getConfig();
                    MyFile configFile = storage.configure(config);
                    if(configFile!= null)
                        storage.saveAll(storage.getRoot(),configFile);
                    break;
                }
                case "mkdir" :{
                    String path;
                    String name;
                    int numberOfFolders = 0;
                    arguments = getArguments(ln);
                    if(arguments.length != 3){
                        if(arguments.length == 2){
                            numberOfFolders = 1;
                        } else {
                            System.out.println("Unesi argumente lepo ako ne zans kako help");
                            break;
                        }
                    }
                    path = arguments[0];
                    name = arguments[1];
                    if (numberOfFolders == 0)
                        try {
                            numberOfFolders = Integer.parseInt(arguments[2]);
                    } catch (Exception e){
                            System.out.println("3. argument nije int");
                        }
                    storage.mkdir(path,name,numberOfFolders);
                    break;
                }
                case "saveall" :{
                    if(storageString.equalsIgnoreCase("LOCAL")) {
                        arguments = getArguments(ln);
                        if (arguments.length != 1) {
                            System.out.println("need one argument");
                            break;
                        }
                        String path = arguments[0];
                        if (clipboard.isEmpty()) {
                            System.out.println("Clipboard is empty");
                            break;
                        }
                        storage.saveAll(path, toArray(clipboard));
                    }else {
                        arguments = getArguments(ln);
                        if (arguments.length <= 1) {
                            System.out.println("give me file path on windows that you want to upload");
                            break;
                        }
                        String path = arguments[0];
                        for(int i = 1 ; i< arguments.length ; i++){
                            clipboard.add(new MyFile(new File(arguments[i])));
                        }
                        storage.saveAll(path, toArray(clipboard));
                    }

                    break;
                }
                case "delete" :{
                    arguments = getArguments(ln);
                    if(arguments.length != 1){
                        System.out.println("need one argument");
                        break;
                    }
                    String path = arguments[0];
                    storage.delete(path);
                    break;
                }
                case "move":{
                    arguments = getArguments(ln);
                    if(arguments.length != 2){
                        System.out.println("need two arguments");
                        break;
                    }
                    String oldPath = arguments[0];
                    String newPath = arguments[1];
                    storage.move(oldPath,newPath);
                    break;
                }
                case "download" :{
                    arguments = getArguments(ln);
                    if(arguments.length != 1){
                        System.out.println("need one argument");
                        break;
                    }
                    String path = arguments[0];
                    storage.download(path);
                    break;
                }
                case "rename" :{
                    arguments = getArguments(ln);
                    if(arguments.length != 2){
                        System.out.println("need two arguments");
                        break;
                    }
                    String oldPath = arguments[0];
                    String newName = arguments[1];
                    storage.rename(oldPath,newName);
                    break;
                }
                case "searchbydir" :{
                    arguments = getArguments(ln);
                    if(arguments.length != 2){
                        System.out.println("need two arguments");
                        break;
                    }
                    String dir = arguments[0];
                    String filename = arguments[1];
                    clipboard = storage.searchByDir(dir,filename);
                    break;
                }
                case "deepsearch" :{
                    arguments = getArguments(ln);
                    if(arguments.length != 2){
                        System.out.println("need two arguments");
                        break;
                    }
                    String dir = arguments[0];
                    String filename = arguments[1];
                    clipboard = storage.deepSearch(dir,filename);
                    break;
                }
                case "recursivesearch" :{

                    arguments = getArguments(ln);
                    if(arguments.length != 2){
                        System.out.println("need two arguments");
                        break;
                    }
                    String dir = arguments[0];
                    String filename = arguments[1];
                    clipboard = storage.recursiveSearch(dir,filename);
                    break;
                }
                case "searchbyextension" :{
                    arguments = getArguments(ln);
                    if(arguments.length != 2){
                        System.out.println("need two arguments");
                        break;
                    }
                    String dir = arguments[0];
                    String filename = arguments[1];
                    clipboard = storage.searchByExtension(dir,filename);
                    break;
                }
                case "beginswith" :{
                    arguments = getArguments(ln);
                    if(arguments.length != 2){
                        System.out.println("need two arguments");
                        break;
                    }
                    String dir = arguments[0];
                    String filename = arguments[1];
                    clipboard = storage.beginsWith(dir,filename);
                    break;
                }
                case "contains" :{
                    arguments = getArguments(ln);
                    if(arguments.length != 2){
                        if(arguments.length == 1){
                            clipboard = storage.contains(arguments[0],"");
                            break;
                        }else {
                            System.out.println("need two arguments");
                            break;
                        }
                    }
                    String dir = arguments[0];
                    String filename = arguments[1];
                    clipboard = storage.contains(dir,filename);
                    break;
                }
                case "endswith" :{
                    arguments = getArguments(ln);
                    if(arguments.length != 2){
                        System.out.println("need two arguments");
                        break;
                    }
                    String dir = arguments[0];
                    String filename = arguments[1];
                    clipboard = storage.endsWith(dir,filename);
                    break;
                }
                case "searchbyfilename" :{
                    arguments = getArguments(ln);
                    if(arguments.length != 1){
                        clipboard = storage.searchByFileName("");
                    } else {
                        String filename = arguments[0];
                        clipboard = storage.searchByFileName(filename);
                        break;
                    }
                }
                case "findbydate" :{
                    arguments = getArguments(ln);
                    String path;
                    String name;
                    String start;
                    String end;
                    if(arguments.length != 4){
                        System.out.println("need four arguments");
                        break;
                    }
                    path = arguments[0];
                    name = arguments[1];
                    start = arguments[2];
                    end = arguments[3];
                    clipboard = storage.findByDate(path,name,start,end);
                    break;
                }
                case "hasfile" :{
                    arguments = getArguments(ln);
                    if(arguments.length != 1){
                        System.out.println("need one argument");
                        break;
                    }
                    String path = arguments[0];
                    String[] args = new String[clipboard.size()];
                    for (int i = 0 ; i< args.length; i++) {
                        args[i] = clipboard.get(i).getName();
                    }
                    boolean res = storage.hasFile(path,args);
                    System.out.println(res);
                    break;
                }
                case "sortbyname":{
                    arguments = getArguments(ln);
                    if(arguments.length != 1){
                        System.out.println("need one argument");
                        break;
                    }
                    if(clipboard.isEmpty()){
                        System.out.println("Clipboard is empty");
                        break;
                    }
                    String order = arguments[0];
                    clipboard = storage.sortByName(clipboard,order);
                    break;
                }
                case "sortbycreationdate" :{
                    arguments = getArguments(ln);
                    if(arguments.length != 1){
                        System.out.println("need one argument");
                        break;
                    }
                    if(clipboard.isEmpty()){
                        System.out.println("Clipboard is empty");
                        break;
                    }
                    String order = arguments[0];
                    clipboard = storage.sortByCreationDate(clipboard,order);
                    break;
                }
                case "sortbylastmodified" :{
                    arguments = getArguments(ln);
                    if(arguments.length != 1){
                        System.out.println("need one argument");
                        break;
                    }
                    if(clipboard.isEmpty()){
                        System.out.println("Clipboard is empty");
                        break;
                    }
                    String order = arguments[0];
                    clipboard = storage.sortByLastModified(clipboard,order);
                    break;
                }
                case "output" :{
                    boolean path = false;
                    boolean size = false;
                    boolean created = false;
                    boolean modified = false;

                    arguments = getArguments(ln);
                    if(arguments.length != 4){
                        System.out.println("need four arguments");
                        break;
                    }
                    if(arguments[0].equalsIgnoreCase("t")||arguments[0].equalsIgnoreCase("true")||arguments[0].equalsIgnoreCase("y")){
                        path = true;
                    }if(arguments[1].equalsIgnoreCase("t")||arguments[1].equalsIgnoreCase("true")||arguments[1].equalsIgnoreCase("y")){
                        size = true;
                    }if(arguments[2].equalsIgnoreCase("t")||arguments[2].equalsIgnoreCase("true")||arguments[2].equalsIgnoreCase("y")){
                        created = true;
                    }if(arguments[3].equalsIgnoreCase("t")||arguments[3].equalsIgnoreCase("true")||arguments[3].equalsIgnoreCase("y")){
                        modified = true;
                    }
                    if(clipboard.isEmpty()){
                        System.out.println("Clipboard is empty");
                        break;
                    }
                    List<String> out = storage.output(clipboard,path,size,created,modified);
                    for(String s:out){
                        System.out.println(s);
                    }
                    break;
                }
                default:{
                    System.out.println("Unknown command : "+command+" type help to show help about helping you");
                }
            }
            ln = in.nextLine();
            command = getCommand(ln);
        }
    }






}
