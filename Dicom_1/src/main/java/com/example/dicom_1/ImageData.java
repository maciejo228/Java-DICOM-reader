package com.example.dicom_1;

import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public class ImageData {

    String Endian;           //(0002,0010)
    float sliceThickness;    //(0018,0050)
    long rows;               //(0028,0010)
    long columns;            //(0028,0011)
    float rowSpacing;        //(0028,0030)
    float columnSpacing;     //(0028,0030)
    long bitsAllocated;      //(0028,0100)
    long bitsStored;         //(0028,0101)
    long pixelRepresentation;//(0028,0103)
    int windowCenter;        //(0028,1050)
    int windowWidth;         //(0028,1051)
    int rescaleIntercept;    //(0028,1052)
    int rescaleSlope;        //(0028,1053)
    long pixelPaddingValue;  //(0028,0120)
    Map<Integer,Object> mapOfPixelData = new HashMap<>();//(7FE0,0010)

    ImageData(String e,long r, String rescaleS, String spacing, String rescaleI, long bitsA, long c, long bitsS, long pixelR, String windowW, String sliceT ,String windowC, long ppV) {
        if(e.contains("1.2.840.10008.1.2.1.99")){
            Endian = "Deflated Explicit VR Little Endian";
        } else if(e.contains("1.2.840.10008.1.2.2")) {
            Endian = "Explicit VR Big Endian";
        } else if(e.contains("1.2.840.10008.1.2.1")) {
            Endian = "Explicit VR Little Endian";
        } else if(e.contains("1.2.840.10008.1.2")) {
            Endian = "Implicit VR Little Endian";
        }

        rows = r;
        columns = c;
        bitsAllocated = bitsA;
        bitsStored = bitsS;
        rescaleSlope = Integer.parseInt(rescaleS.replace(" ",""));
        rescaleIntercept = Integer.parseInt(rescaleI.replace(" ",""));
        pixelRepresentation = pixelR;
        windowCenter = Integer.parseInt(windowC.replace(" ",""));
        windowWidth = Integer.parseInt(windowW.replace(" ",""));
        sliceThickness = Float.parseFloat(sliceT.replace(" ",""));
        String[] tmp = spacing.replace(" ","").split("\\\\");
        rowSpacing = Float.parseFloat(tmp[0]);
        columnSpacing = Float.parseFloat(tmp[1]);
        pixelPaddingValue = ppV;
    }
    void showValues() {
        System.out.format("Endian: %s\nRows: %d\nColumns: %d\nBits allocated: %d\nBits stored: %d\nRescale slope: %d\nRescale intercept: %d\n" +
                "Pixel representation: %d\nWindow center: %d\nWindow width: %d\nSlice thickness: %f\n" +
                "Row spacing: %f\nColumn spacing: %f\nPixel padding value: %d\n",Endian,rows,columns,bitsAllocated,
                bitsStored,rescaleSlope, rescaleIntercept,pixelRepresentation,windowCenter,windowWidth,sliceThickness,rowSpacing,
                columnSpacing,pixelPaddingValue);
    }
    int returnRescaleSlope(){
        return rescaleSlope;
    }

    int returnRescaleIntercept(){
        return rescaleIntercept;
    }

    float returnRowSpacing() {
        return rowSpacing;
    }

    float returnColumnSpacing() {
        return columnSpacing;
    }
    int returnRows() {
        return (int)rows;
    }

    int returnColumns() {
        return (int)columns;
    }

    int returnWindowCenter() {
        return windowCenter;
    }
    int returnWindowWidth() {
        return windowWidth;
    }
    void setWindowCenter(int newVal) {
        windowCenter = newVal;
    }
    void setWindowWidth(int newVal) {
        windowWidth = newVal;
    }
    void addArray(short[][] a,int numberOfFIle) {
        mapOfPixelData.put(numberOfFIle,a);
    }

    void showArrayValue(int fileNumber) {
        short[][] tmp = (short[][])mapOfPixelData.get(fileNumber);
        for(int i=0;i<rows;i++) {
            for(int j=0;j<columns;j++) {
                System.out.format("%5d ",tmp[i][j]);
            }
            System.out.println("");
        }
    }
    WritableImage topToBottomImage(int fileNumber) {
        short[][] tmp = (short[][])mapOfPixelData.get(fileNumber);
        WritableImage image = new WritableImage((int)rows, (int)columns);
        PixelWriter pixelWriter = image.getPixelWriter();
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < columns; y++) {
                int v = hounsfieldUnitToColor(tmp[x][y]);
                pixelWriter.setColor(x, y, Color.rgb(v,v,v));
                //Color color = hounsfieldUnitToColor(tmp[x][y]);
                //pixelWriter.setColor(x, y, color);
            }
        }
        return image;
    }
    /*Color*/int hounsfieldUnitToColor(short val) {
        double minHU = windowCenter - 0.5 - (windowWidth - 1) / 2;
        double maxHU = windowCenter - 0.5 + (windowWidth - 1) / 2;
        int v;
        if (val <= minHU)
            v = 0;
            //return Color.rgb(0,0,0);
        else if (val > maxHU)
            v = 255;
            //return Color.rgb(255,255,255);
        else {
            v = (int)(((val - (windowCenter - 0.5)) / (windowWidth - 1) + 0.5) * 255);
            //return Color.rgb(v,v,v);
        }
        return v;
    }

    WritableImage leftToRightImage(int numberOfFiles,int currRow) {

        short[][] tmpArray = new short[(int)columns][numberOfFiles];          //do interpolacji
        WritableImage image = new WritableImage((int)columns, (int)rows);     //do interpolacji

//        WritableImage image = new WritableImage((int)columns, numberOfFiles);

        PixelWriter pixelWriter = image.getPixelWriter();
        for (Map.Entry<Integer, Object> entry: mapOfPixelData.entrySet()) {
            short[][] array = (short[][])entry.getValue();
            int fileNumber = entry.getKey()-1;
            for (int y = 0; y < columns; y++) {

//                Color color = hounsfieldUnitToColor(array[currRow][y]);
//                pixelWriter.setColor(y, numberOfFiles - fileNumber - 1, color);

                tmpArray[y][numberOfFiles - fileNumber - 1] = array[currRow][y];  //do interpolacji

//                tmpArray[y][numberOfFiles - fileNumber - 1] = (short)hounsfieldUnitToColor(array[currRow][y]);
            }
        }

        short[][] newArray = bilinearInterpolation((int)columns,numberOfFiles,512,512,tmpArray);    //do interpolacji
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                int v = newArray[i][j];
//                pixelWriter.setColor(i, j, Color.rgb(v,v,v));
                int c = hounsfieldUnitToColor(newArray[i][j]);
                pixelWriter.setColor(i, j, Color.rgb(c,c,c));
            }
        }

        return image;
    }
    WritableImage backToFrontImage(int numberOfFiles,int currColumn) {

        short[][] tmpArray = new short[(int)columns][numberOfFiles];          //do interpolacji
        WritableImage image = new WritableImage((int)columns, (int)rows);     //do interpolacji

        //WritableImage image = new WritableImage((int)columns, numberOfFiles);

        PixelWriter pixelWriter = image.getPixelWriter();
        for (Map.Entry<Integer, Object> entry: mapOfPixelData.entrySet()) {
            short[][] array = (short[][])entry.getValue();
            int fileNumber = entry.getKey()-1;
            for (int x = 0; x < rows; x++) {
                //Color color = hounsfieldUnitToColor(array[x][currColumn]);
                //pixelWriter.setColor(x, numberOfFiles - fileNumber - 1, color);

                tmpArray[x][numberOfFiles - fileNumber - 1] = array[x][currColumn];  //do interpolacji

//                tmpArray[x][numberOfFiles - fileNumber - 1] = (short)hounsfieldUnitToColor(array[x][currColumn]);
            }
        }

        short[][] newArray = bilinearInterpolation((int)columns,numberOfFiles,512,512,tmpArray);    //do interpolacji
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                int v = newArray[i][j];
//                pixelWriter.setColor(i, j, Color.rgb(v,v,v));
                int c = hounsfieldUnitToColor(newArray[i][j]);
                pixelWriter.setColor(i, j, Color.rgb(c,c,c));
            }
        }

        return image;
    }

    short[][] bilinearInterpolation(int srcW, int srcH, int dstW, int dstH,short[][] srcArray) {//interpolacja dwuliniowa
        short[][] dstArray = new short[dstW][dstH];
        float ratioX = (float)srcW / (float)dstW;
        float ratioY = (float)srcH / (float)dstH;
        for(int i=0;i<dstW;i++) {
            for(int j=0;j<dstH;j++) {
                double x = i * ratioX;
                double y = j * ratioY;
                int x0 = (int) Math.floor(x);
                int x1 = (int) Math.min(Math.ceil(x),srcW-1);
                int y0 = (int) Math.floor(y);
                int y1 = (int) Math.min(Math.ceil(y),srcH-1);

                double a = x - (double)x0;
                double b = y - (double)y0;
                double color;

                if(x1 == x0 && y1 == y0) {
                    color = srcArray[(int)x][(int)y];
                } else if (x1 == x0) {
                    color = (srcArray[(int)x][y0] * ((double)y1 - y) ) + (srcArray[(int)x][y1] * b);
                }else if(y1 == y0) {
                    color = (srcArray[x0][(int)y] * ((double)x1 - x) ) + (srcArray[x1][(int)y] * a);
                } else {
                    double color0 = ((double)x1 - x) * (double)srcArray[x0][y0] + a * (double)srcArray[x1][y0];
                    double color1 = ((double)x1 - x) * (double)srcArray[x0][y1] + a * (double)srcArray[x1][y1];
                    color = ((double)y1 - y) * color0 + b * color1;
                }
                dstArray[i][j] = (short)color;
            }
        }
        return dstArray;
    }
}
