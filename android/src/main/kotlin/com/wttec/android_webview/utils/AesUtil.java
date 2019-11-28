package com.wttec.android_webview.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Date:       2019-11-08
 * Describe:   AES
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class AesUtil {
    private static final String ALGORITHM = "AES";
    private static final String AES_MODE = "AES/CBC/NoPadding";
    private static final String HASH_ALGORITHM = "SHA-256";

    AesUtil() {
    }

    public static String encode(String secretStr, String inputStr) throws Exception {
        byte[] secretKeyBytes = generateKey(secretStr);
        byte[] inputBytes = inputStr.getBytes(StandardCharsets.UTF_8);
        byte[] resultBytes = encryptCBCNoPadding(secretKeyBytes, Arrays.copyOfRange(secretKeyBytes, 0, 16), inputBytes);
        return Base64.encodeToString(resultBytes, Base64.NO_WRAP);
    }

    private static byte[] generateKey(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        byte[] bytes = password.getBytes(StandardCharsets.UTF_8);
        digest.update(bytes);
        return digest.digest();
    }

    private static byte[] encryptCBCNoPadding(byte[] secretKeyBytes, byte[] intVectorBytes, byte[] input) throws Exception {
        int inputLength = input.length;
        int srcLength;
        Cipher cipher = Cipher.getInstance(AES_MODE);
        int blockSize = cipher.getBlockSize();
        byte[] srcBytes;
        if (0 != inputLength % blockSize) {
            srcLength = inputLength + (blockSize - inputLength % blockSize);
            srcBytes = new byte[srcLength];
            System.arraycopy(input, 0, srcBytes, 0, inputLength);
        } else {
            srcBytes = input;
        }
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(secretKeyBytes, ALGORITHM), new IvParameterSpec(intVectorBytes));
        return cipher.doFinal(srcBytes);
    }

    public static String decode(String secretStr, String inputStr) throws Exception {
        byte[] secretBytes = generateKey(secretStr.trim());
        byte[] inputBytes = Base64.decode(inputStr.trim(), Base64.NO_WRAP);
        byte[] resultBytes = decryptCBCNoPadding(secretBytes, Arrays.copyOfRange(secretBytes, 0, 16), inputBytes);
        return new String(resultBytes, StandardCharsets.UTF_8).trim();
    }

    private static byte[] decryptCBCNoPadding(byte[] secretKeyBytes, byte[] intVectorBytes, byte[] input) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_MODE);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(secretKeyBytes, ALGORITHM), new IvParameterSpec(intVectorBytes));
        return cipher.doFinal(input);
    }

}
