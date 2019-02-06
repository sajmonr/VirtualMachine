package edu.kennesaw;

import edu.kennesaw.core.converters.Hexadecimal;
import edu.kennesaw.core.converters.Integral;
import edu.kennesaw.core.execution.OsDriver;
import edu.kennesaw.core.utils.BitUtils;
import edu.kennesaw.core.utils.Config;

public class Main {

    public static void main(String[] args) {
        OsDriver driver;

//vcc test 2
//version control test 1

        try{
            driver = new OsDriver(1, 1024 * Config.WORD_SIZE, 2048 * Config.WORD_SIZE);
            driver.powerOn();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        System.out.println("Program ended.");
    }
}
