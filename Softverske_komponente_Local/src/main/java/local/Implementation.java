package local;

import com.google.gson.Gson;
import configFolder.Config;
import RuNode.Folder;
import RuNode.MyFile;
import Specifikacija.SpecificationImplementation;
import Specifikacija.StorageManager;
import configFolder.Bans;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Implementation extends SpecificationImplementation {

    private String defaultPath = System.getProperty("user.dir");
    private String path;


    static {
        StorageManager.regiser(new Implementation());
    }

    @Override
    public void createStorage(String mypath) {
        super.setRoot(mypath);
        super.setConfig(new Config(mypath));
        File cfg = new File(super.getRoot() + "\\config.json");
        if (cfg.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(cfg));
                String line = null;
                StringBuilder stringBuilder = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();
                String s = stringBuilder.toString();
                Config conf = new Gson().fromJson(s, Config.class);
                super.setConfig(conf);
            } catch (Exception e) {
                System.out.println("Error while trying to read config.json deploying default values");
                configure(super.getConfig());
            }
        } else {

            defaultPath = mypath;
            File rootFolder = new File(super.getRoot());
            boolean fin = rootFolder.mkdir();
            if (rootFolder.exists())
                fin = true;
            if (fin) {
                super.setRoot(super.getRoot());
            } else {
                String idiotUser = defaultPath.concat(super.getRoot());
                rootFolder = new File(idiotUser);
                rootFolder.mkdir();
                super.setRoot(idiotUser);
            }
            System.out.println(super.getRoot());
            configure(super.getConfig());
        }
        }


    @Override
    public boolean check(MyFile file) {
        if(!super.check(file))
            return false;
        String[] components = file.getPath().split("\\\\");
        String path = file.getPath().substring(0,file.getPath().length()-components[components.length-1].length()-1).toLowerCase();

        List<Bans> banned = super.getConfig().getBansList();
        if(banned == null)
            return true;
        int maxFiles = -1;
        for(Bans b:banned){
            if (b.getFolderPath().equalsIgnoreCase(path))
                maxFiles = b.getMaxNumber();
        }
        if(new File(path).listFiles() == null)
            return true;
        if(new File(path).listFiles().length >= maxFiles && maxFiles != -1)
            return false;
        return true;
    }

    @Override
    public void mkdir(String path ,String name, int number_of_folders) {
        ArrayList<Folder> folders = new ArrayList<>();
        for(int i = 1 ; i<=number_of_folders;i++){
            File folder = new File(getRoot()+"\\"+path+"\\"+name.concat(Integer.toString(i)));
            if(!check(new MyFile(folder.getName(),folder.getPath(),0,0,0)))
                return;
            boolean fin = folder.mkdir();
            if(fin){
                Folder folder1 = new Folder();
                folder1.setName(name.concat(Integer.toString(i)));
                folder1.setPath(path);
                folders.add(folder1);
            }
        }
        return;
    }

    @Override
    public void saveAll(String s,MyFile...files){
        for (MyFile myfile: files) {
            if(check(myfile)) {
                String path = defaultPath + "\\" + s + "\\" + myfile.getName();
                File file = new File(path);
                if (myfile.getLocalFile().exists())
                    file = myfile.getLocalFile();
                myfile.setPath(path);
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    System.out.println("Error file already exists");
                }
            }
        }
    }

    @Override
    public void delete(String s) {
        File f = null;
        try {
            f = new File(s);
            f.delete();
        } catch (Exception e){
            if(!f.exists())
                System.out.println("file already gone");
            if(f.listFiles().length>0)
                System.out.println("File has children unsuported operation");
            else
            System.out.println("Error file in use");
        }
    }

    @Override
    public void move(String oldLocation, String newlocation) {
        File original = new File(getRoot(),oldLocation);
        String[] splited = oldLocation.split("\\\\");
        File newPath = new File(getRoot()+"\\"+newlocation,splited[splited.length-1]);
        if(!check(new MyFile(newPath)))
            return;
        if(original.exists() && !newPath.exists())
            try {
                Files.move(original.toPath(),newPath.toPath());
            } catch (Exception e){
                if(original.listFiles().length>0){
                    System.out.println("File has children operation unsuported");
                } else {
                    System.out.println("File in use");
                }
            }
    }

    @Override
    public void download(String curr) {
        File original = new File(getRoot(),curr);
        String[] splited = curr.split("\\\\");
        File newPath = new File((Paths.get(System.getProperty("user.home"),"Downloads")).toString(),splited[splited.length-1]);
        if(original.exists() && !newPath.exists())
            try {
                Files.copy(original.toPath(),newPath.toPath());
            } catch (Exception e){
                System.out.println("Error file already exists");
            }
    }

    @Override
    public void rename(String oldLocation, String newName) {
        String[] dirs = oldLocation.split("\\\\");
        File old = new File(getRoot()+"\\"+oldLocation);
        dirs[dirs.length-1] = newName;
        String name = String.join("\\",dirs);
        File renamed = new File(getRoot()+"\\"+name);
        if(check(new MyFile(renamed)))
            old.renameTo(renamed);
    }

    @Override
    public void absolutePath(String s) {
        path = defaultPath.concat(s);
    }


    @Override
    public List<MyFile> searchByDir(String directory, String fileName) {
        ArrayList<MyFile> myFiles = new ArrayList<>();
        File file = new File(getRoot()+"\\"+directory);
        File[] files = file.listFiles();
        for(File f : files){
            if(f.getAbsolutePath().toLowerCase().endsWith(fileName.toLowerCase())){
                MyFile myFile = new MyFile(f);
                myFiles.add(myFile);
            }

        }
        return myFiles;
    }

    @Override
    public List<MyFile> deepSearch(String directory, String fileName) {
        ArrayList<MyFile> myFiles = new ArrayList<>();
        File file = new File(directory);
        File[] files = file.listFiles();
        for(File f : files){
            if(f.isDirectory()){
                File[] newfiles = f.listFiles();
                for(File f2 : newfiles){
                    if(f2.getAbsolutePath().toLowerCase().endsWith(fileName.toLowerCase())){
                        MyFile myFile = new MyFile(f2);
                        myFiles.add(myFile);
                    }
                }
            } else if(f.getAbsolutePath().toLowerCase().endsWith(fileName.toLowerCase())){
                MyFile myFile = new MyFile(f);
                myFiles.add(myFile);
            }

        }
        return myFiles;
    }

    @Override
    public List<MyFile> recursiveSearch(String directory, String fileName) {
        ArrayList<MyFile> myFiles = new ArrayList<>();
        File file = new File(directory);
        File[] files = file.listFiles();
        if(files != null)
            for(File f : files){
            if(f.isDirectory())
                myFiles.addAll(recursiveSearch(f.getAbsolutePath(),fileName));
            if(f.getAbsolutePath().toLowerCase().endsWith(fileName.toLowerCase())){
                MyFile myFile = new MyFile(f);
                myFiles.add(myFile);
            }

        }
        return myFiles;
    }

    @Override
    public List<MyFile> searchByExtension(String directory, String extension) {
        ArrayList<MyFile> myFiles = new ArrayList<>();
        File file = new File(getRoot()+"\\"+directory);
        File[] files = file.listFiles();
        for(File f : files){
            if(f.getAbsolutePath().toLowerCase().endsWith(extension.toLowerCase())){
                MyFile myFile = new MyFile(f);
                myFiles.add(myFile);
            }

        }
        return myFiles;
    }

    @Override
    public List<MyFile> beginsWith(String directory, String begining) {
        ArrayList<MyFile> myFiles = new ArrayList<>();
        File file = new File(getRoot()+"\\"+directory);
        File[] files = file.listFiles();
        for(File f : files){
            String[] filst = f.getAbsolutePath().split("\\\\");
            if(filst[filst.length-1].toLowerCase().startsWith(begining.toLowerCase())){
                MyFile myFile = new MyFile(f);
                myFiles.add(myFile);
            }

        }
        return myFiles;
    }

    @Override
    public List<MyFile> contains(String directory, String contains) {
        ArrayList<MyFile> myFiles = new ArrayList<>();
        File file = new File(getRoot()+"\\"+directory);
        File[] files = file.listFiles();
        for(File f : files){
            String[] filst = f.getAbsolutePath().split("\\\\");
            if(filst[filst.length-1].toLowerCase().contains(contains.toLowerCase())){
                MyFile myFile = new MyFile(f);
                myFiles.add(myFile);
            }

        }
        return myFiles;
    }

    @Override
    public List<MyFile> endsWith(String directory, String ending) {

        ArrayList<MyFile> myFiles = new ArrayList<>();
        File file = new File(getRoot()+"\\"+directory);
        File[] files = file.listFiles();
        for(File f : files){
            String[] filst = f.getAbsolutePath().split("\\\\");
            String[] noextention = filst[filst.length-1].split("\\.");
            if(noextention[0].toLowerCase().endsWith(ending.toLowerCase())){
                MyFile myFile = new MyFile(f);
                myFiles.add(myFile);
            }

        }
        return myFiles;
    }

    @Override
    public boolean hasFile(String directory, String... strings) {
        String[] filesstrings = strings;
        ArrayList<MyFile> myFiles = new ArrayList<>();
        File file = new File(getRoot()+"\\"+directory);
        File[] files = file.listFiles();
        for(File f : files){
            String[] filst = f.getAbsolutePath().split("\\\\");
            for (String s:filesstrings) {
                if(filst[filst.length-1].toLowerCase().startsWith(s.toLowerCase())){
                    MyFile myFile = new MyFile(f);
                    myFiles.add(myFile);
                }
            }

        }
        return !myFiles.isEmpty();
    }

    @Override
    public List<MyFile> searchByFileName(String s) {
        List<MyFile> myFiles;
        ArrayList<MyFile> finfiles = new ArrayList<>();
        File file = new File(super.getRoot());
        myFiles = recursiveSearch(getRoot(),s);
        for(MyFile f : myFiles){
            if(f.getName().equalsIgnoreCase(s)){
                finfiles.add(f);
            }

        }
        return myFiles;
    }


    @Override
    public List<MyFile> findByDate(String directory, String fileName, String startDate, String endDate) {
        long start = 0;
        long end = 0;
        try {
            start = Long.parseLong(startDate);
            end = Long.parseLong(endDate);
        } catch (Exception e){
            System.out.println("Please make sure you entered unix time");
        }
        List<MyFile> files = new ArrayList<>();
        files = searchByDir(directory,fileName);
        for (MyFile myFile : files){
            if(myFile.getDateCreated()>end && myFile.getDateCreated()<start){
                files.remove(myFile);
            }
        }
        return files;
    }


}
