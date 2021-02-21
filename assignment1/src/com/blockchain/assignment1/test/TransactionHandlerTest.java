package com.blockchain.assignment1.test;

import com.blockchain.assignment1.src.Transaction;
import com.blockchain.assignment1.src.TxHandler;
import com.blockchain.assignment1.src.UTXO;
import com.blockchain.assignment1.src.UTXOPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransactionHandlerTest {

    private KeyPair scroogeKeypair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
    private KeyPair aliceKeypair  = KeyPairGenerator.getInstance("RSA").generateKeyPair();
    private KeyPair bobKeypair  = KeyPairGenerator.getInstance("RSA").generateKeyPair();

    private TxHandler handler;
    private UTXOPool pool = new UTXOPool();
    private Transaction genesiseBlock;

    public TransactionHandlerTest() throws NoSuchAlgorithmException {
    }

    @BeforeEach
    protected void setUp() throws Exception {
        GenerateInitialCoins();
    }

    @Test
    public void testTransactionHandler_happy() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
        Transaction tx1 = new Transaction();
        tx1.addInput(genesiseBlock.getHash(), 0);
        tx1.addOutput(10, aliceKeypair.getPublic());
        byte[] sig1 = signMessage(aliceKeypair.getPrivate(), tx1.getRawDataToSign(0));
        tx1.addSignature(sig1, 0);
        tx1.finalize();
        assertFalse(handler.isValidTx(tx1));

        Transaction tx2 = new Transaction();
        tx2.addInput(genesiseBlock.getHash(), 0);
        tx2.addOutput(10, aliceKeypair.getPublic());
        byte[] sig2 = signMessage(scroogeKeypair.getPrivate(), tx2.getRawDataToSign(0));
        tx2.addSignature(sig2, 0);
        tx2.finalize();
        assertTrue(handler.isValidTx(tx2));

        Transaction tx3 = new Transaction();
        tx3.addInput(genesiseBlock.getHash(), 0);
        tx3.addOutput(4, aliceKeypair.getPublic());
        tx3.addOutput(6, bobKeypair.getPublic());
        byte[] sig3 = signMessage(scroogeKeypair.getPrivate(), tx3.getRawDataToSign(0));
        tx3.addSignature(sig3, 0);
        tx3.finalize();
        assertTrue(handler.isValidTx(tx3));
    }

    @Test
    public void testTransactionHandler_negativeValue_fail() throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        Transaction tx = new Transaction();
        tx.addInput(genesiseBlock.getHash(), 0);
        tx.addOutput(4, aliceKeypair.getPublic());
        tx.addOutput(7, bobKeypair.getPublic());
        byte[] sig = signMessage(scroogeKeypair.getPrivate(), tx.getRawDataToSign(0));
        tx.addSignature(sig, 0);
        tx.finalize();
        assertFalse(handler.isValidTx(tx));

        Transaction tx1 = new Transaction();
        tx1.addInput(genesiseBlock.getHash(), 0);
        tx1.addOutput(4, aliceKeypair.getPublic());
        tx1.addOutput(-7, bobKeypair.getPublic());
        byte[] sig1 = signMessage(scroogeKeypair.getPrivate(), tx1.getRawDataToSign(0));
        tx1.addSignature(sig1, 0);
        tx.finalize();
        assertFalse(handler.isValidTx(tx1));
    }

    private void GenerateInitialCoins() {
        genesiseBlock = new Transaction();
        genesiseBlock.addOutput(10, scroogeKeypair.getPublic());
        genesiseBlock.finalize();

        UTXO utxo = new UTXO(genesiseBlock.getHash(), 0);
        pool.addUTXO(utxo, genesiseBlock.getOutput(0));

        handler = new TxHandler(pool);
    }

    private byte[] signMessage(PrivateKey sk, byte[] message)
            throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(sk);
        sig.update(message);
        return sig.sign();
    }
}
