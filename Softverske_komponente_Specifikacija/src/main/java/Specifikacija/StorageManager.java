package Specifikacija;

public class StorageManager {
    private static SpecificationImplementation storage = null;

    public static void regiser(SpecificationImplementation specificationImplementation){
        storage = specificationImplementation;
    }

    public static SpecificationImplementation getStorage(String root){
        storage.createStorage(root);
        return storage;
    }
}
