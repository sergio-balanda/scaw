package ar.edu.unlam.scaw.entities;

import java.security.SecureRandom;

public class TokenGenerator {
    protected static SecureRandom random = new SecureRandom();
    
    public synchronized String generateToken() {
            long longToken = Math.abs( random.nextLong() );
            String random = Long.toString( longToken, 16 );
            return random;
    }
}
