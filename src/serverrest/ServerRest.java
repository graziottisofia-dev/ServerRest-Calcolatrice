package serverrest;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ServerRest {

    public static void avviaServer(int porta) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(porta), 0);

            // Endpoint v1
            server.createContext("/api/v1/calcola/post", new PostHandlerV1());
            server.createContext("/api/v1/calcola/get",  new GetHandlerV1());

            // Endpoint v2
            server.createContext("/api/v2/calcola/post", new PostHandlerV2());
            server.createContext("/api/v2/calcola/get",  new GetHandlerV2());

            // Pagina di test — serve l'HTML inline, nessun file esterno necessario
            server.createContext("/test", ServerRest::serviHtml);

            // Benvenuto
            server.createContext("/", ServerRest::gestisciBenvenuto);

            server.setExecutor(null);
            server.start();

            System.out.println("==============================================");
            System.out.println("  Server REST avviato sulla porta " + porta);
            System.out.println("==============================================");
            System.out.println("  Pagina di test: http://localhost:" + porta + "/test");
            System.out.println("  v1 POST: http://localhost:" + porta + "/api/v1/calcola/post");
            System.out.println("  v1 GET:  http://localhost:" + porta + "/api/v1/calcola/get");
            System.out.println("  v2 POST: http://localhost:" + porta + "/api/v2/calcola/post");
            System.out.println("  v2 GET:  http://localhost:" + porta + "/api/v2/calcola/get");
            System.out.println("==============================================");

        } catch (IOException e) {
            System.err.println("Errore: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Serve l'HTML direttamente come stringa — nessun file.html da cercare
    private static void serviHtml(HttpExchange exchange) throws IOException {
        String html = getHtml();
        byte[] bytes = html.getBytes("UTF-8");
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private static String getHtml() {
        return "<!DOCTYPE html>\n"
+ "<html>\n"
+ "<head>\n"
+ "    <title>Test Calcolatrice API</title>\n"
+ "    <style>\n"
+ "        * { box-sizing: border-box; margin: 0; padding: 0; }\n"
+ "        body { font-family: Arial, sans-serif; padding: 20px; background: #f5f5f5; }\n"
+ "        h1 { margin-bottom: 20px; color: #333; }\n"
+ "        .container { display: flex; gap: 20px; flex-wrap: wrap; }\n"
+ "        .panel { flex: 1; min-width: 300px; background: white; border-radius: 10px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }\n"
+ "        .panel h2 { margin-bottom: 15px; padding-bottom: 10px; border-bottom: 3px solid; }\n"
+ "        .v1 h2 { border-color: #888; color: #888; }\n"
+ "        .v2 h2 { border-color: #007bff; color: #007bff; }\n"
+ "        label { display: block; margin-top: 10px; margin-bottom: 4px; font-size: 0.9em; color: #555; }\n"
+ "        input, select { width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 5px; font-size: 1em; }\n"
+ "        .buttons { margin-top: 15px; display: flex; gap: 10px; }\n"
+ "        button { flex: 1; padding: 10px; border: none; border-radius: 5px; cursor: pointer; font-size: 0.95em; font-weight: bold; color: white; }\n"
+ "        .v1 .btn-post { background: #666; } .v1 .btn-get { background: #999; }\n"
+ "        .v2 .btn-post { background: #007bff; } .v2 .btn-get { background: #66aaff; }\n"
+ "        button:hover { opacity: 0.85; }\n"
+ "        .risultato { margin-top: 15px; padding: 12px; background: #f8f8f8; border-radius: 5px; border-left: 4px solid #ccc; font-size: 0.9em; min-height: 50px; }\n"
+ "        .risultato.successo { border-color: #28a745; background: #f0fff4; }\n"
+ "        .risultato.errore { border-color: #dc3545; background: #fff0f0; }\n"
+ "        .risultato pre { margin-top: 8px; font-size: 0.82em; overflow-x: auto; white-space: pre-wrap; }\n"
+ "        .meta { font-size: 0.78em; color: #888; margin-top: 6px; }\n"
+ "        #op2-container-v2 { transition: opacity 0.3s; }\n"
+ "    </style>\n"
+ "</head>\n"
+ "<body>\n"
+ "    <h1>&#x1F9EE; Test Calcolatrice REST API</h1>\n"
+ "    <div class=\"container\">\n"
+ "        <div class=\"panel v1\">\n"
+ "            <h2>Versione 1</h2>\n"
+ "            <label>Operando 1</label>\n"
+ "            <input type=\"number\" id=\"v1-op1\" value=\"10\" step=\"0.01\">\n"
+ "            <label>Operatore</label>\n"
+ "            <select id=\"v1-operatore\">\n"
+ "                <option value=\"SOMMA\">SOMMA (+)</option>\n"
+ "                <option value=\"SOTTRAZIONE\">SOTTRAZIONE (-)</option>\n"
+ "                <option value=\"MOLTIPLICAZIONE\">MOLTIPLICAZIONE (*)</option>\n"
+ "                <option value=\"DIVISIONE\">DIVISIONE (/)</option>\n"
+ "            </select>\n"
+ "            <label>Operando 2</label>\n"
+ "            <input type=\"number\" id=\"v1-op2\" value=\"5\" step=\"0.01\">\n"
+ "            <div class=\"buttons\">\n"
+ "                <button class=\"btn-post\" onclick=\"calcolaV1('POST')\">POST</button>\n"
+ "                <button class=\"btn-get\" onclick=\"calcolaV1('GET')\">GET</button>\n"
+ "            </div>\n"
+ "            <div class=\"risultato\" id=\"v1-risultato\">Il risultato apparer&#xE0; qui...</div>\n"
+ "        </div>\n"
+ "        <div class=\"panel v2\">\n"
+ "            <h2>Versione 2</h2>\n"
+ "            <label>Operando 1</label>\n"
+ "            <input type=\"number\" id=\"v2-op1\" value=\"10\" step=\"0.01\">\n"
+ "            <label>Operatore</label>\n"
+ "            <select id=\"v2-operatore\" onchange=\"aggiornaV2()\">\n"
+ "                <option value=\"+\">Somma (+)</option>\n"
+ "                <option value=\"-\">Sottrazione (-)</option>\n"
+ "                <option value=\"*\">Moltiplicazione (*)</option>\n"
+ "                <option value=\"/\">Divisione (/)</option>\n"
+ "                <option value=\"^\">Potenza (^)</option>\n"
+ "                <option value=\"%\">Modulo (%)</option>\n"
+ "                <option value=\"sqrt\">Radice quadrata (&#x221A;)</option>\n"
+ "            </select>\n"
+ "            <div id=\"op2-container-v2\">\n"
+ "                <label>Operando 2</label>\n"
+ "                <input type=\"number\" id=\"v2-op2\" value=\"5\" step=\"0.01\">\n"
+ "            </div>\n"
+ "            <div class=\"buttons\">\n"
+ "                <button class=\"btn-post\" onclick=\"calcolaV2('POST')\">POST</button>\n"
+ "                <button class=\"btn-get\" onclick=\"calcolaV2('GET')\">GET</button>\n"
+ "            </div>\n"
+ "            <div class=\"risultato\" id=\"v2-risultato\">Il risultato apparer&#xE0; qui...</div>\n"
+ "        </div>\n"
+ "    </div>\n"
+ "    <script>\n"
+ "        function aggiornaV2() {\n"
+ "            var op = document.getElementById('v2-operatore').value;\n"
+ "            var c = document.getElementById('op2-container-v2');\n"
+ "            c.style.opacity = op === 'sqrt' ? '0.3' : '1';\n"
+ "            c.style.pointerEvents = op === 'sqrt' ? 'none' : 'auto';\n"
+ "        }\n"
+ "        async function calcolaV1(metodo) {\n"
+ "            var op1 = parseFloat(document.getElementById('v1-op1').value);\n"
+ "            var op2 = parseFloat(document.getElementById('v1-op2').value);\n"
+ "            var operatore = document.getElementById('v1-operatore').value;\n"
+ "            var box = document.getElementById('v1-risultato');\n"
+ "            box.className = 'risultato'; box.innerHTML = 'Caricamento...';\n"
+ "            try {\n"
+ "                var response;\n"
+ "                if (metodo === 'POST') {\n"
+ "                    response = await fetch('/api/v1/calcola/post', {\n"
+ "                        method: 'POST',\n"
+ "                        headers: { 'Content-Type': 'application/json' },\n"
+ "                        body: JSON.stringify({ operando1: op1, operando2: op2, operatore: operatore })\n"
+ "                    });\n"
+ "                } else {\n"
+ "                    response = await fetch('/api/v1/calcola/get?operando1=' + op1 + '&operando2=' + op2 + '&operatore=' + operatore);\n"
+ "                }\n"
+ "                var result = await response.json();\n"
+ "                mostraRisultato('v1', result, null);\n"
+ "            } catch(e) { mostraErrore('v1', e.message); }\n"
+ "        }\n"
+ "        async function calcolaV2(metodo) {\n"
+ "            var op1 = parseFloat(document.getElementById('v2-op1').value);\n"
+ "            var op2 = parseFloat(document.getElementById('v2-op2').value);\n"
+ "            var operatore = document.getElementById('v2-operatore').value;\n"
+ "            var box = document.getElementById('v2-risultato');\n"
+ "            box.className = 'risultato'; box.innerHTML = 'Caricamento...';\n"
+ "            try {\n"
+ "                var response;\n"
+ "                if (metodo === 'POST') {\n"
+ "                    response = await fetch('/api/v2/calcola/post', {\n"
+ "                        method: 'POST',\n"
+ "                        headers: { 'Content-Type': 'application/json' },\n"
+ "                        body: JSON.stringify({ operando1: op1, operando2: op2, operatore: operatore })\n"
+ "                    });\n"
+ "                } else {\n"
+ "                    var url = '/api/v2/calcola/get?operando1=' + op1 + '&operatore=' + encodeURIComponent(operatore);\n"
+ "                    if (operatore !== 'sqrt') url += '&operando2=' + op2;\n"
+ "                    response = await fetch(url);\n"
+ "                }\n"
+ "                var rid = response.headers.get('X-Request-ID');\n"
+ "                var result = await response.json();\n"
+ "                mostraRisultato('v2', result, rid);\n"
+ "            } catch(e) { mostraErrore('v2', e.message); }\n"
+ "        }\n"
+ "        function mostraRisultato(versione, result, rid) {\n"
+ "            var box = document.getElementById(versione + '-risultato');\n"
+ "            if (result.errore) {\n"
+ "                box.className = 'risultato errore';\n"
+ "                box.innerHTML = '<strong>&#x274C; Errore:</strong> ' + result.errore;\n"
+ "            } else {\n"
+ "                box.className = 'risultato successo';\n"
+ "                var h = '<strong>&#x2705; ' + result.operazione + '</strong>';\n"
+ "                if (versione === 'v2') h += '<div class=\"meta\">&#x1F550; ' + result.timestamp + ' &nbsp;|&nbsp; &#x1F516; ' + (rid || result.request_id) + '</div>';\n"
+ "                h += '<pre>' + JSON.stringify(result, null, 2) + '</pre>';\n"
+ "                box.innerHTML = h;\n"
+ "            }\n"
+ "        }\n"
+ "        function mostraErrore(versione, msg) {\n"
+ "            var box = document.getElementById(versione + '-risultato');\n"
+ "            box.className = 'risultato errore';\n"
+ "            box.innerHTML = '<strong>&#x274C; Errore di connessione:</strong> ' + msg;\n"
+ "        }\n"
+ "    </script>\n"
+ "</body>\n"
+ "</html>";
    }

    private static void gestisciBenvenuto(HttpExchange exchange) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Map<String, Object> info = new HashMap<>();
        info.put("messaggio", "Calcolatrice REST API");
        info.put("test_ui", "http://localhost:8080/test");
        Map<String, String> ep = new HashMap<>();
        ep.put("v1_post", "/api/v1/calcola/post");
        ep.put("v1_get",  "/api/v1/calcola/get");
        ep.put("v2_post", "/api/v2/calcola/post");
        ep.put("v2_get",  "/api/v2/calcola/get");
        info.put("endpoints", ep);
        String json = gson.toJson(info);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        byte[] bytes = json.getBytes("UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }
}