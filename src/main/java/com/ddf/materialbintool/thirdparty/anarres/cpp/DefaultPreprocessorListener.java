package com.ddf.materialbintool.thirdparty.anarres.cpp;

/*
 * Anarres C Preprocessor
 * Copyright (c) 2007-2015, Shevek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A handler for preprocessor events, primarily errors and warnings.
 *
 * If no PreprocessorListener is installed in a Preprocessor, all
 * error and warning events will throw an exception. Installing a
 * listener allows more intelligent handling of these events.
 */
public class DefaultPreprocessorListener implements PreprocessorListener {

    private int errors;
    private int warnings;

    public DefaultPreprocessorListener() {
        clear();
    }

    public void clear() {
        errors = 0;
        warnings = 0;
    }

    @Nonnegative
    public int getErrors() {
        return errors;
    }

    @Nonnegative
    public int getWarnings() {
        return warnings;
    }

    protected void print(@Nonnull String msg) {
        System.out.println(msg);
    }

    /**
     * Handles a warning.
     *
     * The behaviour of this method is defined by the
     * implementation. It may simply record the error message, or
     * it may throw an exception.
     */
    @Override
    public void handleWarning(Source source, int line, int column,
            String msg)
            throws LexerException {
        warnings++;
        print(source.getName() + ":" + line + ":" + column
                + ": warning: " + msg);
    }

    /**
     * Handles an error.
     *
     * The behaviour of this method is defined by the
     * implementation. It may simply record the error message, or
     * it may throw an exception.
     */
    @Override
    public void handleError(Source source, int line, int column,
            String msg)
            throws LexerException {
        errors++;
        print(source.getName() + ":" + line + ":" + column
                + ": error: " + msg);
    }

    @Override
    public void handleSourceChange(Source source, SourceChangeEvent event) {
    }

}
