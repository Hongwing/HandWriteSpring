package cn.henryhe.io;

public class Resource {

    private final String path;
    private final String name;

    public Resource(String path, String name) {
        this.path = path;
        this.name = name;
    }

    public String getPath() {
        return path;
    }
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "ResourceResolver{" +
                "path='" + path + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
