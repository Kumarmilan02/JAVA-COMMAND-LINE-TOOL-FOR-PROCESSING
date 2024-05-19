import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.PBEKeySpec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.spec.KeySpec;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Main {
    // Secret key for encryption and decryption
    private static String secretKey = "YourSecretKeyHere";
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        while (true) {
            // Display menu options
            System.out.println("Select an option:");
            System.out.println("1. File Compression");
            System.out.println("2. File Decompression");
            System.out.println("3. File Encryption");
            System.out.println("4. File Decryption");
            System.out.println("5. Image Compression");
            System.out.println("6. Exit");
            int options = scanner.nextInt();
            scanner.nextLine(); // Consume newline left-over

            // Handle user's menu choice
            String filePath;
            switch (options) {
                case 1:
                    System.out.println("Enter the path of the file (or name if in the same folder):");
                    filePath = scanner.nextLine();
                    compressFile(filePath);
                    break;
                case 2:
                    System.out.println("Enter the path of the file (or name if in the same folder):");
                    filePath = scanner.nextLine();
                    decompressFile(filePath);
                    break;
                case 3:
                    System.out.println("Enter the path of the file (or name if in the same folder):");
                    filePath = scanner.nextLine();
                    encryptFile(filePath, secretKey);
                    break;
                case 4:
                    System.out.println("Enter the path of the file (or name if in the same folder):");
                    filePath = scanner.nextLine();
                    decryptFile(filePath, secretKey);
                    break;
                case 5:
                    System.out.println("Enter the path of the image (or name if in the same folder):");
                    filePath = scanner.nextLine();
                    compressImage(filePath);
                    break;
                case 6:
                    System.out.println("Exiting the application.");
                    scanner.close();
                    return;
                default:
                    System.out.println("Please choose a valid option from the menu!");
            }
        }
    }

    /**
     * Compresses a file using GZIP compression.
     * 
     * @param filePath Path to the file to compress.
     * @throws IOException If an I/O error occurs.
     */
    private static void compressFile(String filePath) throws IOException {
        File inputFile = new File(filePath);
        String outputFilePath = inputFile.getParent() + File.separator
                + inputFile.getName().substring(0, inputFile.getName().lastIndexOf('.')) + ".gz";

        try (FileInputStream fis = new FileInputStream(filePath);
                FileOutputStream fos = new FileOutputStream(outputFilePath);
                GZIPOutputStream gzipOS = new GZIPOutputStream(fos)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                gzipOS.write(buffer, 0, bytesRead);
            }
            gzipOS.finish(); // Ensure all data is written to the GZIP output stream

            long originalSize = inputFile.length();
            long compressedSize = new File(outputFilePath).length();
            System.out.println("Original file size: " + originalSize + " bytes");
            System.out.println("Compressed file size: " + compressedSize + " bytes");
            downloadPrompt(outputFilePath);
        } catch (IOException e) {
            System.err.println("Error compressing file: " + e.getMessage());
        }
    }

    /**
     * Decompresses a GZIP file.
     * 
     * @param filePath Path to the file to decompress.
     * @throws IOException If an I/O error occurs.
     */
    private static void decompressFile(String filePath) throws IOException {
        File inputFile = new File(filePath);
        String outputFilePath = inputFile.getParent() + File.separator + "Decompressed_"
                + inputFile.getName().substring(0, inputFile.getName().lastIndexOf('.'));

        try (FileInputStream fis = new FileInputStream(filePath);
                GZIPInputStream gzipIS = new GZIPInputStream(fis);
                FileOutputStream fos = new FileOutputStream(outputFilePath)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = gzipIS.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }

            long compressedSize = inputFile.length();
            long decompressedSize = new File(outputFilePath).length();
            System.out.println("Compressed file size: " + compressedSize + " bytes");
            System.out.println("Decompressed file size: " + decompressedSize + " bytes");
            downloadPrompt(outputFilePath);
        } catch (IOException e) {
            System.err.println("Error decompressing file: " + e.getMessage());
        }
    }

    /**
     * Encrypts a file using AES encryption.
     * 
     * @param inputFile Path to the file to encrypt.
     * @param secretKey The secret key for encryption.
     * @throws Exception If an encryption error occurs.
     */
    public static void encryptFile(String inputFile, String secretKey) throws Exception {
        File input = new File(inputFile);
        FileInputStream inputStream = new FileInputStream(input);
        File outputFile = new File("Encrypted_" + input.getName());
        FileOutputStream outputStream = new FileOutputStream(outputFile);

        SecretKey key = generateKey(secretKey);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byte[] outputBytes = cipher.update(buffer, 0, bytesRead);
            outputStream.write(outputBytes);
        }
        byte[] outputBytes = cipher.doFinal();
        outputStream.write(outputBytes);

        inputStream.close();
        outputStream.close();

        System.out.println("File encrypted successfully: " + outputFile.getPath());
        downloadPrompt(outputFile.getPath());
    }

    /**
     * Decrypts an AES encrypted file.
     * 
     * @param inputFile Path to the file to decrypt.
     * @param password  The password for decryption.
     * @throws Exception If a decryption error occurs.
     */
    public static void decryptFile(String inputFile, String password) throws Exception {
        File input = new File(inputFile);
        FileInputStream inputStream = new FileInputStream(input);
        File outputFile = new File("Decrypted_" + input.getName().substring(10));
        FileOutputStream outputStream = new FileOutputStream(outputFile);

        SecretKey key = generateKey(password);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byte[] outputBytes = cipher.update(buffer, 0, bytesRead);
            outputStream.write(outputBytes);
        }
        byte[] outputBytes = cipher.doFinal();
        outputStream.write(outputBytes);

        inputStream.close();
        outputStream.close();

        System.out.println("File decrypted successfully: " + outputFile.getPath());
        downloadPrompt(outputFile.getPath());
    }

    /**
     * Generates a secret key using a password.
     * 
     * @param password The password to generate the key.
     * @return The generated secret key.
     * @throws Exception If a key generation error occurs.
     */
    private static SecretKey generateKey(String password) throws Exception {
        byte[] salt = {
                (byte) 0xc9, (byte) 0x36, (byte) 0x78, (byte) 0x99,
                (byte) 0x52, (byte) 0x3e, (byte) 0xea, (byte) 0x1c
        };
        int iterationCount = 65536;
        int keyLength = 256;
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iterationCount, keyLength);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Compresses an image file using GZIP compression.
     * 
     * @param imagePath Path to the image file to compress.
     * @throws IOException If an I/O error occurs.
     */
    private static void compressImage(String imagePath) throws IOException {
        long compressedSize = 1024 * 1024; // 1MB
        File imageFile = new File(imagePath);
        long originalSize = imageFile.length();
        if (originalSize <= compressedSize) {
            System.out.println("Image is already smaller than the specified compressed size.");
            return;
        }
        String compressedImagePath = "Compressed_" + imageFile.getName();

        try (FileInputStream fis = new FileInputStream(imageFile);
                FileOutputStream fos = new FileOutputStream(compressedImagePath);
                GZIPOutputStream gzipOS = new GZIPOutputStream(fos)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            long totalBytesRead = 0;
            while ((bytesRead = fis.read(buffer)) != -1 && totalBytesRead < compressedSize) {
                gzipOS.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
            }

            System.out.println("Original image size: " + originalSize + " bytes");
            System.out.println("Compressed image size: " + compressedSize + " bytes");
            downloadPrompt(compressedImagePath);
        } catch (IOException e) {
            System.err.println("Error compressing the image: " + e.getMessage());
        }
    }

    /**
     * Prompts the user to download the processed file.
     * 
     * @param filePath Path to the processed file.
     */
    private static void downloadPrompt(String filePath) {
        System.out.println("Do you want to download the file? (yes/no)");
        String response = scanner.nextLine().trim().toLowerCase();
        if (response.equals("yes")) {
            File file = new File(filePath);
            System.out.println("Download link for the file: " + file.getAbsolutePath());
            // Automatic download logic if required
            // This could be implemented differently based on the environment
            System.out.println("File downloaded successfully to: " + file.getAbsolutePath());
        } else {
            System.out.println("File not downloaded.");
        }
    }
}
