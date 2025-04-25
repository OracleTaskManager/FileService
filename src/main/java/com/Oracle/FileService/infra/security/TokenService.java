package com.Oracle.FileService.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    @Value("${jwt.secret.oracle}")
    private String secret;

    public Long getUserId(String token){
        if(token == null){
            throw new RuntimeException("Token is null");
        }
        try{
            Algorithm algorithm = Algorithm.HMAC256(secret);
            DecodedJWT verifier = JWT.require(algorithm)
                    .withIssuer("Oracle Project")
                    .build()
                    .verify(token);

            Long id = verifier.getClaim("id").asLong();
            if(id == null){
                throw new RuntimeException("ID claim is missing");
            }
            return id;
        }catch(JWTVerificationException e){
            System.out.println("JWT Verification failed: " + e.getMessage());
            System.out.println("Secret used: " + (secret != null ? secret : "null"));
            throw new RuntimeException("Token verification failed: " + e.getMessage());
        }
    }
}
