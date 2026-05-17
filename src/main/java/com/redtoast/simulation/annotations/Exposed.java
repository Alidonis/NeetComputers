package com.redtoast.simulation.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface Exposed {
    String nameOverride() default "";
    boolean mainThread() default false;
}