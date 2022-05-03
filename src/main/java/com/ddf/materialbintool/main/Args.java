package com.ddf.materialbintool.main;

import com.beust.jcommander.Parameter;

import java.util.List;

public class Args {
    @Parameter(names = {"-h", "--help"}, help = true)
    public boolean help = false;

    @Parameter(names = {"-u", "--unpack"})
    public boolean unpack = false;

    @Parameter(names = {"-a", "--add-flagmodes"})
    public boolean addFlagModesToCode = false;

    @Parameter(names = {"--raw"})
    public boolean raw;

    @Parameter(names = {"-r", "--repack"})
    public boolean repack = false;

    @Parameter(names = {"-c", "--compile"})
    public boolean compile = false;

    @Parameter(names = {"-s", "--shaderc"})
    public String shaderCompilerPath;

    @Parameter(names = {"-i", "--include"})
    public String includePath;

    @Parameter(names = {"-o", "--output"})
    public String outputPath;

    @Parameter(required = true, description = "<file>")
    public String inputPath;
}
