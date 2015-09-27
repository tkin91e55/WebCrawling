package com.tkk.webCrawling.utils;

public class Stopwatch { 

    private final long start;

    /**
     * Initializes a new stopwatch.
     */
    public Stopwatch() {
        start = System.currentTimeMillis();
    } 


    /**
     * Returns the elapsed CPU time (in seconds) since the stopwatch was created.
     *
     * @return elapsed CPU time (in seconds) since the stopwatch was created
     */
    public double GetElapsedTime() {
        long now = System.currentTimeMillis();
        return (now - start) / 1000.0;
    }

}
