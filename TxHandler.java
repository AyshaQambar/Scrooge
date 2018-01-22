
import java.util.*;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent
     * transaction outputs) is {@code utxoPool}. This should make a copy of
     * utxoPool by using the UTXOPool(UTXOPool uPool) constructor.
     */
    private UTXOPool TXPool;

    public TxHandler(UTXOPool utxoPool) {
        TXPool = new UTXOPool(utxoPool);
        // IMPLEMENT THIS
    }

    Crypto crypto = new Crypto();

    /**
     * Returns true if (1) all outputs claimed by tx are in the current UTXO
     * pool,
     *
     * (2) the signatures on each input of tx are valid,
     *
     * (3) no UTXO is claimed multiple times by tx,
     *
     * (4) all of tx’s output values are non-negative, and
     *
     * (5) the sum of tx’s input values is greater than or equal to the sum of
     * its output values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        UTXOPool containUTXO = new UTXOPool();

        double TXin = 0;
        double TXout = 0;
        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input in = tx.getInput(i);
            UTXO checkUTXO = new UTXO(in.prevTxHash, in.outputIndex);
            if (containUTXO.contains(checkUTXO)) {
                return false;
            }

            //            containUTXO.add(checkUTXO);
            if (!TXPool.contains(checkUTXO)) {
                return false;
            }

            TXin += TXPool.getTxOutput(checkUTXO).value;

            if (Crypto.verifySignature(TXPool.getTxOutput(checkUTXO).address, tx.getRawDataToSign(i), in.signature)) {
                return true;
            }

        }

        for (Transaction.Output out : tx.getOutputs()) {
            if (out.value < 0) {
                return false;
            }

            TXout += out.value;

            if (TXin >= TXout) {
                return true;
            }

        }
        return false;
    }
    // IMPLEMENT THIS

    /**
     * Handles each epoch by receiving an unordered array of proposed
     * transactions, checking each transaction for correctness, returning a
     * mutually valid array of accepted transactions, and updating the current
     * UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        ArrayList<Transaction> acceptedTX = new ArrayList<Transaction>();
        
        boolean finish = false;
        while (!finish) {
            
            for (int i = 0; i < possibleTxs.length; i++) {
                if (possibleTxs[i] == null) {
                    if (isValidTx(possibleTxs[i])) {
                        //remove
                        for (Transaction.Input in : possibleTxs[i].getInputs()) {
                            UTXO remUTXO = new UTXO(in.prevTxHash, in.outputIndex);
                            TXPool.removeUTXO(remUTXO);
                        }
                        //add
                        for (int j = 0; j < possibleTxs[i].getOutputs().size(); j++) {
                            UTXO newUTXO = new UTXO(possibleTxs[i].getHash(), j);
                            TXPool.addUTXO(newUTXO, possibleTxs[i].getOutputs().get(j));

                        }

                        acceptedTX.add(possibleTxs[i]);
//                        possibleTxs[i] = null;
                        
                        finish = false;
                    }
                }
            }
            
        }
        Transaction[] vArray = new Transaction[acceptedTX.size()];
        acceptedTX.toArray(vArray);
        return vArray;
    }
}
