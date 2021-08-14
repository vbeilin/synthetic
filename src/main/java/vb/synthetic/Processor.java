package vb.synthetic;

import java.io.IOException;
import java.io.InputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import vb.synthetic.anno.Synthetic;

public class Processor {
    private final static String ANNO = 'L' + Synthetic.class.getCanonicalName().replace('.', '/') + ';';

    public static byte[] processClass(InputStream input) throws IOException {
        return processClass(new ClassReader(input));
    }

    public static byte[] processClass(byte[] bytes) {
        return processClass(new ClassReader(bytes));
    }

    private static byte[] processClass(ClassReader reader) {
        ClassWriter writer = new ClassWriter(0);
        AddSynth addSynth = new AddSynth(writer);
        reader.accept(addSynth, 0);
        return writer.toByteArray();
    }

    private static class AddSynth extends ClassNode {
        private final ClassVisitor cv;

        AddSynth(ClassVisitor cv) {
            super(Opcodes.ASM9);
            this.cv = cv;
        }

        @Override
        public void visitEnd() {
            for (MethodNode m : this.methods) {
                if (m.invisibleAnnotations != null) {
                    for (AnnotationNode a : m.invisibleAnnotations) {
                        if (a.desc.equals(ANNO)) {
                            m.access |= Opcodes.ACC_SYNTHETIC;
                        }
                    }
                }
            }
            accept(cv);
        }
    }
}
