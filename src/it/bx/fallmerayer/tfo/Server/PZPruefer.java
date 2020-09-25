package it.bx.fallmerayer.tfo.Server;

import java.util.concurrent.Callable;

public class PZPruefer implements Callable<Boolean> {

    private long zuPruefen;

    public PZPruefer(long zuPruefen) {
        this.zuPruefen = zuPruefen;
    }

    public boolean isPrime(){
        if (zuPruefen <= 1){
            return false;
        }
        for (int i = 2; i < zuPruefen; i++) {
            if (zuPruefen % i == 0) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Boolean call() {
        return isPrime();
    }
}
