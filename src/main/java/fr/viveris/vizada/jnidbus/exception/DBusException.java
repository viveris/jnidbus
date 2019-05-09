package fr.viveris.vizada.jnidbus.exception;

import java.util.regex.Pattern;

public class DBusException extends Exception {
    public static final String METHOD_NOT_FOUND_CODE = "org.freedesktop.DBus.Error.UnknownMethod";
    public static final Pattern DBUS_ERROR_CODE_FORMAT = Pattern.compile("^[a-zA-z]+\\.([A-zA-Z]+.)*[a-zA-z]$");

    private String code;
    private String message;

    public DBusException(String code, String message) {
        if(!DBUS_ERROR_CODE_FORMAT.matcher(code).matches()) throw new IllegalArgumentException("The error code does not respect DBus error code format : example.com.error");
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
