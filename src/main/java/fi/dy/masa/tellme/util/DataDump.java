package fi.dy.masa.tellme.util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.io.FileUtils;

import fi.dy.masa.tellme.TellMe;

public class DataDump
{
    public static File dumpDataToFile(String fileNameBase, ArrayList<String> lines)
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
                TellMe.logger.error("Failed to create the configuration directory.");
                e.printStackTrace();
                return null;
            }

        }

        String fileNameBaseWithDate = fileNameBase + "_" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date(System.currentTimeMillis()));
        String fileName = fileNameBaseWithDate + ".txt";
        outFile = new File(cfgDir, fileName);
        int postFix = 1;

        while (outFile.exists() == true)
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
            TellMe.logger.error("Failed to create data dump file '" + fileName + "'");
            e.printStackTrace();
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
            TellMe.logger.error("Exception while writing data dump to file '" + fileName + "'.");
            e.printStackTrace();
        }

        return outFile;
    }
}
