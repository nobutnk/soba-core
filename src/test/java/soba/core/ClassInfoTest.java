package soba.core;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Test;

import soba.core.ClassInfo;

public class ClassInfoTest implements ExampleProgram {

    @Test
    public void testClassInfo01() throws Exception {
        String fileName = "target/test-classes/" + CLASS_D + ".class";
        ClassInfo c = new ClassInfo(fileName, new FileInputStream(fileName));

        assertThat(c.getPackageName(), is("soba/testdata/inheritance1"));
        assertThat(c.getClassName(), is(CLASS_D));
        assertThat(c.getSuperClass(), is(CLASS_C));
        assertThat(c.getInterfaces(),
                containsInAnyOrder("soba/testdata/inheritance1/I", "soba/testdata/inheritance1/K"));
        assertThat(c.getHash(), is(notNullValue()));
        assertThat(c.getClassDirPath(), is("soba" + File.separator + "testdata" + File.separator + "inheritance1"));
        assertThat(c.getClassFileName(), is(fileName));
        assertThat(c.getSourceFileName(),
                is("soba" + File.separator + "testdata" + File.separator + "inheritance1" + File.separator + "D.java"));
        assertThat(c.getLabel(), is(nullValue()));
        assertThat(c.isLibrary(), is(false));

        assertThat(c.getMethodCount(), is(11));
        assertThat(c.getMethods(), hasSize(11));
        assertThat(c.getMethod(0), is(notNullValue()));
        assertThat(c.findMethod("m", "()V"), is(notNullValue()));
        assertThat(c.findMethod("n", "()V"), is(notNullValue()));
        assertThat(c.findMethod("x", "(I)V"), is(notNullValue()));
        assertThat(c.findMethod("example", "(IJDLjava/lang/String;)I"), is(notNullValue()));
        assertThat(c.findMethod("toString", "()Ljava/lang/String;"), is(notNullValue()));
        assertThat(c.findMethod("notExist", "()V"), is(nullValue()));

        assertThat(c.getFieldCount(), is(0));
        assertThat(c.getFields(), is(empty()));

        assertThat(c.isInterface(), is(false));
        assertThat(c.isEnum(), is(false));

    }

    @Test
    public void testClassInfo02() throws Exception {
        String fileName = "target/test-classes/" + CLASS_H + ".class";
        ClassInfo c = new ClassInfo(fileName, new FileInputStream(fileName), "label");

        assertThat(c.getPackageName(), is("soba/testdata/inheritance2"));
        assertThat(c.getClassName(), is(CLASS_H));
        assertThat(c.getSuperClass(), is(CLASS_D));
        assertThat(c.getInterfaces(), is(empty()));
        assertThat(c.getHash(), is(notNullValue()));
        assertThat(c.getClassDirPath(), is("soba" + File.separator + "testdata" + File.separator + "inheritance2"));
        assertThat(c.getClassFileName(), is(fileName));
        assertThat(c.getSourceFileName(),
                is("soba" + File.separator + "testdata" + File.separator + "inheritance2" + File.separator + "H.java"));
        assertThat(c.getLabel(), is("label"));
        assertThat(c.isLibrary(), is(false));

        assertThat(c.getMethodCount(), is(4));
        assertThat(c.getMethods(), hasSize(4));
        assertThat(c.getMethod(0), is(notNullValue()));
        assertThat(c.findMethod("n", "()V"), is(notNullValue()));
        assertThat(c.findMethod("p", "(I)V"), is(notNullValue()));
        assertThat(c.findMethod("q", "(D)V"), is(notNullValue()));
        assertThat(c.findMethod("<init>", "()V"), is(notNullValue()));

        assertThat(c.getFieldCount(), is(1));
        assertThat(c.getFields(), hasSize(1));
        assertThat(c.getField(0), is(notNullValue()));
        assertThat(c.findField("x", "I"), is(notNullValue()));
    }

    @Test
    public void testLibrary01() throws IOException {
        String fileName = "target/test-classes/" + CLASS_H + ".class";
        ClassInfo c = ClassInfo.createLibraryClass(fileName, new FileInputStream(fileName));
        assertThat(c.isLibrary(), is(true));
    }

    @Test
    public void testClassInfo03() throws Exception {
        String fileName = "target/test-classes/" + CLASS_I + ".class";
        ClassInfo c = new ClassInfo(fileName, new FileInputStream(fileName));

        assertThat(c.getPackageName(), is("soba/testdata/inheritance1"));
        assertThat(c.getClassName(), is(CLASS_I));
        assertThat(c.getHash(), is(notNullValue()));
        assertThat(c.getClassDirPath(), is("soba" + File.separator + "testdata" + File.separator + "inheritance1"));
        assertThat(c.getClassFileName(), is(fileName));
        assertThat(c.getSourceFileName(),
                is("soba" + File.separator + "testdata" + File.separator + "inheritance1" + File.separator + "I.java"));
        assertThat(c.getLabel(), is(nullValue()));
        assertThat(c.isLibrary(), is(false));

        assertThat(c.getMethodCount(), is(1));
        assertThat(c.getMethods(), hasSize(1));
        assertThat(c.getMethod(0), is(notNullValue()));
        assertThat(c.findMethod("m", "()V"), is(notNullValue()));

        assertThat(c.getFieldCount(), is(1));
        assertThat(c.getFields().get(0), is(notNullValue()));

        assertThat(c.isInterface(), is(true));
        assertThat(c.isEnum(), is(false));
    }

    @Test
    public void testClassInfo04() throws Exception {
        String fileName = "target/test-classes/" + CLASS_L + ".class";
        ClassInfo c = new ClassInfo(fileName, new FileInputStream(fileName));

        assertThat(c.getPackageName(), is("soba/testdata/inheritance1"));
        assertThat(c.getClassName(), is(CLASS_L));
        assertThat(c.getHash(), is(notNullValue()));
        assertThat(c.getClassDirPath(), is("soba" + File.separator + "testdata" + File.separator + "inheritance1"));
        assertThat(c.getClassFileName(), is(fileName));
        assertThat(c.getSourceFileName(),
                is("soba" + File.separator + "testdata" + File.separator + "inheritance1" + File.separator + "L.java"));
        assertThat(c.getLabel(), is(nullValue()));
        assertThat(c.isLibrary(), is(false));

        assertThat(c.getMethodCount(), is(5));
        assertThat(c.getMethods(), hasSize(5));
        assertThat(c.getMethod(0), is(notNullValue()));
        assertThat(c.findMethod("m", "(Lsoba/testdata/inheritance1/L;)Z"), is(notNullValue()));
        // for Enum
        assertThat(c.findMethod("valueOf", "(Ljava/lang/String;)Lsoba/testdata/inheritance1/L;"), is(notNullValue()));
        // for Enum
        assertThat(c.findMethod("values", "()[Lsoba/testdata/inheritance1/L;"), is(notNullValue()));
        assertThat(c.getFieldCount(), is(4));
        assertThat(c.getFields().get(0), is(notNullValue()));

        assertThat(c.isInterface(), is(false));
        assertThat(c.isEnum(), is(true));
    }

    @Test
    public void testClassInfo05() throws Exception {
        String fileName = "target/test-classes/" + CLASS_M + ".class";
        ClassInfo c = new ClassInfo(fileName, new FileInputStream(fileName));

        assertThat(c.getPackageName(), is("soba/testdata/inheritance1"));
        assertThat(c.getClassName(), is(CLASS_M));
        assertThat(c.getSuperClass(), is(CLASS_C));
        assertThat(c.getInterfaces(),
                containsInAnyOrder("soba/testdata/inheritance1/I", "soba/testdata/inheritance1/K"));
        assertThat(c.getHash(), is(notNullValue()));
        assertThat(c.getClassDirPath(), is("soba" + File.separator + "testdata" + File.separator + "inheritance1"));
        assertThat(c.getClassFileName(), is(fileName));
        assertThat(c.getSourceFileName(),
                is("soba" + File.separator + "testdata" + File.separator + "inheritance1" + File.separator + "M.java"));
        assertThat(c.getLabel(), is(nullValue()));
        assertThat(c.isLibrary(), is(false));

        assertThat(c.getMethodCount(), is(15));
        assertThat(c.getMethods(), hasSize(15));
        assertThat(c.getMethod(0), is(notNullValue()));
        assertThat(c.findMethod("m", "()V"), is(notNullValue()));
        assertThat(c.findMethod("n", "()V"), is(notNullValue()));
        assertThat(c.findMethod("x", "(I)V"), is(notNullValue()));
        assertThat(c.findMethod("example", "(IJDLjava/lang/String;)I"), is(notNullValue()));
        assertThat(c.findMethod("toString", "()Ljava/lang/String;"), is(notNullValue()));
        assertThat(c.findMethod("notExist", "()V"), is(nullValue()));
        assertThat(c.findMethod("lambda$new$0", "(Ljava/lang/String;)V"), is(notNullValue()));

        assertThat(c.getFieldCount(), is(1));
        assertThat(c.getFields().get(0), is(notNullValue()));

        assertThat(c.isInterface(), is(false));
        assertThat(c.isEnum(), is(false));

    }

}
