package com.ddf.materialbintool.main.util;

import com.beust.jcommander.DefaultUsageFormatter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterDescription;
import com.beust.jcommander.WrappedParameter;

import java.util.EnumSet;
import java.util.List;

public class UsageFormatter extends DefaultUsageFormatter {
    public UsageFormatter(JCommander commander) {
        super(commander);
    }

    @Override
    public void appendAllParametersDetails(StringBuilder out, int indentCount, String indent, List<ParameterDescription> sortedParameters) {
        if (sortedParameters.size() > 0) {
            out.append(indent).append("Options:\n");
        }

        // Calculate prefix indent
        int prefixIndent = 0;

        for (ParameterDescription pd : sortedParameters) {
            WrappedParameter parameter = pd.getParameter();
            String prefix = (parameter.required() ? "* " : "  ") + pd.getNames();

            if (prefix.length() > prefixIndent) {
                prefixIndent = prefix.length();
            }
        }

        // Append parameters
        for (ParameterDescription pd : sortedParameters) {
            WrappedParameter parameter = pd.getParameter();

            String prefix = (parameter.required() ? "* " : "  ") + pd.getNames();
            out.append(indent)
                    .append(prefix)
                    .append(s(prefixIndent - prefix.length()))
                    .append(" ");
            final int initialLinePrefixLength = indent.length() + prefixIndent + 3;

            // Generate description
            String description = pd.getDescription();
            Object def = pd.getDefault();

            if (pd.isDynamicParameter()) {
                String syntax = "(syntax: " + parameter.names()[0] + "key" + parameter.getAssignment() + "value)";
                description += (description.length() == 0 ? "" : " ") + syntax;
            }

            Class<?> type = pd.getParameterized().getType();

            if (type.isEnum()) {
                @SuppressWarnings({"unchecked", "rawtypes"})
                String valueList = EnumSet.allOf((Class<? extends Enum>) type).toString();

                // Prevent duplicate values list, since it is set as 'Options: [values]' if the description
                // of an enum field is empty in ParameterDescription#init(..)
                if (!description.contains("Options: " + valueList)) {
                    String possibleValues = "(values: " + valueList + ")";
                    description += (description.length() == 0 ? "" : " ") + possibleValues;
                }
            }

            // Append description
            // The magic value 3 is the number of spaces between the name of the option and its description
            // in DefaultUsageFormatter#appendCommands(..)
            wrapDescription(out, indentCount + prefixIndent - 3, initialLinePrefixLength, description);
            out.append("\n");
        }
    }
}
