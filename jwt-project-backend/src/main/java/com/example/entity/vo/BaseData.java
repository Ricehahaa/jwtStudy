package com.example.entity.vo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.function.Consumer;

//用于将DTO数据快速转成VO
//哪个DTO想用，就implement一下这个接口就行了(内部已经有实现了)
public interface BaseData {

    //可能VO还有些参数是DTO没的，可以写到consumer里处理
    default <V> V asViewObject(Class<V> clazz, Consumer<V> consumer){
        V v = this.asViewObject(clazz);
        consumer.accept(v);
        return v;
    }
    //参数就是你想转换成什么类型的VO
    default <V> V asViewObject(Class<V> clazz){
        try{
            Field[] declaredFields = clazz.getDeclaredFields();
            Constructor<V> constructor = clazz.getConstructor();
            V v = constructor.newInstance();
            for (Field declaredField : declaredFields) {
                convert(declaredField, v);
            }
            return v;
        } catch (ReflectiveOperationException e){
            throw new RuntimeException(e.getMessage());
        }
    }

    private void convert(Field field, Object vo){
        try{
            Field source = this.getClass().getDeclaredField(field.getName());
            field.setAccessible(true);
            source.setAccessible(true);
            field.set(vo,source.get(this));
        }catch (IllegalAccessException | NoSuchFieldException ignored) {

        }
    }
}
