package edu.kennesaw.core.execution;

import edu.kennesaw.core.converters.Hexadecimal;
import edu.kennesaw.core.processes.ProcessControlBlock;
import edu.kennesaw.core.processes.ProcessQueue;

import java.io.*;

public class Loader {
    //Control card headers
    private final String[] CARD_HEADERS = {"job", "data", "end"};
    //Fields
    private final File _programFile;
    private final ProcessQueue _jobQueue;

    public Loader(String programFilePath, ProcessQueue jobQueue) throws FileNotFoundException{
        _programFile = new File(programFilePath);

        if(!_programFile.exists())
            throw new FileNotFoundException(String.format("File path: %s", programFilePath));

        _jobQueue = jobQueue;
    }

    public void loadJobs() throws FileNotFoundException, IOException{
        BufferedReader reader = new BufferedReader(new FileReader(_programFile));
        int currentAddress = 0;
        String line;

        while((line = reader.readLine()) != null){
            if(isJobControlCard(line)){
                ProcessControlBlock pcb = createProcessControlBlock(parseControlCard(line));
                //Save the beginning address in pcb
                pcb.diskAddress = currentAddress;

                while(!isEnd(line = reader.readLine())){
                    if(isInstruction(line)){

                    }
                }

            }
        }

    }

    private ProcessControlBlock createProcessControlBlock(int[] jobControlCard){
        ProcessControlBlock pcb = new ProcessControlBlock();

        pcb.jobId = jobControlCard[0];
        pcb.textSize = jobControlCard[1];
        pcb.priority = jobControlCard[2];

        return pcb;
    }

    private boolean isInstruction(String line){
        return line.startsWith("0x");
    }
    private boolean isJobControlCard(String line){
        return line.toLowerCase().contains(CARD_HEADERS[0]);
    }
    private boolean isDataControlCard(String line){
        return line.toLowerCase().contains(CARD_HEADERS[1]);
    }
    private boolean isEnd(String line){
        return line.toLowerCase().contains(CARD_HEADERS[2]);
    }
    /* Job control card structure:
     * [0] => job id
     * [1] => text size (words)
     * [2] => priority
     * Data control card structure:
     * [0] => input buffer size (words)
     * [1] => output buffer size (words)
     * [2] => temp buffer size (words)
     */
    private int[] parseControlCard(String line){
        int[] controlCard = new int[3];
        int currentCardIndex = 0;

        for(String component : line.split(" ")){
            if(!component.isEmpty() && !isControlHeader(component))
                controlCard[currentCardIndex++] = Hexadecimal.toInt(component);
        }

        return controlCard;
    }

    private boolean isControlHeader(String text){
        for(String header : CARD_HEADERS)
            if(header.equals(text.toLowerCase()))
                return true;

        return false;
    }

}
