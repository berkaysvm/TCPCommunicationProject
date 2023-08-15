package com.example.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class App 
{
    // Matematiksel işlemleri tanımlanamak oluşturulan bir enum
    enum Operation {
        ADD,
        SUBTRACT,
        MULTIPLY,
        DIVIDE
    }
    public static void main(String[] args) throws IOException {
        // Sabit sayıda iş parçacığı oluştur.
        ExecutorService executor = Executors.newFixedThreadPool(1000);

            // Belirlediğimiz sayıda işlem yapılması için bir for döngüsü
            for (int i = 0; i < 1000; i++) {

                int index = i;
                // "executor.submit(() -> {}" ifadesi gönderilen iş parçasını asenkron olarak yürütülmesi için iş parçacığı havuzuna eklenir.
                executor.submit(() -> {
                    try (// Yeni bir socket bağlantısı oluşturulur.
                         Socket socket = new Socket("localhost", 12345);
                         //Verilen sockete yazma işlemi yapılması için bir nesne oluşturulur.
                         PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                         //Verilen socketten okuma işlemi yapılması için bir nesne oluşturulur.
                         BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                         //Random nesnesi oluşturulur.
                         Random random = new Random();
                         //Random sayılar oluşturuur.
                        int num1 = random.nextInt(100);
                        int num2 = random.nextInt(100);
                        // "Operation" enumundan bir operation seçilir.
                        Operation operation = Operation.values()[random.nextInt(Operation.values().length)];

                        //Oluşturulan random değerler ile bir request string'i oluşturulur.
                        String requestJson = "{\"id\":" + index + ",\"operation\":\"" + operation.name() + "\",\"num1\":" + num1 + ",\"num2\":" + num2 + "}";
                        // PrintWriter nesnesi üzerinden string ifade socket'e gönderilir.
                        writer.println(requestJson);

                        // BufferedReader üzerinden socketten gelen cevabı okur ve "responseJson" adlı string değişken tanımlanır.
                        String responseJson = reader.readLine();
                        // ObjectMapper nesnesi oluşturulur.
                        ObjectMapper objectMapper = new ObjectMapper();
                        try {
                            //ObjectMapper nesnesi ile string ifadeler JsonNode nesnesine dönüştürülür ve değerler değişkenlere atanır.
                            JsonNode responseNode = objectMapper.readTree(responseJson);
                            int id = responseNode.get("id").asInt();
                            double result = responseNode.get("result").asDouble();
                            String sortList = responseNode.get("sort").toString();
                            //Program çıktıları yazdırılır.
                            System.out.println(requestJson);
                            System.out.println("Client - ID: " + id + " - Result: " + result + " Sort List: " + sortList);
                        }
                        // JSON verisi okunurken veya yazılırken hatalar meydana gelirse yakalanır.
                        catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    }
                    //girdi/çıktı işlemleri sırasında hataların yakalanmasını sağlar.
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                });

            }


        //ExecutorService'in çalışmasını sonlandırmak ve tüm iş parçacıklarının tamamlanmasını beklemek için kullanılır.
        executor.shutdown();
    }
}
