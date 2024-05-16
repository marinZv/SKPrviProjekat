package RuNode;

import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Data
@Getter
public class Folder extends MyFile {

    private List<MyFile> children = new ArrayList<>();

    public Folder(){

    }

    public Folder(String fileName,String filePath,long fileSize,long fileCreated, long fileModified){
        super(fileName,filePath,fileSize,fileCreated,fileModified);
    }

    public Folder(java.io.File file){
        super(file);
        java.io.File[] childlist = file.listFiles();
        for(java.io.File f : childlist){
            addChild(new MyFile(f));
        }
    }

    public void addChild(MyFile myFile){
        this.children.add(myFile);
    }

}
