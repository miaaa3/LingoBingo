package com.estsb.QuizIT.Service.Authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.estsb.QuizIT.Entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JWTService {
    @Value("${secret.key}")
    private String secretKey;

    public String generateToken(User user){
            Algorithm algorithm = Algorithm.HMAC256(secretKey.getBytes());
            return JWT.create()
                    .withSubject(user.getEmail()).withExpiresAt(new Date(System.currentTimeMillis()+24*60*60*1000))
                    .withClaim("role",user.getRole().toString())
                    .sign(algorithm);
        }
    }
