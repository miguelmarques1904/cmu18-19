package pt.ist.cmu.helpers;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class SecurityHelper {

    // encrypt files
    public static File encryptFile(Context context, File file, String password) {
        SecretKey key = null;
        File outputFile = new File(context.getFilesDir().getPath() + "/" + StringGenerator.generateName(25));

        try {
            FileInputStream inputFileStream = new FileInputStream(file);
            FileOutputStream outputFileStream = new FileOutputStream(outputFile);

            // generate key from key on the server
            key = generateKeyFromPassword(password);

            // DES/ECB/PKCS5Padding
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            CipherInputStream cipherInputStream = new CipherInputStream(inputFileStream, cipher);

            byte[] bytes = new byte[64];
            int numBytes;
            while ((numBytes = cipherInputStream.read(bytes)) != -1) {
                outputFileStream.write(bytes, 0, numBytes);
            }

            inputFileStream.close();
            cipherInputStream.close();
            outputFileStream.flush();
            outputFileStream.close();

            file.delete();

        } catch (InvalidKeySpecException | NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | IOException e) {
            e.printStackTrace();
            return file;
        }

        return outputFile;
    }

    // decrypt files
    public static File decryptFile(Context context, File file, String password) {
        SecretKey key = null;
        File outputFile = new File(context.getFilesDir().getPath() + "/" + StringGenerator.generateName(25));

        try {
            FileInputStream inputFileStream = new FileInputStream(file);
            FileOutputStream outputFileStream = new FileOutputStream(outputFile);

            // generate key from key on the server
            key = generateKeyFromPassword(password);

            // DES/ECB/PKCS5Padding
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, key);

            CipherOutputStream cipherOutputStream = new CipherOutputStream(outputFileStream, cipher);

            byte[] bytes = new byte[64];
            int numBytes;
            while ((numBytes = inputFileStream.read(bytes)) != -1) {
                cipherOutputStream.write(bytes, 0, numBytes);
            }

            inputFileStream.close();
            cipherOutputStream.flush();
            cipherOutputStream.close();
            outputFileStream.close();

            file.delete();

        } catch (InvalidKeySpecException | NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | IOException e) {
            e.printStackTrace();
            return file;
        }

        return outputFile;
    }

    // generate SecretKey from password/key
    private static SecretKey generateKeyFromPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        DESKeySpec dks = new DESKeySpec(password.getBytes());
        SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
        return skf.generateSecret(dks);
    }

}
