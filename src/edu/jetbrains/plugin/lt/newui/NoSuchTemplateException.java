package edu.jetbrains.plugin.lt.newui;

import org.jetbrains.annotations.NonNls;

public class NoSuchTemplateException extends RuntimeException {

    public NoSuchTemplateException(@NonNls String message) {
        super(message);
    }
}
