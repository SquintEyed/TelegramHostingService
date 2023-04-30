package org.example.enums;

public enum LinkType {

    GET_DOC("file/get-doc/"),
    GET_PHOTO("file/get-photo/");

    private String uri;

    private LinkType(String uri) {
        this.uri = uri;
    }

    @Override
    public String toString() {
        return this.uri;
    }
}
