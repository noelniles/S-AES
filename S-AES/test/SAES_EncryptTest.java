/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author noel
 */
public class SAES_EncryptTest {
    
    public SAES_EncryptTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void addKeyTest() {
        System.out.println("Testing addKey() from SAES_Encrypt");
        final byte[][] state = {{0xa,4},{7,9}};
        final byte[][] key = {{0x02, 0x0d}, {0x05, 0x05}};       
        final byte[][] expResult = {{8,1},{0xa,0xc}};  
        final byte[][] result = SAES_Encrypt.addKey(state, key);
        assertArrayEquals(expResult, result);
    }
    @Test
    public void substituteNibblesTest(){
        System.out.println("Testing substituteNibbles() from SAES_Encrypt");
        final byte[] nibArr = {0x8, 0x1, 0xa, 0xc};
        final int expResult = 0x64c0;
        final byte result = SAES_Encrypt.substituteNibbles(nibArr);
    }
    @Test
    public void shiftRowsTest(){
        System.out.println("Testing shiftRows() from SAES_Encrypt");
        final byte[][] state = {{6, 4}, {0, 0xc}};
        final byte[][] expResult ={{6, 4}, {0xc, 0}};
        final byte[][] result = SAES_Encrypt.shiftRows(state);
        assertArrayEquals(expResult, result);
        
    }
    @Test
    public void mixColumnsTest(){
        System.out.println("Testing mixColumns from SAES_Encrypt");
        final byte[][] state ={{6, 4}, {0xc, 0}};
        final byte[][] expResult ={{3, 4}, {0x7, 0x3}};
        final byte[][] result = SAES_Encrypt.mixColumns(state);
        assertArrayEquals(expResult, result);
    }
}
