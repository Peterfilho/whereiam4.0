package com.example.whereiam40;

class ScanSignal {
    private int mediaTsi;
    private int mediaC7;
    private int mediaF5;
    private int mediaD0;
    private int mediaF6;
    private int mediaC8;

    public ScanSignal(int mediaTsi, int mediaC7, int mediaF5, int mediaD0, int mediaF6, int mediaC8) {
        this.mediaTsi = mediaTsi;
        this.mediaC7 = mediaC7;
        this.mediaF5 = mediaF5;
        this.mediaD0 = mediaD0;
        this.mediaF6 = mediaF6;
        this.mediaC8 = mediaC8;
    }


    public int getMediaTsi() {
        return mediaTsi;
    }

    public void setMediaTsi(int mediaTsi) {
        this.mediaTsi = mediaTsi;
    }

    public int getMediaC7() {
        return mediaC7;
    }

    public void setMediaC7(int mediaC7) {
        this.mediaC7 = mediaC7;
    }

    public int getMediaF5() {
        return mediaF5;
    }

    public void setMediaF5(int mediaF5) {
        this.mediaF5 = mediaF5;
    }

    public int getMediaD0() {
        return mediaD0;
    }

    public void setMediaD0(int mediaD0) {
        this.mediaD0 = mediaD0;
    }

    public int getMediaF6() {
        return mediaF6;
    }

    public void setMediaF6(int mediaF6) {
        this.mediaF6 = mediaF6;
    }

    public int getMediaC8() {
        return mediaC8;
    }

    public void setMediaC8(int mediaC8) {
        this.mediaC8 = mediaC8;
    }

    public String toString(){
        if(mediaTsi == 0){
            mediaTsi = 100;
        }
        if(mediaC7 == 0){
            mediaC7 = 100;
        }
        if(mediaF5 == 0){
            mediaF5 = 100;
        }
        if(mediaD0 == 0){
            mediaD0 = 100;
        }
        if(mediaF6 == 0){
            mediaF6 = 100;
        }
        if(mediaC8 == 0){
            mediaC8 = 100;
        }
        return (mediaTsi+","+mediaC7+","+mediaF5+","+mediaD0+","+mediaF6+","+mediaC8);
    }
}