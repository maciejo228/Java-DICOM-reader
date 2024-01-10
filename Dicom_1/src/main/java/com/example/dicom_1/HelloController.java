package com.example.dicom_1;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;

import java.io.*;
import java.util.*;

public class HelloController {
    String directoryPath = "";
    ImageData ID;
    MouseEvent e1,e2,e3;
    int c1Dots,c2Dots,c3Dots,sizeOfPixels;
    @FXML
    Slider s1,s2,s3,sCenter,sWidth;
    @FXML
    Canvas c1,c2,c3;
    @FXML
    Label lCenter,lWidth,l1,l2,l3,s1Value,s2Value,s3Value,dirPath;
    String[] tags = {"(0002,0010)","(0018,0050)","(0028,0010)","(0028,0011)","(0028,0030)","(0028,0100)",
            "(0028,0101)","(0028,0103)","(0028,1050)","(0028,1051)","(0028,1052)","(0028,1053)","(0028,0120)"};
    Map<String,Object> myMap = new HashMap<>();
    //inty: SL, SS, UL, US
    //floaty: FL (pojedyncza precyzja), FD (podwójna precyzja),

    public void prepareData() throws IOException {
        if(directoryPath.equals("")) return;
        addKeysToMap();
        int numberOfFiles = new File(directoryPath).listFiles().length;
        s1.setMin(1);
        s1.setMax(numberOfFiles);
        s2.setMin(0);
        s2.setMax(511);
        s3.setMin(0);
        s3.setMax(511);
        s1Value.setText("1");
        s2Value.setText("1");
        s3Value.setText("1");
        long start = System.currentTimeMillis();

        for (int i=1;i<=numberOfFiles;i++) {
            String path = directoryPath + "\\IM" + i;
            File f = new File(path);
            byte[] bytes_ = new byte[(int) f.length()];

            try (FileInputStream fis = new FileInputStream(f)) {
                fis.read(bytes_);
            }
            if(i == 1) {
                getTagsData(bytes_);
                ID = new ImageData((String)myMap.get("(0002,0010)"),(long)myMap.get("(0028,0010)"),(String)myMap.get("(0028,1053)"),(String)myMap.get("(0028,0030)"),
                        (String)myMap.get("(0028,1052)"), (long)myMap.get("(0028,0100)"),(long)myMap.get("(0028,0011)"),
                        (long)myMap.get("(0028,0101)"),(long)myMap.get("(0028,0103)"), (String)myMap.get("(0028,1051)"), (String)myMap.get("(0018,0050)"),
                        (String)myMap.get("(0028,1050)"),(long)myMap.get("(0028,0120)"));
                ID.showValues();
            }
            getPixelData(bytes_,i);
        }
        long elapsedTimeMillis = System.currentTimeMillis()-start;
        System.out.format("Loaded files in %.3f seconds\n",elapsedTimeMillis/1000F);

        sCenter.setValue(ID.returnWindowCenter());
        sWidth.setValue(ID.returnWindowWidth());
        lCenter.setText(String.valueOf(ID.returnWindowCenter()));
        lWidth.setText(String.valueOf(ID.returnWindowWidth()));

        //draw first image
        c1.getGraphicsContext2D().drawImage(ID.topToBottomImage(1),0,0,512,512);
        c2.getGraphicsContext2D().drawImage(ID.leftToRightImage(numberOfFiles,0),0,0,512,512);
        c3.getGraphicsContext2D().drawImage(ID.backToFrontImage(numberOfFiles,0),0,0,512,512);

        //slider functions to draw image when newVal is set
        s1.valueProperty().addListener(((observableValue, oldVal, newVal) -> {
            c1.getGraphicsContext2D().drawImage(ID.topToBottomImage(newVal.intValue()),0,0,512,512);
            l1.setText("");
            s1Value.setText(String.valueOf(newVal.intValue()));
        }));
        s2.valueProperty().addListener(((observableValue, oldVal, newVal) -> {
            c2.getGraphicsContext2D().drawImage(ID.leftToRightImage(numberOfFiles,newVal.intValue()),0,0,512,512);
            l2.setText("");
            s2Value.setText(String.valueOf(newVal.intValue()));
        }));
        s3.valueProperty().addListener(((observableValue, oldVal, newVal) -> {
            c3.getGraphicsContext2D().drawImage(ID.backToFrontImage(numberOfFiles,newVal.intValue()),0,0,512,512);
            l3.setText("");
            s3Value.setText(String.valueOf(newVal.intValue()));
        }));
        //window center slider
        sCenter.valueProperty().addListener(((observableValue, oldVal, newVal) -> {
            l1.setText("");
            l2.setText("");
            l3.setText("");
            lCenter.setText(String.valueOf(newVal.intValue()));
            ID.setWindowCenter(newVal.intValue());
            int currVal = (int)s1.getValue();
            c1.getGraphicsContext2D().drawImage(ID.topToBottomImage(currVal),0,0,512,512);
            currVal = (int)s2.getValue();
            c2.getGraphicsContext2D().drawImage(ID.leftToRightImage(numberOfFiles,currVal),0,0,512,512);
            currVal = (int)s3.getValue();
            c3.getGraphicsContext2D().drawImage(ID.backToFrontImage(numberOfFiles,currVal),0,0,512,512);
        }));
        //window width slider
        sWidth.valueProperty().addListener(((observableValue, oldVal, newVal) -> {
            l1.setText("");
            l2.setText("");
            l3.setText("");

            lWidth.setText(String.valueOf(newVal.intValue()));
            ID.setWindowWidth(newVal.intValue());
            int currVal = (int)s1.getValue();
            c1.getGraphicsContext2D().drawImage(ID.topToBottomImage(currVal),0,0,512,512);
            currVal = (int)s2.getValue();
            c2.getGraphicsContext2D().drawImage(ID.leftToRightImage(numberOfFiles,currVal),0,0,512,512);
            currVal = (int)s3.getValue();
            c3.getGraphicsContext2D().drawImage(ID.backToFrontImage(numberOfFiles,currVal),0,0,512,512);
        }));
        //draw line on canvas
        c1.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            if(c1Dots == 0) {
                int currVal = (int)s1.getValue();
                c1.getGraphicsContext2D().drawImage(ID.topToBottomImage(currVal),0,0,512,512);
                l1.setText("");
            }
            c1.getGraphicsContext2D().setStroke(Color.ORANGE);
            c1.getGraphicsContext2D().beginPath();
            c1.getGraphicsContext2D().setLineWidth(2);
            c1.getGraphicsContext2D().moveTo(Math.floor(event.getX()), Math.floor(event.getY()));
            c1.getGraphicsContext2D().lineTo(Math.floor(event.getX()), Math.floor(event.getY()));
            c1.getGraphicsContext2D().stroke();

            c1Dots++;
            if(c1Dots == 1) {
                e1 = event;
            } else if(c1Dots == 2) {
                c1Dots = 0;
                c1.getGraphicsContext2D().beginPath();
                c1.getGraphicsContext2D().moveTo(Math.floor(e1.getX()), Math.floor(e1.getY()));
                c1.getGraphicsContext2D().lineTo(Math.floor(event.getX()), Math.floor(event.getY()));
                c1.getGraphicsContext2D().setLineWidth(2);
                c1.getGraphicsContext2D().stroke();
                //calculate distance
                float rowSpacing = ID.returnRowSpacing();
                float colSpacing = ID.returnColumnSpacing();
                double x0 = Math.floor(e1.getX());
                double y0 = Math.floor(e1.getY());
                double x1 = Math.floor(event.getX());
                double y1 = Math.floor(event.getY());
                double val = Math.sqrt( Math.pow( (x0-x1) * rowSpacing ,2) + Math.pow( (y0-y1) * colSpacing ,2) );
                if(val > 10) {
                    val /= 10;
                    l1.setText(String.format("%.2f cm",val));
                } else {
                    l1.setText(String.format("%.2f mm",val));
                }

            }
        });
        c2.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            if(c2Dots == 0) {
                int currVal = (int)s2.getValue();
                c2.getGraphicsContext2D().drawImage(ID.leftToRightImage(numberOfFiles,currVal),0,0,512,512);
                l2.setText("");
            }
            c2.getGraphicsContext2D().setStroke(Color.ORANGE);
            c2.getGraphicsContext2D().beginPath();
            c2.getGraphicsContext2D().setLineWidth(2);
            c2.getGraphicsContext2D().moveTo(Math.floor(event.getX()), Math.floor(event.getY()));
            c2.getGraphicsContext2D().lineTo(Math.floor(event.getX()), Math.floor(event.getY()));
            c2.getGraphicsContext2D().stroke();

            c2Dots++;
            if(c2Dots == 1) {
                e2 = event;
            } else if(c2Dots == 2) {
                c2Dots = 0;
                c2.getGraphicsContext2D().beginPath();
                c2.getGraphicsContext2D().moveTo(Math.floor(e2.getX()), Math.floor(e2.getY()));
                c2.getGraphicsContext2D().lineTo(Math.floor(event.getX()), Math.floor(event.getY()));
                c2.getGraphicsContext2D().setLineWidth(2);
                c2.getGraphicsContext2D().stroke();
                //calculate distance
                float rowSpacing = ID.returnRowSpacing();
                float colSpacing = ID.returnColumnSpacing();
                double x0 = Math.floor(e2.getX());
                double y0 = Math.floor(e2.getY());
                double x1 = Math.floor(event.getX());
                double y1 = Math.floor(event.getY());
                double val = Math.sqrt( Math.pow( (x0-x1) * rowSpacing ,2) + Math.pow( (y0-y1) * colSpacing ,2) );
                if(val > 10) {
                    val /= 10;
                    l2.setText(String.format("%.2f cm",val));
                } else {
                    l2.setText(String.format("%.2f mm",val));
                }
            }
        });
        c3.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            if(c3Dots == 0) {
                int currVal = (int)s3.getValue();
                c3.getGraphicsContext2D().drawImage(ID.backToFrontImage(numberOfFiles,currVal),0,0,512,512);
                l3.setText("");
            }
            c3.getGraphicsContext2D().setStroke(Color.ORANGE);
            c3.getGraphicsContext2D().beginPath();
            c3.getGraphicsContext2D().setLineWidth(2);
            c3.getGraphicsContext2D().moveTo(Math.floor(event.getX()), Math.floor(event.getY()));
            c3.getGraphicsContext2D().lineTo(Math.floor(event.getX()), Math.floor(event.getY()));
            c3.getGraphicsContext2D().stroke();

            c3Dots++;
            if(c3Dots == 1) {
                e3 = event;
            } else if(c3Dots == 2) {
                c3Dots = 0;
                c3.getGraphicsContext2D().beginPath();
                c3.getGraphicsContext2D().moveTo(Math.floor(e3.getX()), Math.floor(e3.getY()));
                c3.getGraphicsContext2D().lineTo(Math.floor(event.getX()), Math.floor(event.getY()));
                c3.getGraphicsContext2D().setLineWidth(2);
                c3.getGraphicsContext2D().stroke();
                //calculate distance
                float rowSpacing = ID.returnRowSpacing();
                float colSpacing = ID.returnColumnSpacing();
                double x0 = Math.floor(e3.getX());
                double y0 = Math.floor(e3.getY());
                double x1 = Math.floor(event.getX());
                double y1 = Math.floor(event.getY());
                double val = Math.sqrt( Math.pow( (x0-x1) * rowSpacing ,2) + Math.pow( (y0-y1) * colSpacing ,2) );
                if(val > 10) {
                    val /= 10;
                    l3.setText(String.format("%.2f cm",val));
                } else {
                    l3.setText(String.format("%.2f mm",val));
                }
            }
        });
        //ID.showArrayValue(1);
    }

    public void getDirectory() {
        final DirectoryChooser directoryChooser =
                new DirectoryChooser();
        final File selectedDirectory =
                directoryChooser.showDialog(null);
        if (selectedDirectory != null) {
            directoryPath = selectedDirectory.getAbsolutePath();
            dirPath.setText(directoryPath);
        }
    }
    void getTagsData(byte[] tab) {
        for (int i = 132; i < tab.length; i++) {
            //System.out.format("I:%d ",i);
            //sklejamy tag
            String tag = String.format("(%02X%02X,%02X%02X)", tab[i+1] & 0xFF,tab[i] & 0xFF,tab[i+3] & 0xFF,tab[i+2] & 0xFF);
            //System.out.format("%s ",tag);
            /*if(tag.equals("(7FE0,0010)")) {
                System.out.format("Size=%d I=%d\n",tab.length,i);
                return;
            }*/
            i += 4;
            String vr = String.format("%s%s", (char) tab[i], (char) tab[i + 1]);
            //System.out.format("%s ",vr);
            int vl;
            if (isExtraBytes(vr) == true) {   //VR ma 4 bity i VL ma 4 bity
                i += 4;
                String hex = String.format("%02X%02X%02X%02X", tab[i + 3] & 0xFF, tab[i + 2] & 0xFF, tab[i + 1] & 0xFF, tab[i] & 0xFF);
                vl = Integer.parseInt(hex, 16);
                i += 4;
            } else {    //VR ma 2 bity i VL ma 2 bity
                i += 2;
                String hex = String.format("%02X%02X", tab[i + 1] & 0xFF, tab[i] & 0xFF);
                vl = Integer.parseInt(hex, 16);
                i += 2;
            }
            //System.out.format("%5s ",vl);

            if(tag.equals("(7FE0,0010)")) {
                sizeOfPixels = vl;
                return;
            }
            String val = "";
            long longIntVal = 0;
            float floatVal = 0;
            if (isValueInt(vr) == true) {
                for (int j = vl + i - 1; j >= i; j--) {
                    val = String.format("%s%02X", val, tab[j] & 0xFF);
                }
                longIntVal = Long.parseLong(val, 16);
                //System.out.format("%5d\n",longIntVal);
                addValueToMap(tag,longIntVal);
            } else if (isValueFloat(vr) == true) {
                for (int j = vl + i - 1; j >= i; j--) {
                    val = String.format("%s%02X", val, tab[j] & 0xFF);
                }
                Long l = Long.parseLong(val, 16);
                floatVal = Float.intBitsToFloat(l.intValue());
                //System.out.format("%5f\n",floatVal);
                addValueToMap(tag,floatVal);
            } else {
                String txt = "";
                for (int j = i; j < (i + vl); j++) {
                    txt += (char)tab[j];
//                    System.out.print((char) tab[j]);
                }
                //System.out.format("%s\n",txt);
                addValueToMap(tag,txt);

            }
            i += (vl-1);
            //System.out.println(" ");
        }
    }

    void getPixelData(byte[] tab,int fileNumber) {
        int pixelsStartHere = tab.length - sizeOfPixels;
        int rows = ID.returnRows();
        int columns = ID.returnColumns();
        int slope = ID.returnRescaleSlope();
        int intercept = ID.returnRescaleIntercept();
        short[][] tmp = new short[rows][columns];
        int currRow = 0;
        int currCol = 0;

        for(int j=pixelsStartHere;j<tab.length;j+=2) {
            byte byte2 = tab[j+1];
            byte byte1 = tab[j];
            int pixelValue = (byte2 << 8) | (byte1 & 0xFF);

            tmp[currRow][currCol] = hounsfieldUnit(pixelValue,slope,intercept);
            currRow++;
            if(currRow == rows) {
                currRow = 0;
                currCol++;
            }
        }
        ID.addArray(tmp,fileNumber);
    }
    short hounsfieldUnit(int val, int slope, int intercept) {
        int v = val * slope + intercept;
        if(v > 1000) v = 1000;
        if(v < -1000) v = -1000;
        return (short)v;
    }
    void addValueToMap(String newTag, Object val) {
        if(myMap.containsKey(newTag)) {
            myMap.put(newTag,val);
        }
    }
    static boolean isExtraBytes(String s) {
        String[] listOfVA = {"OB", "OW", "OF", "SQ", "UT", "UN"};
        for (String a : listOfVA) {
            if (a.equals(s))
                return true;
        }
        return false;
    }
    static boolean isValueInt(String s) {
        String[] listOfVA = {"SL", "SS", "UL", "US"};
        for (String a : listOfVA) {
            if (a.equals(s))
                return true;
        }
        return false;
    }
    static boolean isValueFloat(String s) {
        String[] listOfVA = {"FL", "FD"};
        for (String a : listOfVA) {
            if (a.equals(s))
                return true;
        }
        return false;
    }
    void addKeysToMap() {
        for (String a:tags) {
            myMap.put(a,null);
        }
    }
}