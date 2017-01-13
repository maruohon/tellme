package fi.dy.masa.tellme.datadump;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.FileUtils;
import fi.dy.masa.tellme.TellMe;

public class DataDump
{
    public static final String EMPTY_STRING = "";

    protected final int columns;
    protected Alignment[] alignment;
    protected List<Row> lines = new ArrayList<Row>();
    protected int[] widths;
    protected String formatString;
    protected String lineSeparator;
    protected Row title;
    protected boolean useColumnSeparator = false;
    private boolean sort = true;

    protected DataDump(int columns)
    {
        this.columns = columns;
        this.alignment = new Alignment[this.columns];
        this.widths = new int[this.columns];
        for (int i = 0; i < this.columns; i++) { this.alignment[i] = Alignment.LEFT; }
    }

    protected DataDump setColumnAlignment(int columnId, Alignment align)
    {
        if (columnId >= this.columns)
        {
            throw new IllegalArgumentException("Invalid column id '" + columnId + "', max is " + (this.columns - 1));
        }

        this.alignment[columnId] = align;
        return this;
    }

    protected void setSort(boolean sort)
    {
        this.sort = sort;
    }

    protected void setUseColumnSeparator(boolean value)
    {
        this.useColumnSeparator = value;
    }

    protected void checkData(String... data)
    {
        if (data.length != this.columns)
        {
            throw new IllegalArgumentException("Invalid number of columns, you must add exactly " +
                    this.columns + " columns for this type of DataDump");
        }

        for (int i = 0; i < data.length; i++)
        {
            int len = data[i].length();

            if (len > this.widths[i])
            {
                this.widths[i] = len;
            }
        }
    }

    protected void generateFormatStrings()
    {
        String colSep = this.useColumnSeparator ? "|" : " ";
        String lineColSep = this.useColumnSeparator ? "+" : "-";
        StringBuilder sbFmt = new StringBuilder(128);
        StringBuilder sbSep = new StringBuilder(256);
        sbFmt.append(colSep);
        sbSep.append(lineColSep);

        for (int i = 0; i < this.columns; i++)
        {
            if (this.alignment[i] == Alignment.LEFT)
            {
                sbFmt.append(String.format(" %%-%ds %s", this.widths[i], colSep));
            }
            else
            {
                sbFmt.append(String.format(" %%%ds %s", this.widths[i], colSep));
            }

            for (int j = 0; j < this.widths[i] + 2; j++)
            {
                sbSep.append("-");
            }

            sbSep.append(lineColSep);
        }

        this.formatString = sbFmt.toString();
        this.lineSeparator = sbSep.toString();
    }

    protected String getFormattedLine(Row row)
    {
        return String.format(this.formatString, row.getValues());
    }

    protected List<String> getFormattedData(List<String> lines)
    {
        lines.add(this.lineSeparator);
        lines.add(this.getFormattedLine(this.title));
        lines.add(this.lineSeparator);

        if (this.sort)
        {
            Collections.sort(this.lines);
        }

        int len = this.lines.size();

        for (int i = 0; i < len; i++)
        {
            lines.add(this.getFormattedLine(this.lines.get(i)));
        }

        lines.add(this.lineSeparator);
        lines.add(this.getFormattedLine(this.title));
        lines.add(this.lineSeparator);

        return lines;
    }

    protected List<String> getLines()
    {
        List<String> lines = new ArrayList<String>();

        this.generateFormatStrings();
        this.getFormattedData(lines);

        return lines;
    }

    public void addTitle(String... data)
    {
        this.checkData(data);
        this.title = new Row(data);
    }

    public void addData(String... data)
    {
        this.checkData(data);
        this.lines.add(new Row(data));
    }

    public static File dumpDataToFile(String fileNameBase, List<String> lines)
    {
        File outFile = null;

        File cfgDir = new File(TellMe.configDirPath);

        if (cfgDir.exists() == false)
        {
            try
            {
                cfgDir.mkdirs();
            }
            catch (Exception e)
            {
                TellMe.logger.error("dumpDataToFile(): Failed to create the configuration directory", e);
                return null;
            }

        }

        String fileNameBaseWithDate = fileNameBase + "_" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date(System.currentTimeMillis()));
        String fileName = fileNameBaseWithDate + ".txt";
        outFile = new File(cfgDir, fileName);
        int postFix = 1;

        while (outFile.exists())
        {
            fileName = fileNameBaseWithDate + "_" + postFix + ".txt";
            outFile = new File(cfgDir, fileName);
            postFix++;
        }

        try
        {
            outFile.createNewFile();
        }
        catch (IOException e)
        {
            TellMe.logger.error("dumpDataToFile(): Failed to create data dump file '" + fileName + "'", e);
            return null;
        }

        try
        {
            for (int i = 0; i < lines.size(); ++i)
            {
                FileUtils.writeStringToFile(outFile, lines.get(i) + System.getProperty("line.separator"), true);
            }
        }
        catch (IOException e)
        {
            TellMe.logger.error("dumpDataToFile(): Exception while writing data dump to file '" + fileName + "'", e);
        }

        return outFile;
    }

    public static void printDataToLogger(List<String> lines)
    {
        int size = lines.size();

        for (int i = 0; i < size; i++)
        {
            TellMe.logger.info(lines.get(i));
        }
    }

    public static class Row implements Comparable<Row>
    {
        private String[] strings;

        public Row(String[] strings)
        {
            this.strings = strings;
        }

        public Object[] getValues()
        {
            return this.strings;
        }

        @Override
        public int compareTo(Row other)
        {
            for (int i = 0; i < this.strings.length; i++)
            {
                int res = this.strings[i].compareTo(other.strings[i]);

                if (res != 0)
                {
                    return res;
                }
            }

            return 0;
        }
    }
    public static enum Alignment
    {
        LEFT,
        RIGHT;
    }
}
