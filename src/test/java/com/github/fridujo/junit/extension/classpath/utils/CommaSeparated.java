package com.github.fridujo.junit.extension.classpath.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.jupiter.params.converter.ConvertWith;

@Retention(RetentionPolicy.RUNTIME)
@ConvertWith(FromCommaSeparatedConverter.class)
public @interface CommaSeparated {
}
