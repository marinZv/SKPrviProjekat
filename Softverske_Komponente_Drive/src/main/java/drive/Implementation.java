package drive;

import com.google.gson.Gson;
import configFolder.Bans;
import configFolder.Config;
import RuNode.MyFile;
import Specifikacija.SpecificationImplementation;
import Specifikacija.StorageManager;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Implementation extends SpecificationImplementation {

    static Drive service;

    static{
        try {
            service = GoogleDriveCredentials.getDriveService();
        } catch (IOException e) {
            System.out.println("ERROR! Couldn't connect to the google drive");
            System.exit(1);
        }
        StorageManager.regiser(new Implementation());
    }


    @Override
    public void createStorage(String path) {
        super.setRoot(path);
        super.setConfig(new Config(path));

        List<File> files = new ArrayList<>();
        String pageToken;
        do {
            FileList result = null;
            try {
                result = service.files().list()
                        .setQ("'"+super.getRoot()+"' in parents and name = 'config.json'")
                        .setFields("files(id, name, size, modifiedTime, createdTime,parents,mimeType)")
                        .execute();
            } catch (IOException e) {
                System.out.println("Error with google drive response");
            }
            if(result == null){
                configure(super.getConfig());
                return;
            }
            files.addAll(result.getFiles());
            pageToken = result.getNextPageToken();
        }while (pageToken!= null);
        if(files == null || files.isEmpty()){
            configure(super.getConfig());
            return;
        }

        File jsonconfigfile = files.get(0);
        download(jsonconfigfile.getId());
        java.io.File file = new java.io.File((Paths.get(System.getProperty("user.home"),"Downloads")).toString(),jsonconfigfile.getName());
        if(file.exists()){
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                StringBuilder stringBuilder = new StringBuilder();

                while((line = reader.readLine()) != null){
                    stringBuilder.append(line);
                }
                reader.close();
                String s = stringBuilder.toString();
                Config conf = new Gson().fromJson(s,Config.class);
                super.setConfig(conf);
            }catch (Exception e){
                System.out.println("Error with found config.json deploying default values");
                configure(super.getConfig());
                return;
            }
            file.delete();
        }
        configure(super.getConfig());

    }

    @Override
    public void mkdir(String parentId, String folderName, int numberOfFolders) {


        for(int i = 1; i <= numberOfFolders; i++){

            File fileMetaData = new File();
            fileMetaData.setName(folderName + Integer.toString(i));
            fileMetaData.setParents(Collections.singletonList(parentId));
            fileMetaData.setMimeType("application/vnd.google-apps.folder");
            try{
                File file = service.files().create(fileMetaData)
                        .setFields("name,id, parents")
                        .execute();
                if(!check(new MyFile(fileMetaData.getName(),file.getId(),0,0,0))){
                    return;
                }
            } catch (IOException e) {
                System.out.println("Error with google drive response");
                return;
            }

        }

    }


        @Override
        public void saveAll(String folderID, MyFile... myFiles) {
            List<java.io.File> files = new ArrayList<>();
            for(MyFile f: myFiles){
                files.add(new java.io.File(f.getPath()));
            }
            for(java.io.File f:files) {
                try {
                    File fileMetadata = new File();
                    String[] filename = f.getPath().split("\\\\");
                    fileMetadata.setName(filename[filename.length-1]);
                    fileMetadata.setParents(Collections.singletonList(folderID));
                    FileContent content = new FileContent("mime/type", f);
                    File file = service.files().create(fileMetadata,content).setFields("id").execute();
                } catch (Exception e) {
                    System.out.println("Error with google drive response");
                    return;
                }
            }

        }

    @Override
    public boolean check(MyFile file) {
        if(!super.check(file))
            return false;
        String fileid = file.getPath();

        List<File> files = new ArrayList<>();
        String pageToken = null;
        do {
            FileList result = null;
            try {
                result = service.files().list()
                        .setQ("name = '"+file.getName()+"'")
                        .setFields("files(id, name, size, modifiedTime, createdTime,parents,mimeType)")
                        .execute();
            } catch (Exception e){
                System.out.println("Error with google drive response");
                return true;
            }
            if(result == null || result.isEmpty())
                return true;
            files.addAll(result.getFiles());
            pageToken = result.getNextPageToken();
        }while (pageToken!= null);
        String parentid = null;
        for(File f : files){
            if(f.getId().equals(fileid))
                parentid = f.getParents().get(0);
        }
        int filesinparent;
        if(parentid == null)
            return true;
        do {
            FileList result = null;
            try {
                result = service.files().list()
                        .setQ("'"+parentid+"' in parents")
                        .setFields("files(id, name, size, modifiedTime, createdTime,parents,mimeType)")
                        .execute();
            } catch (Exception e){
                System.out.println("Error with google drive response");
                return true;
            }

            files = result.getFiles();
            filesinparent = files.size();
            pageToken = result.getNextPageToken();
        }while (pageToken!= null);
        int maxfilesinparent = -1;
        List<Bans> banned = super.getConfig().getBansList();
        for(Bans b : banned){
            if (b.getFolderPath().equalsIgnoreCase(parentid))
                maxfilesinparent = b.getMaxNumber();
        }
        if(filesinparent >= maxfilesinparent && maxfilesinparent!= -1)
            return false;
        return true;
    }

    @Override
    public void delete(String fileId) {
        try {
            service.files().delete(fileId).execute();
        } catch (IOException e) {
            System.out.println("Error with google drive response");
            return;
        }
    }

    @Override
    public MyFile configure(Config config) {
        List<MyFile> files = searchByDir(super.getRoot(),"config.json");
        for(MyFile f : files){
            delete(f.getPath());
        }
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();

        File conf = new File();
        String json  = builder.create().toJson(config);
        try {
            File fileMetadata = new File();
            Path tempfile = Files.createTempFile("config",".json");
            Files.write(tempfile,json.getBytes(StandardCharsets.UTF_8));
            fileMetadata.setName("config.json");
            fileMetadata.setParents(Collections.singletonList(getRoot()));
            fileMetadata.setMimeType("application/json");
            FileContent content = new FileContent("application/json",tempfile.toFile());
            File file = service.files().create(fileMetadata,content).setFields("id").execute();
        } catch (IOException e) {
            System.out.println("Error in creating temp file");
            return null;
        } catch (Exception e){
            System.out.println("Error with google drive response");
            return null;
        }
        return null;
    }

    @Override
    public void move(String fileId, String folderId) {

        // Retrieve the existing parents to remove
        File file = null;
        try {
            file = service.files().get(fileId)
                    .setFields("name,id, parents")
                    .execute();
        } catch (IOException e) {
            System.out.println("Error with google drive response");
            return;
        }
        StringBuilder previousParents = new StringBuilder();
        for (String parent : file.getParents()) {
            previousParents.append(parent);
            previousParents.append(',');
        }
        try {
            // Move the file to the new folder
            if(!check(new MyFile(file.getName(),file.getId(), file.size(), 0,0)))
                return;
            file = service.files().update(fileId, null)
                    .setAddParents(folderId)
                    .setRemoveParents(previousParents.toString())
                    .setFields("name, id, parents")
                    .execute();
            if(!check(new MyFile(file.getName(),file.getId(), file.size(), 0,0))){
                file = service.files().update(fileId, null)
                        .setAddParents(previousParents.toString())
                        .setRemoveParents(folderId)
                        .setFields("name, id, parents")
                        .execute();
                return;
            }
        } catch (GoogleJsonResponseException e) {
            System.err.println("Unable to move file: " + e.getDetails());
            try {
                throw e;
            } catch (GoogleJsonResponseException ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void download(String realFileId) {

        OutputStream outputStream = null;
        FileWriter fw = null;
        try {
            outputStream = new ByteArrayOutputStream();
            String name = service.files().get(realFileId).execute().getName();
            service.files().get(realFileId)
                    .executeMediaAndDownloadTo(outputStream);
            java.io.File file = new java.io.File((Paths.get(System.getProperty("user.home"),"Downloads")).toString(),name);
            fw = new FileWriter(file.getPath());
            fw.write(String.valueOf(outputStream));
        } catch (GoogleJsonResponseException e) {
            System.err.println("Unable to move file: " + e.getDetails());
            try {
                throw e;
            } catch (GoogleJsonResponseException ex) {
                ex.printStackTrace();
            }
        } catch (IOException e) {
            System.out.println("Error coulnt downlaod file");
            return;
        } finally {
            try {
                fw.close();
                outputStream.close();
            } catch (IOException e) {
            }
        }

    }

    @Override
    public void rename(String fileId, String fileName) {
        try {
            File file = service.files().get(fileId).setFields("name").execute();
            file.setName(fileName);
            service.files().update(fileId,file).setFields("name").execute();
        }catch (Exception e){
            System.out.println("Error with google drive response");
            return;
        }
    }

    @Override
    public void absolutePath(String s) {

    }



    @Override
    public List<MyFile> searchByDir(String folderName, String fileName) {

        List<File> files = new ArrayList<>();
        List<MyFile> myFiles = new ArrayList<>();
        String pageToken = null;
        do {
            FileList result = null;
            try {
                result = service.files().list()
                        .setQ("'"+folderName+"' in parents and name = '"+fileName+"'")
                        .setFields("files(id, name, size, modifiedTime, createdTime,parents,mimeType)")
                        .execute();
            } catch (Exception e){
                System.out.println("Error with google drive response");
                return myFiles;
            }
            files.addAll(result.getFiles());
            pageToken = result.getNextPageToken();
        }while (pageToken!= null);
        for (File file : files){
            long size;
            if (file.getMimeType().contains("folder")){
                size = 0;
            } else {
                size = file.getSize();
            }
            myFiles.add(new MyFile(file.getName(),file.getId(),size,file.getCreatedTime().getValue(),file.getModifiedTime().getValue()));
        }
        return myFiles;
    }

    @Override
    public List<MyFile> deepSearch(String folderName, String fileName) {
        List<File> files = new ArrayList<>();
        List<MyFile> myFiles = new ArrayList<>();
        String pageToken = null;
        do {
            FileList result = null;
            try {
                result = service.files().list()
                        .setQ("'"+folderName+"' in parents")
                        .setFields("files(id, name, size, modifiedTime, createdTime,parents,mimeType)")
                        .execute();
            } catch (Exception e){
                System.out.println("Error with google drive response");
                return myFiles;
            }
            files.addAll(result.getFiles());
            pageToken = result.getNextPageToken();
        }while (pageToken!= null);
        for (File file : files){
            long size;
            if (file.getMimeType().contains("folder")){
                size = 0;
                myFiles.addAll(searchByDir(file.getId(),fileName));
            } else {
                size = file.getSize();
            }
            if(file.getName().equalsIgnoreCase(fileName))
                myFiles.add(new MyFile(file.getName(),file.getId(),size,file.getCreatedTime().getValue(),file.getModifiedTime().getValue()));
        }
        return myFiles;
    }

    @Override
    public List<MyFile> recursiveSearch(String folderName, String fileName) {

        List<File> files = new ArrayList<>();
        List<MyFile> myFiles = new ArrayList<>();
        String pageToken = null;
        do {
            FileList result = null;
            try {
                result = service.files().list()
                        .setQ("'"+folderName+"' in parents")
                        .setFields("files(id, name, size, modifiedTime, createdTime,parents,mimeType)")
                        .execute();
            } catch (Exception e){
                System.out.println("Error with google drive response");
                return myFiles;
            }
            files.addAll(result.getFiles());
            pageToken = result.getNextPageToken();
        }while (pageToken!= null);
        for (File file : files){
            long size;
            if (file.getMimeType().contains("folder")){
                size = 0;
                myFiles.addAll(recursiveSearch(file.getId(),fileName));
            } else {
                size = file.getSize();
            }
            if(file.getName().contains(fileName))
                myFiles.add(new MyFile(file.getName(),file.getId(),size,file.getCreatedTime().getValue(),file.getModifiedTime().getValue()));
        }
        return myFiles;
    }

    @Override
    public List<MyFile> searchByExtension(String folderName, String extension) {

        List<File> files = new ArrayList<>();
        List<MyFile> myFiles = new ArrayList<>();
        String pageToken = null;
        do {
            FileList result = null;
            try {
                result = service.files().list()
                        .setQ("'"+folderName+"' in parents")
                        .setFields("files(id, name, size, modifiedTime, createdTime,parents,mimeType)")
                        .execute();
            } catch (Exception e){
                System.out.println("Error with google drive response");
                return myFiles;
            }
            files.addAll(result.getFiles());
            pageToken = result.getNextPageToken();
        }while (pageToken!= null);
        for (File file : files){
            long size;
            if (file.getMimeType().contains("folder")){
                size = 0;
            } else {
                size = file.getSize();
            }
            if(file.getName().endsWith(extension))
                myFiles.add(new MyFile(file.getName(),file.getId(),size,file.getCreatedTime().getValue(),file.getModifiedTime().getValue()));
        }
        return myFiles;
    }

    @Override
    public List<MyFile> beginsWith(String folderName, String startsWith) {

        List<File> files = new ArrayList<>();
        List<MyFile> myFiles = new ArrayList<>();
        String pageToken = null;
        do {
            FileList result = null;
            try {
                result = service.files().list()
                        .setQ("'"+folderName+"' in parents")
                        .setFields("files(id, name, size, modifiedTime, createdTime,parents,mimeType)")
                        .execute();
            } catch (Exception e){

                System.out.println("Error with google drive response");
                return myFiles;
            }
            files.addAll(result.getFiles());
            pageToken = result.getNextPageToken();
        }while (pageToken!= null);
        for (File file : files){
            long size;
            if (file.getMimeType().contains("folder")){
                size = 0;
            } else {
                size = file.getSize();
            }
            if(file.getName().toLowerCase().startsWith(startsWith.toLowerCase()))
                myFiles.add(new MyFile(file.getName(),file.getId(),size,file.getCreatedTime().getValue(),file.getModifiedTime().getValue()));
        }
        return myFiles;
    }

    @Override
    public List<MyFile> contains(String folderName, String fileName) {

        List<File> files = new ArrayList<>();
        List<MyFile> myFiles = new ArrayList<>();
        String pageToken = null;
        do {
            FileList result = null;
            try {
                result = service.files().list()
                        .setQ("'"+folderName+"' in parents")
                        .setFields("files(id, name, size, modifiedTime, createdTime,parents,mimeType)")
                        .execute();
            } catch (Exception e){
                System.out.println("Error with google drive response");
                return myFiles;
            }
            files.addAll(result.getFiles());
            pageToken = result.getNextPageToken();
        }while (pageToken!= null);
        for (File file : files){
            long size;
            if (file.getMimeType().contains("folder")){
                size = 0;
            } else {
                size = file.getSize();
            }
            if(file.getName().toLowerCase().contains(fileName.toLowerCase()));
                myFiles.add(new MyFile(file.getName(),file.getId(),size,file.getCreatedTime().getValue(),file.getModifiedTime().getValue()));
        }
        return myFiles;
    }

    @Override
    public List<MyFile> endsWith(String folderName, String endsWith) {

        List<File> files = new ArrayList<>();
        List<MyFile> myFiles = new ArrayList<>();
        String pageToken = null;
        do {
            FileList result = null;
            try {
                result = service.files().list()
                        .setQ("'"+folderName+"' in parents")
                        .setFields("files(id, name, size, modifiedTime, createdTime,parents,mimeType)")
                        .execute();
            } catch (Exception e){
                System.out.println("Error with google drive response");
                return myFiles;
            }
            files.addAll(result.getFiles());
            pageToken = result.getNextPageToken();
        }while (pageToken!= null);
        for (File file : files){
            long size;
            if (file.getMimeType().contains("folder")){
                size = 0;
            } else {
                size = file.getSize();
            }
            if(file.getName().split("\\.")[0].toLowerCase().endsWith(endsWith.toLowerCase()))
                myFiles.add(new MyFile(file.getName(),file.getId(),size,file.getCreatedTime().getValue(),file.getModifiedTime().getValue()));
        }
        return myFiles;
    }

    @Override
    public boolean hasFile(String folderName, String... strings) {

        List<MyFile> myFiles = new ArrayList<>();
        for(String s : strings){
           myFiles.addAll(searchByDir(folderName, s));
        }

        if(myFiles.size() == strings.length)
            return true;

        return false;
    }

    @Override
    public List<MyFile> searchByFileName(String fileName) {
        return recursiveSearch(super.getRoot(),fileName);
    }

    @Override
    public List<MyFile> findByDate(String folderName, String fileName, String startLocalDate, String endLocalDate) {

        List<File> files = new ArrayList<>();
        List<MyFile> myFiles = new ArrayList<>();
        String pageToken = null;
        do {
            FileList result = null;
            try {
                result = service.files().list()
                        .setQ("'"+folderName+"' in parents and name ='"+fileName+"'")
                        .setFields("files(id, name, size, modifiedTime, createdTime,parents,mimeType)")
                        .execute();
            } catch (Exception e){
                System.out.println("Error with google drive response");
                return myFiles;
            }
            files.addAll(result.getFiles());
            pageToken = result.getNextPageToken();
        }while (pageToken!= null);
        for (File file : files){
            long size;
            if (file.getMimeType().contains("folder")){
                size = 0;
            } else {
                size = file.getSize();
            }
            long start = 0;
            long end = 0;
            try {
                start = Long.parseLong(startLocalDate);
                end = Long.parseLong(endLocalDate);
            } catch (Exception e){
                System.out.println("Please enter unix date");
            }
            if(file.getCreatedTime().getValue()>=start &&(file.getCreatedTime().getValue() <= end || end == 0))
                myFiles.add(new MyFile(file.getName(),file.getId(),size,file.getCreatedTime().getValue(),file.getModifiedTime().getValue()));
        }
        return myFiles;
    }
}
