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
    public static void dumpDataToFile(String fileNameBase, ArrayList<String> lines)
    {
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
                return;
            }

        }

        String fileName = fileNameBase + "_" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date(System.currentTimeMillis())) + ".txt";
        File outFile = new File(cfgDir, fileName);

        try
        {
            outFile.createNewFile();
        }
        catch (IOException e)
        {
            TellMe.logger.error("Failed to create data dump file '" + fileName + "'");
            e.printStackTrace();
            return;
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
    }
}
