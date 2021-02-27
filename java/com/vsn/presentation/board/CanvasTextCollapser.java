package com.vsn.presentation.board;
import android.graphics.Paint;
import android.graphics.Rect;
import java.util.ArrayList;

public abstract class CanvasTextCollapser {

    //Returns the height of a string given some Paint settings
    public static float textHeight(String text, Paint textPaint){
        Rect bounding = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), bounding);
        return bounding.height();
    }

    //Returns the height of a string given some Paint settings
    public static float textWidth(String text, Paint textPaint){
        Rect bounding = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), bounding);
        return bounding.width();
    }

    public static ArrayList<String> stringToConstrainedList(String content,
                                                            Paint textPaint,
                                                            float height,
                                                            float width,
                                                            float textYGap){

        ArrayList<String> contentLines = CanvasTextCollapser.splitStringByWidth(
                content, textPaint, width);
        contentLines = CanvasTextCollapser.trimListByHeight(contentLines,
                textPaint, textYGap, height);
        return contentLines;
    }

    //Divide text into a list:
    //  Each entry should fit into 'width' when drawn with the provided Paint
    private static ArrayList<String> splitStringByWidth(
            String data, Paint paint, float width){
        ArrayList<String> distributedText = new ArrayList<String>();
        String toDistribute = data;
        while (toDistribute.contains("\n\n")) {
            toDistribute = toDistribute.replace("\n\n", "\n  \n");
        }

        while (toDistribute.length() > 0){
            int rightIndex = splitString(toDistribute, paint, width);
            String subStr = toDistribute.substring(0, rightIndex);
            distributedText.add(subStr);
            if (rightIndex >= toDistribute.length()) {
                return distributedText;
            }
            toDistribute = toDistribute.substring(rightIndex);
        }
        return distributedText;
    }

    private static int splitString(
            String data, Paint paint, float wBound){
        int indexWidth = indexOfWidthConstrainedString(data, paint, wBound);
        String substr =  data.substring(0,indexWidth);
        int indexNewLine = getIndexOfNewLine(substr);
        int indexSpace = substr.lastIndexOf(" ");

        if (indexNewLine > 0 && indexNewLine <= indexWidth) {
            return indexNewLine + 1;
        }
        if (indexSpace > 0 && indexSpace < indexWidth) {
            return indexSpace + 1;
        }
        return indexWidth;
    }

    private static int getIndexOfNewLine(String data){
        // newLines \r, \n, \r\n, \n\r, \025
        int index = -1;
        index = data.indexOf("\r\n");
        if (index > 0){ return index + 2;}

        index = data.indexOf("\n\r");
        if (index > 0){ return index + 2;}

        index = data.indexOf("\n");
        if (index > 0){ return index + 0;}

        index = data.indexOf("\r");
        if (index > 0){ return index + 0;}

        index = data.indexOf("\025");
        if (index > 0){ return index + 2;}
        return index;
    }

    // Returns the index of the largest substring that will fit into the width
    private static int indexOfWidthConstrainedString(
            String data, Paint paint, float wBound){
        float width = textWidth(data, paint);
        int index = data.length();
        if (width <= wBound) {return index;}

        while (width > wBound) {
            float divisor = wBound / width;
            index = (int) (data.length() * divisor) - 1;
            data = data.substring(0, index);
            width = textWidth(data, paint);
        }
        return index;
    }

    // Trims a list of text into a list that can be drawn into the given height
    private static ArrayList<String> trimListByHeight(
            ArrayList<String> content, Paint textPaint, float textGap,
            float maxHeight){
        ArrayList <String> trimmedList = new ArrayList<String>();
        float totalHeight = 0;
        int index = 0;

        while (totalHeight < maxHeight && index < content.size()){
            String line = content.get(index);
            //Append 'A' to give empty lines height
            totalHeight += textHeight(line+ "A", textPaint) + textGap;
            trimmedList.add(line);
            index++;
        }
        if (totalHeight > maxHeight) {
            trimmedList.remove(index-1);
            writeElipsesIntoFinalEntry(trimmedList);
        }
        return trimmedList;
    }

    // remove bottom right word of a list of Strings and replace with an ellipse
    private static void writeElipsesIntoFinalEntry(ArrayList<String> list){
        int entry = list.size() - 1;
        String content = list.get(entry);
        int lastSpace = content.lastIndexOf(" ");
        if (lastSpace > 0) {
            content = content.substring(0, lastSpace);
            content += " ...";
            list.remove(entry);
            list.add(content);
        }
    }

}