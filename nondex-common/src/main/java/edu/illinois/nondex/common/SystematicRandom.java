/*
The MIT License (MIT)
Copyright (c) 2015 Alex Gyori
Copyright (c) 2015 Owolabi Legunsen
Copyright (c) 2015 Darko Marinov
Copyright (c) 2015 August Shi


Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package edu.illinois.nondex.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.logging.Level;

public class SystematicRandom extends Random {
    private final Stack<ExplorationEntry> choices;
    private final Configuration config;
    private int replayIndex;

    public SystematicRandom(Configuration config) {
        this.config = config;
        this.choices = new Stack<ExplorationEntry>();
        List<String> lines = null;
        File file = new File(config.systematicLog);
        if (file.exists()) {
            try {
                lines = Files.readAllLines(config.getSystematicLogPath());
            } catch (IOException ioe) {
                Logger.getGlobal().log(Level.SEVERE,"Could not read lines from systematic.log" ,ioe);
            }
            String[] choiceValues;
            for (String element: lines) {
                String delimiter = "[ ]+";
                if (!element.isEmpty()) {
                    choiceValues = element.split(delimiter);
                    if (choiceValues.length == 3) {
                        int current = Integer.parseInt(choiceValues[0]);
                        int maximum = Integer.parseInt(choiceValues[1]);
                        boolean shouldExplore = Boolean.parseBoolean(choiceValues[2]);
                        choices.push(new ExplorationEntry(current, maximum, shouldExplore));
                    } else {
                        Logger.getGlobal().log(Level.SEVERE, "The 3 ExplorationEntry variable were not stored properly");
                    }

                }
            }
        }
    }

    public int nextInt(final int maximum) {
        int current;
        boolean explore;
        if (replayIndex < choices.size()) {
            current =  choices.get(replayIndex).getCurrent();
        } else {
            current = 0;
            if (choices.size() > config.start) {
                explore = true;
            } else {
                explore = false;
            }
            ExplorationEntry choiceNums = new ExplorationEntry(current, maximum, explore);
            choices.push(choiceNums);
        }
        replayIndex++;
        return current;
    }

    public void endRun() {
        while (!choices.isEmpty()) {
            ExplorationEntry currentMaximum = choices.pop();
            int current = currentMaximum.getCurrent();
            int maximum = currentMaximum.getMaximum();
            boolean shouldExplore = false;
            if (current < maximum - 1) {
                current++;
                if (choices.size() > config.start) {
                    shouldExplore = true;
                }
                currentMaximum.setCurrent(current);
                currentMaximum.setShouldExplore(shouldExplore);
                choices.push(currentMaximum);
                replayIndex = 0;
                try (BufferedWriter bufferedWriter = Files.newBufferedWriter(config.getSystematicLogPath())) {
                    for (ExplorationEntry element : choices) {
                        String lastAndMax = element.getCurrent() + " " + element.getMaximum()
                            + " " + element.getShouldExplore();
                        bufferedWriter.write(lastAndMax);
                        bufferedWriter.newLine();
                    }
                } catch (IOException ioe) {
                    Logger.getGlobal().log(Level.SEVERE,"Could not write systematic.log file" ,ioe);
                }
                return;
            }

            boolean allFalse = true;
            for (ExplorationEntry ch: choices) {
                if (ch.getShouldExplore()) {
                    allFalse = false;
                }
            }
            if (allFalse) {
                break;
            }
        }

        if (Files.exists(config.getSystematicLogPath())) {
            try {
                Files.delete(config.getSystematicLogPath());
            } catch (IOException ioe) {
                Logger.getGlobal().log(Level.WARNING,"Could not delete systematic.log file" ,ioe);
            }
        }
    }
}
