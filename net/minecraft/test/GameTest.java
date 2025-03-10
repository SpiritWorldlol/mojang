package net.minecraft.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GameTest {
   int tickLimit() default 100;

   String batchId() default "defaultBatch";

   int rotation() default 0;

   boolean required() default true;

   String templateName() default "";

   long duration() default 0L;

   int maxAttempts() default 1;

   int requiredSuccesses() default 1;
}
