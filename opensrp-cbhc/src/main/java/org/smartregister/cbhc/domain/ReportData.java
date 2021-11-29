package org.smartregister.cbhc.domain;

public class ReportData {
    int image,color;
    String title;
    String subTitle;

    public ReportData(int image, String title, String subTitle, int color) {
        this.image = image;
        this.title = title;
        this.subTitle = subTitle;
        this.color = color;
    }

    public int getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public int getColor() {
        return color;
    }
}
