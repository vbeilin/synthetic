package vb.synthetic.testutils;

public class PredefinedClassLoader extends ClassLoader {
    private final String binaryName;
    private final Class<?> predef;
    
    public PredefinedClassLoader(ClassLoader parent, String binaryName, byte[] bytes) {
        super(parent);
        this.binaryName = binaryName;
        this.predef = defineClass(null, bytes, 0, bytes.length);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (name.equals(binaryName)) {
            return predef;
        }
        return super.loadClass(name);
    }
}