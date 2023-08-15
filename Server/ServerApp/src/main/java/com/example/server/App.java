package com.example.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Matematiksel işlemleri tanımlanamak oluşturulan bir enum
enum Operation {
        ADD,
        SUBTRACT,
        MULTIPLY,
        DIVIDE
        }
public class App {
    //Senkronize bir şekilde erişilebilen bir liste oluşturuldu.
    private static List<Double> resultList = Collections.synchronizedList(new ArrayList<>());

    public static void main( String[] args ) throws IOException {
        // Bir Socket nesnesi tanımlanır.
        ServerSocket serverSocket = new ServerSocket(12345);
        // Sabit sayıda iş parçacığı oluşturuldu.
        ExecutorService executor = Executors.newFixedThreadPool(1000);

        // Belirlediğimiz sayıda işlem yapılması için bir for döngüsü
        while (true) {
            // Sunucudan gelen bağlantı isteklerini kabul eder.
            Socket clientSocket = serverSocket.accept();
            // "executor.submit(() -> {}" ifadesi gönderilen iş parçasını asenkron olarak yürütülmesi için iş parçacığı havuzuna eklenir.
            executor.submit(() -> {
                try (//Verilen socketten okuma işlemi yapılması için bir nesne oluşturulur.
                     BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     //Verilen sockete yazma işlemi yapılması için bir nesne oluşturulur.
                     PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {
                    // BufferedReader nesnesi ile clientten gelen isteği okur.
                    String inputLine = reader.readLine();
                    // ObjectMapper nesnesi oluşturulur.
                    ObjectMapper objectMapper = new ObjectMapper();
                    // String ifade JsonNode nesnesine dönüştürülür.
                    JsonNode requestNode = objectMapper.readTree(inputLine);

                    //ObjectMapper nesnesi ile değerler değişkenlere atanır.
                    int id = requestNode.get("id").asInt();
                    String operationStr = requestNode.get("operation").asText();
                    int num1 = requestNode.get("num1").asInt();
                    int num2 = requestNode.get("num2").asInt();

                    // Operasyonları işleme ve sonucu hesaplama
                    double result = 0;
                    if (operationStr.equals(Operation.ADD.name())) {
                        result = num1 + num2;
                    } else if (operationStr.equals(Operation.SUBTRACT.name())) {
                        result = num1 - num2;
                    } else if (operationStr.equals(Operation.MULTIPLY.name())) {
                        result = num1 * num2;
                    } else if (operationStr.equals(Operation.DIVIDE.name())) {
                        if (num2 != 0) {
                            result = (double) num1 / num2;
                        }
                    }//siralama işlemi
                    synchronized (resultList) {
                        resultList.add(result);
                        Collections.sort(resultList, Collections.reverseOrder());
                    }
                    // Cevap JSON'u oluşturma
                        String responseJson = "{\"id\":" + id + ",\"result\":" + result + ",\"sort\":" + resultList + "}";
                    // result değişkeni mutlak değer işlemi uygulayarak delay süresinin pozitif bir değer olması sağlanır ve değer long türüne dönüştürülür.
                    long delaytime = (long) Math.abs(result);
                    // Thread nesnesi ile delay işlemi uygulanır.
                    Thread.sleep(delaytime);
                    // Yanıt sockete BufferedReader nesnesi ile yazılır.
                    writer.println(responseJson);
                }
               //girdi/çıktı işlemleri sırasında hataların yakalanmasını sağlar.
                catch (IOException e) {
                    e.printStackTrace();
                }
                // Thread işlemlerinde hata yakalanmasını sağlar.
                catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    try {
                        // socketin kapatılmasını sağlar.
                        clientSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }
}
