package io.github.binarybeing.hotcat.plugin.scheme;

import io.github.binarybeing.hotcat.plugin.BaseTest;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

public class SchemeTest extends BaseTest {
    @Override
    public Object doExecute() throws Exception {

        return null;
    }

    @Override
    public void verify(int code, String msg, String data) throws Exception {
        assert code == 200;
    }

    @Override
    public long until() throws Exception {
        String expireAt = "2023-10-01";
        return new SimpleDateFormat("yyyy-MM-dd")
                .parse(expireAt).getTime();
    }

    private static enum TypesEnum {
        //primitive
        BOOLEAN_PRIMITIVE(boolean.class, 1, false),
        BYTE_PRIMITIVE(byte.class, 2, 0),
        CHAR_PRIMITIVE(char.class, 3, 0),
        INT_PRIMITIVE(int.class, 4, 0),
        LONG_PRIMITIVE(long.class, 5, 0),
        DOUBLE_PRIMITIVE(double.class, 6, 0.0D),
        FLOAT_PRIMITIVE(float.class, 7, 0.0F),

        //wrapper
        BOOLEAN(Boolean.class, 11, null),
        BYTE(Byte.class, 12, null),
        CHAR(Character.class, 13, null),
        INT(Integer.class, 14, null),
        LONG(Long.class, 15, null),
        DOUBLE(Double.class, 16, null),
        FLOAT(Float.class, 17, null),
        STRING(String.class, 18, null),

        //array
        BOOLEAN_ARRAY(boolean[].class, 21, null),
        BYTE_ARRAY(byte[].class, 22, null),
        CHAR_ARRAY(char[].class, 23, null),
        INT_ARRAY(int[].class, 24, null),
        LONG_ARRAY(long[].class, 25, null),
        DOUBLE_ARRAY(double[].class, 26, null),
        FLOAT_ARRAY(float[].class, 27, null),

        BOOLEAN_PRIMITIVE_ARRAY(Boolean[].class, 31, null),
        BYTE_PRIMITIVE_ARRAY(Byte[].class, 32, null),
        CHAR_PRIMITIVE_ARRAY(Character[].class, 33, null),
        INT_PRIMITIVE_ARRAY(Integer[].class, 34, null),
        LONG_PRIMITIVE_ARRAY(Long[].class, 35, null),
        DOUBLE_PRIMITIVE_ARRAY(Double[].class, 36, null),
        FLOAT_PRIMITIVE_ARRAY(Float[].class, 37, null),

        //Collection
        LIST(List.class, 41, null),
        SET(Set.class, 42, null),



//        STRING(String.class, 11, null),
//        INTEGER(Integer.class, 2, null),
        ;

        TypesEnum(Class<?> clazz, int type, Object defaultValue) {
            this.clazz = clazz;
            this.type = type;
            this.defaultValue = defaultValue;
        }

        private Class<?> clazz;
        private int type;
        private Object defaultValue;
    }

}
