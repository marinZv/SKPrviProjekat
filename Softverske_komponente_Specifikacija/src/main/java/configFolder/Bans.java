package configFolder;

public class Bans {
    private String folderPath;
    private int maxNumber;

    public Bans(String path,int number){
        this.folderPath = path;
        this.maxNumber = number;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public int getMaxNumber() {
        return maxNumber;
    }

    public void setMaxNumber(int maxNumber) {
        this.maxNumber = maxNumber;
    }
}
