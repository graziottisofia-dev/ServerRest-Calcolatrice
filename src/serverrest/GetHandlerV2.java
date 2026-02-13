/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package serverrest;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler per richieste GET - API v2
 * Supporta: +, -, *, /, ^, %, sqrt
 * 
 * @author delfo
 */
public class GetHandlerV2 implements HttpHandler {
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            inviaErrore(exchange, 405, "Metodo non consentito. Usa GET");
            return;
        }
        
        try {
            Map<String, String> parametri = estraiParametri(exchange.getRequestURI().getQuery());
            if (!parametri.containsKey("operando1") || 
                !parametri.containsKey("operatore")) {
                inviaErrore(exchange, 400, 
                    "Parametri mancanti. Necessari: operando1, operatore. " +
                    "Operando2 opzionale (richiesto per operazioni binarie)");
                return;
            }
            double operando1 = Double.parseDouble(parametri.get("operando1"));
            String operatore = parametri.get("operatore");
            
            double risultato;
            double operando2 = 0;
            if (operatore.equalsIgnoreCase("sqrt")) {
                risultato = CalcolatriceServiceV2.calcola(operando1, 0, operatore);
            } else {
                if (!parametri.containsKey("operando2")) {
                    inviaErrore(exchange, 400, 
                        "operando2 richiesto per l'operatore: " + operatore);
                    return;
                }
                operando2 = Double.parseDouble(parametri.get("operando2"));
                risultato = CalcolatriceServiceV2.calcola(operando1, operando2, operatore);
            }
            OperazioneResponseV2 response = new OperazioneResponseV2(
                operando1,
                operando2,
                operatore,
                risultato
            );
            String jsonRisposta = gson.toJson(response);
            inviaRisposta(exchange, 200, jsonRisposta, response.getRequest_id());
            
        } catch (NumberFormatException e) {
            inviaErrore(exchange, 400, "Operandi non validi. Devono essere numeri");
        } catch (IllegalArgumentException e) {
            inviaErrore(exchange, 400, e.getMessage());
        } catch (Exception e) {
            inviaErrore(exchange, 500, "Errore interno del server: " + e.getMessage());
        }
    }
    
    /**
     * Estrae i parametri dalla query string
     */
    private Map<String, String> estraiParametri(String query) {
        Map<String, String> parametri = new HashMap<>();
        
        if (query == null || query.isEmpty()) {
            return parametri;
        }
        
        String[] coppie = query.split("&");
        for (String coppia : coppie) {
            String[] keyValue = coppia.split("=");
            if (keyValue.length == 2) {
                try {
                    String chiave = URLDecoder.decode(keyValue[0], "UTF-8");
                    String valore = URLDecoder.decode(keyValue[1], "UTF-8");
                    parametri.put(chiave, valore);
                } catch (Exception e) {
                }
            }
        }
        
        return parametri;
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