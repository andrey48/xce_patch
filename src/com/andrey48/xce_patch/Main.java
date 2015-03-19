package com.andrey48.xce_patch;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class Main {
    public static final String XENONAUTS = "Xenonauts";
    public static final String SOLDIER_FLAGS = "SoldierFlags";
    public static final String[] ITEMS = {"Alenium", "Alienalloys"};

    public static void main(String[] args) {
        try {
            File saveFile = new File(args[0]);
            int saveSize = (int)saveFile.length();
            System.out.print("Save file size: " + saveSize + "\n");
            byte[] buf = new byte[saveSize];
            InputStream input = new FileInputStream(saveFile);
            int res = input.read(buf, 0, saveSize);
            if (res <= 0) {
                System.err.print("ERROR: Read file\n");
                System.exit(-1);
            }
            input.close();
            System.out.print("Bytes read: " + res + "\n");


            int pos = search(buf, XENONAUTS.getBytes(), 0);
            if (pos > 0) {
                System.out.print("MONEY: " + ByteBuffer.wrap(buf, pos + XENONAUTS.length(), 4).order(ByteOrder.LITTLE_ENDIAN).getInt() + "\n");
            }

            System.out.print("SOLDIERS:\n");
            pos = 0;
            while ((pos = search(buf, SOLDIER_FLAGS.getBytes(), pos + 1)) > 0) {
                System.out.print(String.format("%1$7d: ", pos));
                int skip = ByteBuffer.wrap(buf, pos + SOLDIER_FLAGS.length() + 1, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
                String country = new String(buf ,pos + SOLDIER_FLAGS.length() + 1 + 4, skip);
                System.out.print(String.format("%1$16s: ", country));
                IntBuffer props = ByteBuffer.wrap(buf, pos + SOLDIER_FLAGS.length() + 1 + 4 + skip, 12 * 4).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
                for (int i = 0; i < 12; i++) {
                    System.out.print(String.format("%1$3d ", props.get(i)));
                    if (props.get(i) < 96) props.put(96);
                }
                System.out.print("\n");
            }

            System.out.print("ITEMS:\n");
            for (String item:ITEMS) {
                pos = 0;
                while ((pos = search(buf, "Items.".concat(item).getBytes(), pos + 1)) > 0) {
                    System.out.print(String.format("%1$7d: ", pos));
                    int value = ByteBuffer.wrap(buf, pos + "Items.".concat(item).length(), 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
                    System.out.print(String.format("%1$s: %2$6d\n", item, value));
                }
            }

            File newFile = new File(args[0] + ".new");
            if (!newFile.createNewFile()) {
                System.err.print("ERROR: File already exists!\n");
                System.exit(-1);
            }
            OutputStream output = new FileOutputStream(newFile);
            output.write(buf, 0, saveSize);
            output.close();
        } catch (Exception e) {
            System.err.print(e.toString() + "\n");
        }
    }
    public static int search(byte[] data, byte[] pattern, int start) {
        int i, j;
        boolean diff = false;
        if ((data.length-start) < pattern.length) return -1;
        for (i = start; i < (data.length - pattern.length); i++) {
            diff = false;
            for (j = 0; j < pattern.length; j++) {
                if (data[i+j] != pattern[j]) {
                    diff = true;
                    break;
                }
            }
            if (diff) continue;
            else return i;
        }
        return -1;
    }
}
