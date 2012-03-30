package org.crsh.jcr.command;

import org.crsh.cmdline.annotations.Argument;
import org.crsh.cmdline.annotations.Usage;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Usage("a list of properties")
@Argument(name = "properties")
public @interface PropertiesArg {
}
