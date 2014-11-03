/** Copyright 2014 Noel Niles
 * 
 * This file is part of SAES
 *
 * S-AES is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
import java.security.SecureRandom;

/** S-AES_Keys
 *
 * Generates an array of keys using the methods outlined in the Simplified AES
 * algorithm.
 * @author Noel Niles
 * @version 1.0
 * @since 2014-10-28
 */
public class SAES_Key {
    public SAES_Key(){
    }
    
    /** Round constants
     * 
     * These can be generated by x^i+2 and 0000 where x is a polynomial and the 
     * value of i depends on the round number. But since there are only 3 rounds
     * it's easier to just make these constants.
     **************************************************************************/
    private static final byte[] rCon = {(byte)0x80,(byte)0x30};
    
    /** S-Box
     * 
     * S-AES lookup table used to swap nibbles in SAES_g function during key 
     * expansion.
     **************************************************************************/
    private static final byte[][] SBox = 
        {{0x09, 0x04, 0x0a, 0x0b},
         {0x0d, 0x01, 0x08, 0x05},
         {0x06, 0x02, 0x00, 0x03},
         {0x0c, 0x0e, 0x0f, 0x07}};

    /** Generates a random 2x2, 16-bit, matrix key Not currently used. 
     * 
     * The 16-bits are arranged in a 2x2 matrix with 4-bits in each cell. Each 
     * cell actually contains 8-bits, but the 4 high bits are set to zero. 
     * 
     * @return key: 2x2 matrix with one nibble in each cell.
     **************************************************************************/
    protected static byte[][] genKey(){
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[1];
        byte key[][] = new byte[2][2];
        for (int col = 0; col < 2; col++) {
            for (int row = 0; row < 2; row++) {
                random.nextBytes(bytes);
                key[row][col] = (byte)((bytes[0] & 0xff) >>> 0x04);
            }
        }       
        return key;
    }

    /** Swaps the left and right nibbles in a word.
     * 
     * Returns an array of two bytes, but only the first 4-bits from each byte 
     * are used the rest should be zero.
     * 
     * @param w: An 8-bit word.
     * @return rotWord: w with a circular shift performed on the nibbles.
     * @example rotNib(0x2d) = 0xd2
     **************************************************************************/
    protected static byte[] rotNib(final byte w) {
        byte lnib = (byte) ((w & 0xf0) >>> 0x04);
        byte rnib = (byte) ((w & 0x0f));
        byte[] rotWord = {rnib, lnib};
        return rotWord;
    }
    
    /** Substitutes nibbles during key expansion.
     * 
     * @param nibArr: array with 2 nibbles
     * @return 
     **************************************************************************/
    protected static byte subNib(final byte[] nibArr) {
        // x, y, xx, yy are indexes into the SBox
        int x, y;
        x = (nibArr[0] >>> 0x02) & 0x03;
        y = nibArr[0] & 0x03;
        byte subNib1 = SBox[x][y];

        int xx, yy;
        xx = (nibArr[1] >>> 0x02) & 0x03;
        yy = nibArr[1] & 0x03;

        byte subNib2 = SBox[xx][yy];
        byte subbedByte = (byte) (((subNib1 & 0x0f) << 0x04) | (subNib2));        
        return subbedByte;
    }
    
    /** Substitutes nibbles using a table look in S-AES S-Box.
     * 
     * @param  nibArr: 2x2 matrix of nibbles
     * @return matrix: a new 2x2 matrix with the nibbles substituted
     **************************************************************************/
    protected static byte[][] subNib(final byte[][] nibArr) {

        byte[][] matrix = new byte[2][2];
        byte[] w1 = {nibArr[0][0], nibArr[1][0]};
        byte[] w2 = {nibArr[0][1], nibArr[1][1]};
        matrix[0][0] = (byte)((subNib(w1) >>> 4) & 0xf);
        matrix[1][0] = (byte)(subNib(w1) & 0xf);
        matrix[0][1] = (byte)((subNib(w2) >>> 4) & 0xf);
        matrix[1][1] = (byte)(subNib(w2) & 0xf);      
        return matrix;
    }
    
    /** SAES_g function.
     * 
     * Used during key expansion.
     * @param w: 8-bit word.
     * @param i: selects the round constant to use. Index into rCon[].
     * @return word: 8-bit word with nibbles rotated and SBox transform.
     * 
     * @TODO Build tests
     **************************************************************************/
    protected static byte SAES_g(final byte w, final int i) {
        byte[] nibArr = rotNib(w);
        byte subbedNibs = subNib(nibArr);
        return (byte) (subbedNibs ^ rCon[i]);
    }
     
    /** S-AES Key Expansion
     * 
     * Expands the 16-bit key into an array of shorts. The 3 shorts in the 
     * array are the 3 16-bit keys.
     * @param cipherKey: a 2x2 matrix generated by genKey()
     * @return key: array of 3 16-bit keys
     * 
     * @TODO Build tests. Should this return an array of 2x2 matrices?   
     **************************************************************************/
    protected static short[] keyExpansion(final byte[][] cipherKey){
        // Array of 16-bit keys
        short key[] = new short[3];
    
        // w0-w5 are 8-bit words. Each word is combined into a 16-bit key
        byte w0 = (byte)((cipherKey[0][0] << 0x04) | cipherKey[0][1]);
        byte w1 = (byte)((cipherKey[1][0] << 0x04) | cipherKey[1][1]);
        byte w2 = (byte)(w0 ^ SAES_g(w1, 0));
        byte w3 = (byte)(w1 ^ w2);
        byte w4 = (byte)(w2 ^ SAES_g(w3, 1));
        byte w5 = (byte)(w3 ^ w4);
        
        // key[0-2] are the 16-bit keys.
        key[0] = (short) ((w0 << 0x08) | (w1 & 0xff));
        key[1] = (short) ((w2 << 0x08) | (w3 & 0xff));
        key[2] = (short) ((w4 << 0x08) | (w5 & 0xff));       
        return key;
    }
}