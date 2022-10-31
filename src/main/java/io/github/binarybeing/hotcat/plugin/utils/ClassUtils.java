package io.github.binarybeing.hotcat.plugin.utils;

import java.lang.reflect.Method;
import java.util.Base64;
import java.util.HashMap;
import java.util.Objects;


/**
 * @author gn.binarybei
 * @date 2022/9/23
 * @note
 */
public class ClassUtils {
    private static final HashMap<String, LoadClass> LOAD_CLASSES = new HashMap<>();

    public static String load(String className, String base64Code){
        LOAD_CLASSES.put(className, new LoadClass(className, base64Code));
        return className;
    }

    public static Object getClassInstance(String name){
        LoadClass aClass = LOAD_CLASSES.get(name);
        if(aClass == null){
            return null;
        }
        String base64Code = aClass.base64Code;
        try {
            Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass",
                    String.class, byte[].class, int.class, int.class);
            defineClass.setAccessible(true);
            byte[] codes = Base64.getDecoder().decode(base64Code);
            LocalAppClassLoader classLoader = new LocalAppClassLoader();
            Class<?> clazz = (Class<?>) defineClass.invoke(classLoader,
                    name, codes, 0, codes.length);
            return clazz.newInstance();
        } catch (Exception e) {
            //LogUtils.addLog("load class error: " + e.getMessage() + " name=" + name + " "+ e.getStackTrace()[0]);
            return null;
        }
    }
    public static class LoadClass{
        private String className;
        private String base64Code;

        public LoadClass(String className, String base64Code) {
            this.className = className;
            this.base64Code = base64Code;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LoadClass loadClass = (LoadClass) o;
            return className.equals(loadClass.className);
        }

        @Override
        public int hashCode() {
            return Objects.hash(className);
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getBase64Code() {
            return base64Code;
        }

        public void setBase64Code(String base64Code) {
            this.base64Code = base64Code;
        }
    }
    private static class LocalAppClassLoader extends ClassLoader {}
}
