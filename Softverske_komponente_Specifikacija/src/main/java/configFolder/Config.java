package configFolder;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


public class Config {

    public Config(String root){
        this.root = root;
    }

    public Config(String root, float maxSize, List<String> extensions, List<Bans> bansList) {
        this.root = root;
        this.maxSize = maxSize;
        this.extensions = extensions;
        this.bansList = bansList;
    }

    private String root;
    private float maxSize = 1024*1024*1024;
    private List<String> extensions = new ArrayList<>();
    private List<Bans> bansList = new ArrayList<>();

    public void addExtension(String ext){
        extensions.add(ext);
    }

    public void removeExtension(String ext){
        extensions.remove(ext);
    }

   public void addBan(String path,int num){
        removeBan(path);
        bansList.add(new Bans(path,num));
   }
   public void removeBan(String path){
        List<Bans> removes = new ArrayList<>();
        for(Bans b:bansList){
            if (b.getFolderPath().equalsIgnoreCase(path))
                removes.add(b);
        }
        bansList.removeAll(removes);
   }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public float getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(float maxSize) {
        this.maxSize = maxSize;
    }

    public List<String> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<String> extensions) {
        this.extensions = extensions;
    }

    public List<Bans> getBansList() {
        return bansList;
    }

    public void setBansList(List<Bans> bansList) {
        this.bansList = bansList;
    }
}
