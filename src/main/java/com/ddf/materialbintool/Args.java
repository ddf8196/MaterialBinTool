package com.ddf.materialbintool;

import com.beust.jcommander.Parameter;

public class Args {
    @Parameter(names = {"-a", "--add-flagmodes"})
    public boolean addFlagModesToCode = false;

//    @Parameter(names = {"-o", "--output"})
//    public String outputPath;

    @Parameter(required = true)
    public String inputPath;
}
