package com.example.alias.webapi;

public class ImageCover {
    public int code;
    public String message;
    public int ttl;
    public Data data;
    public static class Data  {

        public String pvdata;
        public String img_x_len;
        public String img_y_len;
        public String img_x_size;
        public String img_y_size;

        public String[] image;
        public int [] index;

    }

}