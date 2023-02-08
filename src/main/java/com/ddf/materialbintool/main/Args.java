package com.ddf.materialbintool.main;

import com.beust.jcommander.Parameter;

import java.util.List;

public class Args {
    @Parameter(names = {"-h", "--help"}, help = true)
    public boolean help = false;

    @Parameter(names = {"-u", "--unpack"})
    public boolean unpack = false;

    @Parameter(names = {"-a", "--add-flags"})
    public boolean addFlagsToCode = false;

    @Parameter(names = {"--raw"})
    public boolean raw;

    @Parameter(names = {"--data-only"})
    public boolean dataOnly = false;

    @Parameter(names = {"-r", "--repack"})
    public boolean repack = false;

    @Parameter(names = {"-c", "--compile"})
    public boolean compile = false;

    @Parameter(names = {"-s", "--shaderc"})
    public String shaderCompilerPath;

    @Parameter(names = {"-i", "--include"})
    public List<String> includePath;

    @Parameter(names = {"--debug"})
    public boolean debug = false;

    @Parameter(names = {"-m", "--merge-data"})
    public boolean mergeData = false;

    @Parameter(names = {"-o", "--output"})
    public String outputPath;

    @Parameter(names = {"-e", "--encrypt"})
    public boolean encrypt = false;

    @Parameter(required = true, description = "files", variableArity = true)
    public List<String> inputPath;
}
