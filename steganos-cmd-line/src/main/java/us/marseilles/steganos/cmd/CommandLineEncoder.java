package us.marseilles.steganos.cmd;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import org.apache.commons.io.FilenameUtils;
import us.marseilles.steganos.core.decoder.DiffDecoder;
import us.marseilles.steganos.core.decoder.DiffDecoderImpl;
import us.marseilles.steganos.core.decoder.ReservedPlaceDecoderImpl;
import us.marseilles.steganos.core.encoder.DiffWithPrepEncoder;
import us.marseilles.steganos.core.encoder.DiffWithPrepWithPrepEncoderImpl;
import us.marseilles.steganos.core.encoder.Encoder;
import us.marseilles.steganos.core.encoder.UpDownDiffEncoder;
import us.marseilles.steganos.core.io.ImageReader;
import us.marseilles.steganos.core.io.ImageWriter;
import us.marseilles.steganos.core.io.LocalImageReader;
import us.marseilles.steganos.core.io.LocalImageWriter;
import us.marseilles.steganos.core.decoder.ReservedPlaceDecoder;
import us.marseilles.steganos.core.encoder.ReservedPlaceEncoderImpl;
import us.marseilles.steganos.core.util.Utils;

/**
 * Command line tool for example usage of steganos text-to-image encoder
 */
public class CommandLineEncoder
{
    private static ImageReader imageReader = new LocalImageReader();
    private static ImageWriter imageWriter = new LocalImageWriter();
    private static Encoder reservedPlaceEncoder = new ReservedPlaceEncoderImpl();
    private static ReservedPlaceDecoder reservedPlaceDecoder = new ReservedPlaceDecoderImpl();
    private static DiffWithPrepEncoder diffWithPrepEncoder = new DiffWithPrepWithPrepEncoderImpl();
    private static Encoder upDownDiffEncoder = new UpDownDiffEncoder();
    private static DiffDecoder diffDecoder = new DiffDecoderImpl();

    public static void main(String[] args)
    {
        validateArgs(args);

        Mode mode = Mode.valueOf(args[0].toUpperCase());
        try
        {
            if (mode == Mode.MAX_ENCODABLE_BYTES)
            {
                getMaxEncodableBytes(args[1]);
            }
            else if (mode == Mode.RESERVED_PLACE_ENCODE)
            {
                int conspicuousness = args.length == 4 ? Integer.parseInt(args[3]) : 1;
                saveEncoded(reservedPlaceEncoder, args[1], args[2], conspicuousness);
            }
            else if (mode == Mode.RESERVED_PLACE_DECODE)
            {
                int conspicuousness = args.length == 3 ? Integer.parseInt(args[2]) : 1;
                printReservedPlaceDecoded(args[1], conspicuousness);
            }
            else if (mode == Mode.DIFF_IMG_PREP)
            {
                int conspicuousness = args.length == 3 ? Integer.parseInt(args[2]) : 1;
                savePreppedImage(args[1], conspicuousness);
            }
            else if (mode == Mode.DIFF_WITH_PREP_ENCODE)
            {
                int conspicuousness = args.length == 4 ? Integer.parseInt(args[3]) : 1;
                saveEncoded(diffWithPrepEncoder, args[1], args[2], conspicuousness);
            }
            else if (mode == Mode.UP_DOWN_DIFF_ENCODE)
            {
                int conspicuousness = args.length == 4 ? Integer.parseInt(args[3]) : 1;
                saveEncoded(upDownDiffEncoder, args[1], args[2], conspicuousness);
            }
            else if (mode == Mode.DIFF_DECODE)
            {
                printDiffDecoded(args[1], args[2]);
            }
            else if (mode == Mode.WATERMARK)
            {
                watermark(Arrays.copyOfRange(args, 1, args.length));
            }
        }
        catch (Exception ex)
        {
            System.err.println("Error: " + ex.getMessage());
            System.exit(1);
        }

        System.exit(0);
    }

    private static void getMaxEncodableBytes(String sourceFilePath) throws IOException
    {
        BufferedImage sourceImage = imageReader.read(sourceFilePath);
        System.out.println("Source file: " + sourceFilePath);
        System.out.println("Max encodable bytes: " + Utils.getMaxEncodableBytes(sourceImage));
    }

    private static void saveEncoded(Encoder encoder, String sourceFilePath, String messageOrPath, int conspicuousness)
        throws IOException
    {
        String message = getMessage(messageOrPath);
        System.out.println("Encoding message: " + message);

        BufferedImage sourceImage = imageReader.read(sourceFilePath);
        String outputPath = appendToFileName(sourceFilePath, "-encoded");

        BufferedImage encodedImage = encoder.encode(sourceImage, message, conspicuousness);
        imageWriter.write(encodedImage, outputPath);

        System.out.println("Encoding complete.");
        System.out.println("Encoded file location: " + outputPath);
    }

    private static String getMessage(String messageOrPath) throws IOException
    {
        String message;
        File inputFile = new File(messageOrPath);
        if (inputFile.exists())
        {
            message = Files.readString(inputFile.toPath());
            System.out.println("Message read from file: " + messageOrPath);
        }
        else
        {
            message = messageOrPath;
        }
        return message;
    }

    private static void printReservedPlaceDecoded(String encodedFilePath, int conspicuousness) throws IOException
    {
        BufferedImage encodedImage = imageReader.read(encodedFilePath);
        String decodedMessage = reservedPlaceDecoder.decode(encodedImage, conspicuousness);
        System.out.println("Decoded message:");
        System.out.println(decodedMessage);
    }

    private static void printDiffDecoded(String sourceFilePath, String encodedFilePath) throws IOException
    {
        BufferedImage sourceImage = imageReader.read(sourceFilePath);
        BufferedImage encodedImage = imageReader.read(encodedFilePath);
        String decodedMessage = diffDecoder.decode(sourceImage, encodedImage);
        System.out.println("Decoded message:");
        System.out.println(decodedMessage);
    }

    private static void watermark(String[] args) throws IOException
    {
        BufferedImage sourceImage = imageReader.read(args[1]);
        int maxEncodableBytes = Utils.getMaxEncodableBytes(sourceImage);
        args[2] = Utils.makeLongestEncodableString(maxEncodableBytes, args[2]);
        main(args);
    }

    private static void savePreppedImage(String sourceFilePath, int conspicuousness) throws IOException
    {
        System.out.println("Prepping source file for diff-type encoding");

        BufferedImage sourceImage = imageReader.read(sourceFilePath);
        BufferedImage preppedImage = diffWithPrepEncoder.prepSourceImage(sourceImage, conspicuousness);

        String outputPath = appendToFileName(sourceFilePath, "-prepped");
        imageWriter.write(preppedImage, outputPath);

        System.out.println("Prepping complete.");
        System.out.println("Prepped file location: " + outputPath);
    }

    private static String appendToFileName(String sourceFilePath, String appendText)
    {
        String baseName = FilenameUtils.getBaseName(sourceFilePath);
        String extension = FilenameUtils.getExtension(sourceFilePath);
        String name = FilenameUtils.getName(sourceFilePath);

        return sourceFilePath.replaceAll(name + "$", baseName + appendText + '.' + extension);
    }

    private static void validateArgs(String[] args)
    {
        // Check any args passed
        if (args.length == 0)
        {
            System.err.println("Please pass a mode: " + Arrays.toString(Mode.values()));
            System.exit(1);
        }

        // Check first arg is a valid mode
        Mode mode = null; //init null because compiler doesn't get program terminates at exit(1), warns un-init later on
        try
        {
            mode = Mode.valueOf(args[0].toUpperCase());
        }
        catch (IllegalArgumentException ex)
        {
            System.err.println("Unrecognized mode: " + args[0]);
            System.err.println("Try: " + Arrays.toString(Mode.values()));
            System.exit(1);
        }

        if (mode == Mode.WATERMARK)
        {
            return; // valid mode; remaining args will be validated on 2nd pass
        }

        // Check correct num of args for mode
        if (args.length < mode.minArgs || args.length > mode.maxArgs)
        {
            System.err.println("Wrong number of args. Try: " + mode.sampleUsage);
            System.exit(1);
        }

        // Check img file can be read in from specified location
        try
        {
            imageReader.read(args[1]);
        }
        catch (Exception ex)
        {
            System.err.println("Could not read image file " + args[1]);
            System.err.println(ex.getMessage());
            System.exit(1);
        }

        // DIFF_DECODE is the only mode to take 2 files
        if (mode == Mode.DIFF_DECODE)
        {
            try
            {
                imageReader.read(args[2]);
            }
            catch (Exception ex)
            {
                System.err.println("Could not read encoded image file " + args[2]);
                System.err.println(ex.getMessage());
                System.exit(1);
            }
        }

        // We good.
    }

    private enum Mode
    {
        MAX_ENCODABLE_BYTES(2, 2, "max_encodable_bytes source.png"),
        RESERVED_PLACE_ENCODE(3, 4, "reserved_place_encode source.png \"a message\" [2]"),
        RESERVED_PLACE_DECODE(2, 3, "reserved_place_decode encoded.png [2]"),
        DIFF_IMG_PREP(2, 3, "diff_img_prep source.png [2]"),
        DIFF_WITH_PREP_ENCODE(3, 4, "diff_with_prep_encode source.png \"a message\" [2]"),
        UP_DOWN_DIFF_ENCODE(3, 4, "up_down_diff_encode source.png \"a message\" [2]"),
        DIFF_DECODE(3, 3, "diff_decode source.png encoded.png"),

        /**
         * Pass a piece of text to be repeated across the entire image the maximum number of times. The first arg after
         * "watermark" must be the name of an encode mode.
         */
        WATERMARK(4, 5, "watermark up_down_diff_encode source.png \"repeat this watermark\" [2]");

        private int minArgs;
        private int maxArgs;
        private String sampleUsage;

        Mode(int minArgs, int maxArgs, String sampleUsage)
        {
            this.minArgs = minArgs;
            this.maxArgs = maxArgs;
            this.sampleUsage = sampleUsage;
        }
    }
}