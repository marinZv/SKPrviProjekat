package drive;

import RuNode.MyFile;
import Specifikacija.SpecificationImplementation;
import Specifikacija.StorageManager;

import java.io.File;

public class Main {



    public static void main(String[] args) {
        Implementation implementation = new Implementation();
        implementation.createStorage("18S2hV7WPpbx5QjPqtc_hUm7JrquZeEDk");
        implementation.saveAll("18S2hV7WPpbx5QjPqtc_hUm7JrquZeEDk",new MyFile(new File("C:\\Users\\Petar\\Desktop\\Projects\\Java\\Softverske_Komponente_Drive\\src\\test\\java\\placeholder.txt")));
    }
}
