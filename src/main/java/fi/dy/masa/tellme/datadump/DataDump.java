package fi.dy.masa.tellme.datadump;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import fi.dy.masa.tellme.TellMe;

public class DataDump
{
    public static final String EMPTY_STRING = "";

    protected final int columns;
    protected Alignment[] alignment;
    protected Row title;
    protected List<Row> headers = new ArrayList<Row>();
    protected List<Row> footers = new ArrayList<Row>();
    protected List<Row> lines = new ArrayList<Row>();
    protected int[] widths;
    protected int totalWidth;
    protected String formatStringColumns;
    protected String formatStringSingleCenter;
    protected String formatStringSingleLeft;
    protected String lineSeparator;
    protected boolean useColumnSeparator = false;
    protected boolean centerTitle = false;
    protected boolean repeatTitleAtBottom = true;
    private boolean sort = true;
    private Format format = Format.ASCII;

    protected DataDump(int columns)
    {
        this(columns, Format.ASCII);
    }

    protected DataDump(int columns, Format format)
    {
        this.columns = columns;
        this.format = format;
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

    protected Format getFormat()
    {
        return this.format;
    }

    protected void setFormat(Format format)
    {
        this.format = format;
    }

    protected void setSort(boolean sort)
    {
        this.sort = sort;
    }

    protected void setCenterTitle(boolean center)
    {
        this.centerTitle = center;
    }

    protected void setRepeatTitleAtBottom(boolean repeat)
    {
        this.repeatTitleAtBottom = repeat;
    }

    protected void setUseColumnSeparator(boolean value)
    {
        this.useColumnSeparator = value;
    }

    public void addTitle(String... data)
    {
        this.checkHeaderData(data);
        this.title = new Row(data);
    }

    public void addHeader(String... data)
    {
        this.checkHeaderData(data);
        this.headers.add(new Row(data));
    }

    public void addFooter(String... data)
    {
        this.checkHeaderData(data);
        this.footers.add(new Row(data));
    }

    public void addData(String... data)
    {
        this.checkData(data);
        this.lines.add(new Row(data));
    }

    private void checkHeaderData(String... data)
    {
        if (data.length != 1 || this.columns == 1)
        {
            this.checkData(data);
        }
    }

    private void checkAllHeaders()
    {
        if (this.format == Format.ASCII && this.columns != 1)
        {
            this.checkHeaderLength(this.title);

            int size = this.headers.size();
            for (int i = 0; i < size; i++)
            {
                this.checkHeaderLength(this.headers.get(i));
            }

            size = this.footers.size();
            for (int i = 0; i < size; i++)
            {
                this.checkHeaderLength(this.footers.get(i));
            }
        }
    }

    private void checkHeaderLength(Row row)
    {
        Object[] values = row.getValues();

        if (values.length == 1)
        {
            this.checkHeaderLength(String.valueOf(values[0]));
        }
    }

    private void checkHeaderLength(String header)
    {
        int len = header.length();
        int columns = this.widths.length;
        int space = this.totalWidth + (Math.max(columns - 1, 0) * 3);

        // The title is longer than all the columns and padding character put together,
        // so we will add to the last column's width enough to widen the entire table enough to fit the header.
        if (len > space)
        {
            int diff = len - space;
            this.widths[this.widths.length - 1] += diff;
            this.totalWidth += diff;
        }
    }

    private void checkData(String... data)
    {
        if (data.length != this.columns)
        {
            throw new IllegalArgumentException("Invalid number of columns, you must add exactly " +
                    this.columns + " columns for this type of DataDump");
        }

        if (this.format != Format.ASCII)
        {
            return;
        }

        int total = 0;

        for (int i = 0; i < data.length; i++)
        {
            int len = data[i].length();

            int width = this.widths[i];

            if (len > width)
            {
                this.widths[i] = len;
            }

            total += this.widths[i];
        }

        this.totalWidth = total;
    }

    protected void generateFormatStrings()
    {
        if (this.format == Format.ASCII)
        {
            this.generateFormatStringsASCII();
        }
        else if (this.format == Format.CSV)
        {
            this.generateFormatStringsCSV();
        }
    }

    private String getFormattedLine(Row row)
    {
        if (this.format == Format.ASCII)
        {
            return this.getFormattedLineASCII(row);
        }
        else if (this.format == Format.CSV)
        {
            return this.getFormattedLineCSV(row);
        }

        return EMPTY_STRING;
    }

    protected void generateFormatStringsASCII()
    {
        this.checkAllHeaders();

        String colSep = this.useColumnSeparator ? "|" : " ";
        String lineColSep = this.useColumnSeparator ? "+" : "-";
        StringBuilder sbFmt = new StringBuilder(128);
        StringBuilder sbSep = new StringBuilder(256);
        sbFmt.append(colSep);
        sbSep.append(lineColSep);

        for (int i = 0; i < this.columns; i++)
        {
            int width = this.widths[i];

            if (this.alignment[i] == Alignment.LEFT)
            {
                sbFmt.append(String.format(" %%-%ds %s", width, colSep));
            }
            else
            {
                sbFmt.append(String.format(" %%%ds %s", width, colSep));
            }

            for (int j = 0; j < width + 2; j++)
            {
                sbSep.append("-");
            }

            sbSep.append(lineColSep);
        }

        this.formatStringColumns = sbFmt.toString();
        this.lineSeparator = sbSep.toString();
        this.formatStringSingleCenter = colSep + " %%%ds%%s%%%ds " + colSep;
        this.formatStringSingleLeft = colSep + " %%-%ds " + colSep;
    }

    private String getFormattedLineASCII(Row row)
    {
        Object[] values = row.getValues();

        if (values.length == 1 && this.columns > 1)
        {
            int space = this.totalWidth + (Math.max(this.columns - 1, 0) * 3);
            String fmt = null;
            boolean isCenter = false;

            if (this.centerTitle)
            {
                String str = String.valueOf(values[0]);
                int len = str.length();
                int start = (space - len) / 2;

                if (start > 0)
                {
                    fmt = String.format(this.formatStringSingleCenter, start, space - len - start);
                    isCenter = true;
                }
                else
                {
                    fmt = String.format(this.formatStringSingleLeft, space);
                }
            }
            else
            {
                fmt = String.format(this.formatStringSingleLeft, space);
            }

            return isCenter ? String.format(fmt, " ", values[0], " ") : String.format(fmt, values[0]);
        }

        return String.format(this.formatStringColumns, values);
    }

    protected void generateFormatStringsCSV()
    {
        StringBuilder sbFmtColumn = new StringBuilder(128);
        StringBuilder sbFmtTitle = new StringBuilder(128);
        sbFmtTitle.append("%s");

        for (int i = 0; i < this.columns - 1; i++)
        {
            sbFmtColumn.append("%s, ");
            sbFmtTitle.append(",");
        }

        sbFmtColumn.append("%s");

        this.formatStringColumns = sbFmtColumn.toString();
        this.lineSeparator = EMPTY_STRING;
        this.formatStringSingleCenter = EMPTY_STRING;
        this.formatStringSingleLeft = sbFmtTitle.toString();
    }

    private String getFormattedLineCSV(Row row)
    {
        Object[] values = row.getValues();

        if (values.length == 1 && this.columns > 1)
        {
            return String.format(this.formatStringSingleLeft, values[0]);
        }
        else
        {
            return String.format(this.formatStringColumns, values);
        }
    }

    protected List<String> getFormattedData(List<String> lines)
    {
        if (this.sort)
        {
            Collections.sort(this.lines);
        }

        lines.add(this.lineSeparator);

        int len = this.headers.size();

        if (len > 0)
        {
            for (int i = 0; i < len; i++)
            {
                lines.add(this.getFormattedLine(this.headers.get(i)));
            }

            lines.add(this.lineSeparator);
        }

        lines.add(this.getFormattedLine(this.title));
        lines.add(this.lineSeparator);

        len = this.lines.size();

        for (int i = 0; i < len; i++)
        {
            lines.add(this.getFormattedLine(this.lines.get(i)));
        }

        lines.add(this.lineSeparator);
        len = this.footers.size();

        if (len > 0)
        {
            for (int i = 0; i < len; i++)
            {
                lines.add(this.getFormattedLine(this.footers.get(i)));
            }

            lines.add(this.lineSeparator);
        }

        if (this.repeatTitleAtBottom)
        {
            lines.add(this.getFormattedLine(this.title));
            lines.add(this.lineSeparator);
        }

        return lines;
    }

    protected List<String> getLines()
    {
        List<String> lines = new ArrayList<String>();

        this.generateFormatStrings();
        this.getFormattedData(lines);

        return lines;
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
            BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
            int size = lines.size();

            for (int i = 0; i < size; i++)
            {
                writer.write(lines.get(i));
                writer.newLine();
            }

            writer.close();
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

    public enum Format
    {
        ASCII,
        CSV;
    }
}
