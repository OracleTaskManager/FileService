package com.Oracle.FileService.infra.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        System.out.println("Authorization: " + authHeader);
        if(authHeader != null && SecurityContextHolder.getContext().getAuthentication() == null){
            var token = authHeader.replace("Bearer ", "");
            System.out.println("Token: " + token);
            System.out.println("Header: " + authHeader);
            System.out.println("Authorization: " + request.getHeader("Authorization"));
            System.out.println("Id" + tokenService.getUserId(token));
            try{
                var id = tokenService.getUserId(token);
                var authentication = new UsernamePasswordAuthenticationToken(id,null,null);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }catch(RuntimeException ex){
                System.out.println("Token inválido: " + ex.getMessage());
            }
        }
        filterChain.doFilter(request,response);
    }

}
