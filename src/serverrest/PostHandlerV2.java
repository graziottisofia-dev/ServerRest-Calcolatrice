/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package serverrest;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler per richieste POST - API v2
 * Supporta: +, -, *, /, ^, %, sqrt
 * 
 * @author delfo
 */
public class PostHandlerV2 implements HttpHandler {
    
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            inviaErrore(exchange, 405, "Metodo non consentito. Usa POST");
            return;
        }
        
        try {
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)
            );
           
            OperazioneRequest request = gson.fromJson(reader, OperazioneRequest.class);
            reader.close();
            
            if (request == null) {
                inviaErrore(exchange, 400, "Body della richiesta vuoto o non valido");
                return;
            }
            
            if (request.getOperatore() == null || request.getOperatore().trim().isEmpty()) {
                inviaErrore(exchange, 400, "Operatore mancante o vuoto");
                return;
            }
            
            double risultato;
            
            if (request.getOperatore().equalsIgnoreCase("sqrt")) {
                risultato = CalcolatriceServiceV2.calcola(
                    request.getOperando1(),
                    0,
                    request.getOperatore()
                );
            } else {

                risultato = CalcolatriceServiceV2.calcola(
                    request.getOperando1(),
                    request.getOperando2(),
                    request.getOperatore()
                );
            }
            
            OperazioneResponseV2 response = new OperazioneResponseV2(
                request.getOperando1(),
                request.getOperando2(),
                request.getOperatore(),
                risultato
            );
            
            String jsonRisposta = gson.toJson(response);
           
            inviaRisposta(exchange, 200, jsonRisposta, response.getRequest_id());
            
        } catch (JsonSyntaxException e) {
            inviaErrore(exchange, 400, "JSON non valido: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            inviaErrore(exchange, 400, e.getMessage());
        } catch (Exception e) {
            inviaErrore(exchange, 500, "Errore interno del server: " + e.getMessage());
        }
    }
    
    /**
     * Invia una risposta di successo con X-Request-ID header
     */
    private void inviaRisposta(HttpExchange exchange, int codice, 
            String jsonRisposta, String requestId) throws IOException {
        
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("X-Request-ID", requestId);
        exchange.getResponseHeaders().set("X-API-Version", "2.0");
        
        byte[] bytes = jsonRisposta.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(codice, bytes.length);
        
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
    
    /**
     * Invia una risposta di errore in formato JSON
     */
    private void inviaErrore(HttpExchange exchange, int codice, String messaggio) 
            throws IOException {
        
        Map<String, Object> errore = new HashMap<>();
        errore.put("errore", messaggio);
        errore.put("status", codice);
        errore.put("versione_api", "2.0");
        
        String jsonErrore = gson.toJson(errore);
        
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("X-API-Version", "2.0");
        
        byte[] bytes = jsonErrore.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(codice, bytes.length);
        
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}