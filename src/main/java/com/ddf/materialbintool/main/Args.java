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

    @Parameter(names = {"--data-only"})
    public boolean dataOnly = false;

    @Parameter(names = {"-r", "--repack"})
    public boolean repack = false;

    @Parameter(names = {"-c", "--compile"})
    public boolean compile = false;

    @Parameter(names = {"-s", "--shaderc"})
    public String shaderCompilerPath;

    @Parameter(names = {"-i", "--include"}, variableArity = true)
    public List<String> includePath;

    @Parameter(names = {"-o", "--output"})
    public String outputPath;

    @Parameter(names = {"-e", "--encrypt"})
    public boolean encrypt = false;

    @Parameter(names = "--diff")
    public boolean diff;

    @Parameter(names = {"--patch"})
    public boolean patch;

    @Parameter(names = "--original")
    public String original;

    @Parameter(required = true, description = "<file>")
    public String inputPath;
}
