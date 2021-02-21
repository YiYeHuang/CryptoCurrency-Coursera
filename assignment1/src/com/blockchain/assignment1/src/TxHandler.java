package com.blockchain.assignment1.src;

import java.util.*;

public class TxHandler {

    private final UTXOPool utxoPool;

    /**
     * Creates a public ledger whose current com.blockchain.assignment1.src.UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the com.blockchain.assignment1.src.UTXOPool(com.blockchain.assignment1.src.UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        this.utxoPool = utxoPool;
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no com.blockchain.assignment1.src.UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        List<Transaction.Input> inputs = tx.getInputs();
        List<Transaction.Output> outputs = tx.getOutputs();
        Set<UTXO> spentSet = new HashSet<>();

        // (3) no com.blockchain.assignment1.src.UTXO is claimed multiple times by {@code tx},
        boolean noDoubleSpent = true;
        boolean signatureAllVerified = true;
        boolean containAllOutput = true;
        double inputSum = 0;
        for (int i = 0; i < inputs.size(); i++) {
            UTXO key = new UTXO(inputs.get(i).prevTxHash, inputs.get(i).outputIndex);
            if (spentSet.contains(key)) {
                noDoubleSpent = false;
                break;
            } else {
                spentSet.add(key);
                // (1) all outputs claimed by {@code tx} are in the current UTXO pool
                if (utxoPool.contains(key)) {
                    Transaction.Output output = utxoPool.getTxOutput(key);
                    // (2) the signatures on each input of {@code tx} are valid
                    if (!Crypto.verifySignature(output.address, tx.getRawDataToSign(i), inputs.get(i).signature)) {
                        signatureAllVerified = false;
                        break;
                    }

                    inputSum += output.value;
                } else {
                    containAllOutput = false;
                    break;
                }
            }
        }

        if (noDoubleSpent && signatureAllVerified && containAllOutput) {
            //(5) the sum of {@code tx}s input values is greater than or equal to the sum of its output values; and false otherwise.
            boolean inputValueExceedOutput = inputSum >= outputs.stream().mapToDouble(output -> output.value).sum();

            // (4) all of {@code tx}s output values are non-negative, and
            boolean outputValidate = outputs.stream().allMatch(output -> output.value >= 0);

            return inputValueExceedOutput && outputValidate;
        }

        return false;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current com.blockchain.assignment1.src.UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        List<Transaction> verifiedTx = new ArrayList<>();
        Arrays.stream(possibleTxs).filter(
                this::isValidTx
        ).forEach(


                // input
                validTx -> {
                    // add result
                    verifiedTx.add(validTx);

                    // remove output that product input for the current tx
                    validTx.getInputs().forEach(
                            input -> {
                                int outputIndex = input.outputIndex;
                                byte[] prevTxHash = input.prevTxHash;
                                UTXO utxo = new UTXO(prevTxHash, outputIndex);
                                utxoPool.removeUTXO(utxo);
                            }
                    );

                    // add output in current tx for next tx to validate
                    for (int i = 0; i < validTx.getOutputs().size(); i++) {
                        Transaction.Output output =  validTx.getOutputs().get(i);
                        // to form previous hash
                        UTXO utxo = new UTXO(validTx.getHash(), i);
                        utxoPool.addUTXO(utxo, output);
                    }
                }
        );

        return (Transaction[]) verifiedTx.toArray();
    }

}
