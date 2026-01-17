package dev.kofeychi;

import java.awt.*;

public class Test {
    public static void main(String[] args) throws Exception{
        Thread.sleep(3000);
        System.out.println(MouseInfo.getPointerInfo().getLocation());
    }
}
