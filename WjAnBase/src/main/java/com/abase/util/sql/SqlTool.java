package com.abase.util.sql;

import android.database.Cursor;

import com.abase.annotations.Primary;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * sql操作映射
 *
 * @author Administrator
 * @version 1.0
 * @date 2018/10/26/026
 */
public class SqlTool {

    /**
     * 真正的建表方法
     *
     * @param clazz     实体类
     * @param tableName 表明
     * @return sql建表语句
     */
    public static String createTable(Class clazz) {
        //实例化一个容器，用来拼接sql语句
        StringBuffer sBuffer = new StringBuffer();
        //sql语句，第一个字段为_ID 主键自增，这是通用的，所以直接写死
        sBuffer.append("create table if not exists " + clazz.getSimpleName() );
        sBuffer.append(" (");
        //得到实体类中所有的公有属性
        Field[] fields = clazz.getFields();
        if(fields==null || fields.length<1 ) fields = clazz.getDeclaredFields();
        //遍历所有的公有属性
        for (int i = fields.length - 1; i >= 0; i--) {
            Field field = fields[i];
            //得到属性的基本数据类型
            String type = field.getType().getSimpleName();
            //如果是String类型的属性，就把字段类型设置为TEXT
            if (type.equals("String")) {
                sBuffer.append(field.getName() + " TEXT");
                //如果是int类型的属性，就把字段类型设置为INTEGER
            } else if (type.equals("int")) {
                sBuffer.append(field.getName() + " INTEGER");
            } else if (type.equals("boolean")) {
                sBuffer.append(field.getName() + " BLOB");
            }
            if (field.isAnnotationPresent(Primary.class)) {
                sBuffer.append(" PRIMARY KEY");
            }
            if (i > 0) {
                sBuffer.append(" , ");
            }
        }
        //替换成); 表明sql语句结束
        sBuffer.append(");");
        //返回这条sql语句
        return sBuffer.toString();
    }

    /**
     * sql数据转单个实体类
     *
     * @param module
     * @return
     */
    public static <V> V cursor2Object(Cursor cursor, Class<V> object) {
        if (cursor.isBeforeFirst()) {
            cursor.moveToFirst();
        }
        Field[] arrField = object.getFields();
        try {
            for (Field f : arrField) {
                String columnName = f.getName();
                int columnIdx = cursor.getColumnIndex(columnName);
                if (columnIdx != -1) {
                    if (f.isAccessible()) {
                        f.setAccessible(true);
                    }
                    Class<?> type = f.getType();
                    if (type == byte.class) {
                        f.set(object, (byte) cursor.getShort(columnIdx));
                    } else if (type == short.class) {
                        f.set(object, cursor.getShort(columnIdx));
                    } else if (type == int.class) {
                        f.set(object, cursor.getInt(columnIdx));
                    } else if (type == long.class) {
                        f.set(object, cursor.getLong(columnIdx));
                    } else if (type == String.class) {
                        f.set(object, cursor.getString(columnIdx));
                    } else if (type == byte[].class) {
                        f.set(object, cursor.getBlob(columnIdx));
                    } else if (type == boolean.class) {
                        f.set(object, cursor.getInt(columnIdx) == 1);
                    } else if (type == float.class) {
                        f.set(object, cursor.getFloat(columnIdx));
                    } else if (type == double.class) {
                        f.set(object, cursor.getDouble(columnIdx));
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        cursor.close();
        try {
            return object.newInstance();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * sql数据转多个实体类
     *
     * @param module
     * @return
     */
    public static <V> ArrayList<V> cursor2ObjectList(Cursor cursor, Class<V> object) {
        if (cursor.isBeforeFirst()) {
            cursor.moveToFirst();
        }
        ArrayList<V> list=new ArrayList<V>();
        Field[] arrField = object.getFields();
        cursor.moveToFirst();
        while (cursor.moveToNext()) {
            try {
                for (Field f : arrField) {
                    String columnName = f.getName();
                    int columnIdx = cursor.getColumnIndex(columnName);
                    if (columnIdx != -1) {
                        if (f.isAccessible()) {
                            f.setAccessible(true);
                        }
                        Class<?> type = f.getType();
                        if (type == byte.class) {
                            f.set(object, (byte) cursor.getShort(columnIdx));
                        } else if (type == short.class) {
                            f.set(object, cursor.getShort(columnIdx));
                        } else if (type == int.class) {
                            f.set(object, cursor.getInt(columnIdx));
                        } else if (type == long.class) {
                            f.set(object, cursor.getLong(columnIdx));
                        } else if (type == String.class) {
                            f.set(object, cursor.getString(columnIdx));
                        } else if (type == byte[].class) {
                            f.set(object, cursor.getBlob(columnIdx));
                        } else if (type == boolean.class) {
                            f.set(object, cursor.getInt(columnIdx) == 1);
                        } else if (type == float.class) {
                            f.set(object, cursor.getFloat(columnIdx));
                        } else if (type == double.class) {
                            f.set(object, cursor.getDouble(columnIdx));
                        }
                    }
                }
                list.add(object.newInstance());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        return list;
    }
}
