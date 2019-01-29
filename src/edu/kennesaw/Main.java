package edu.kennesaw;

import edu.kennesaw.core.execution.OsDriver;

public class Main {

    public static void main(String[] args) {
        OsDriver driver = new OsDriver(1, 4096, 8192);
        driver.powerOn();
    }
}
