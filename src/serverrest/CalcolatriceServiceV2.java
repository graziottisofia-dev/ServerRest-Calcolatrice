/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package serverrest;

/**
 * Service per calcoli matematici - Versione 2
 * Supporta operazioni base + potenza, modulo, radice quadrata
 * 
 * @author delfo
 */
public class CalcolatriceServiceV2 {
    
    /**
     * Esegue un'operazione matematica tra due operandi
     * 
     * @param op1 Primo operando
     * @param op2 Secondo operando (ignorato per sqrt)
     * @param operatore Operatore: +, -, *, /, ^, %, sqrt
     * @return Risultato dell'operazione
     * @throws IllegalArgumentException se l'operatore non è valido o l'operazione non è permessa
     */
    public static double calcola(double op1, double op2, String operatore) {
        
        if (operatore == null || operatore.trim().isEmpty()) {
            throw new IllegalArgumentException("Operatore non specificato");
        }
        
        switch (operatore.toLowerCase()) {
            case "+":
                return op1 + op2;
                
            case "-":
                return op1 - op2;
                
            case "*":
                return op1 * op2;
                
            case "/":
                if (op2 == 0) {
                    throw new IllegalArgumentException("Divisione per zero non consentita");
                }
                return op1 / op2;
                
            case "^":
            case "pow":
                return Math.pow(op1, op2);
                
            case "%":
            case "mod":
                if (op2 == 0) {
                    throw new IllegalArgumentException("Modulo con divisore zero non consentito");
                }
                return op1 % op2;
                
            case "sqrt":
            case "radice":
                if (op1 < 0) {
                    throw new IllegalArgumentException(
                        "Radice quadrata di numero negativo non consentita");
                }
                return Math.sqrt(op1);
                
            default:
                throw new IllegalArgumentException(
                    "Operatore non valido: " + operatore + 
                    ". Operatori supportati: +, -, *, /, ^, %, sqrt");
        }
    }
}