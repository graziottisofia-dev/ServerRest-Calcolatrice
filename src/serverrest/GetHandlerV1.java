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

public class GetHandlerV1 implements HttpHandler {
    
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            inviaErrore(exchange, 405, "Metodo non consentito. Usa GET");
            return;
        }
        
        try {
            Map<String, String> parametri = estraiParametri(exchange.getRequestURI().getQuery());
            
            if (!parametri.containsKey("operando1") || 
                !parametri.containsKey("operando2") || 
                !parametri.containsKey("operatore")) {
                inviaErrore(exchange, 400, "Parametri mancanti. Necessari: operando1, operando2, operatore");
                return;
            }
            
            double operando1 = Double.parseDouble(parametri.get("operando1"));
            double operando2 = Double.parseDouble(parametri.get("operando2"));
            String operatore = parametri.get("operatore");
            
            double risultato = CalcolatriceService.calcola(operando1, operando2, operatore);
            
            OperazioneResponseV1 response = new OperazioneResponseV1(operando1, operando2, operatore, risultato);
            
            String jsonRisposta = gson.toJson(response);
            inviaRisposta(exchange, 200, jsonRisposta);
            
        } catch (NumberFormatException e) {
            inviaErrore(exchange, 400, "Operandi non validi. Devono essere numeri");
        } catch (IllegalArgumentException e) {
            inviaErrore(exchange, 400, e.getMessage());
        } catch (Exception e) {
            inviaErrore(exchange, 500, "Errore interno del server: " + e.getMessage());
        }
    }
    
    private Map<String, String> estraiParametri(String query) {
        Map<String, String> parametri = new HashMap<>();
        if (query == null || query.isEmpty()) return parametri;
        
        for (String coppia : query.split("&")) {
            String[] keyValue = coppia.split("=");
            if (keyValue.length == 2) {
                try {
                    parametri.put(URLDecoder.decode(keyValue[0], "UTF-8"),
                                  URLDecoder.decode(keyValue[1], "UTF-8"));
                } catch (Exception e) { }
            }
        }
        return parametri;
    }
    
    private void inviaRisposta(HttpExchange exchange, int codice, String jsonRisposta) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("X-API-Version", "1.0");
        
        byte[] bytes = jsonRisposta.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(codice, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
    
    private void inviaErrore(HttpExchange exchange, int codice, String messaggio) throws IOException {
        Map<String, Object> errore = new HashMap<>();
        errore.put("errore", messaggio);
        errore.put("status", codice);
        inviaRisposta(exchange, codice, gson.toJson(errore));
    }
}